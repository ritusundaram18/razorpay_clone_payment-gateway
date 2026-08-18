package com.codingshuttle.razorpay.merchant.security;

import com.codingshuttle.razorpay.common.exception.RateLimitException;
import com.codingshuttle.razorpay.common.ratelimit.RateLimitResult;
import com.codingshuttle.razorpay.common.ratelimit.RateLimiter;
import com.codingshuttle.razorpay.merchant.cache.ApiKeyCache;
import com.codingshuttle.razorpay.merchant.cache.ApiKeyCacheEntry;
import com.codingshuttle.razorpay.merchant.entity.ApiKey;
import com.codingshuttle.razorpay.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String BASIC_PREFIX = "Basic ";

    private final ApiKeyRepository apiKeyRepository;
    private final MerchantContext merchantContext;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ApiKeyCache apiKeyCache;
    private final RateLimiter rateLimiter;

    @Value("${app.rate-limit.use-case.api-key.requests-per-minute:60}")
    private Integer requestsPerMinute;

    private final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        log.info("========== API KEY FILTER START ==========");
        log.info("Incoming request: {}", request.getRequestURI());

        try {

            String header = request.getHeader("Authorization");

            log.info("Authorization header present: {}", header != null);

            if (header == null || !header.startsWith("Basic")) {
                log.info("No Basic Authorization header. Passing request to next filter.");
                filterChain.doFilter(request, response);
                return;
            }

            String[] credentials = decode(header);

            if (credentials == null) {
                log.error("Could not decode API Key header");
                throw new BadRequestException("Malformed API Key Header");
            }

            String keyId = credentials[0];
            String rawSecret = credentials[1];

            log.info("API key decoded successfully. keyId={}", keyId);

            // Check Redis cache first, then DB
            ApiKeyCacheEntry apiKeyEntry =
                    apiKeyCache.get(keyId)
                            .orElseGet(() -> loadAndCache(keyId));

            log.info("API key cache/DB lookup completed. Found={}",
                    apiKeyEntry != null);

            if (apiKeyEntry == null) {
                log.error("API key not found. keyId={}", keyId);
                throw new BadRequestException("Invalid or missing API Key");
            }

            log.info("API key enabled={}", apiKeyEntry.enabled());

            boolean secretMatched = secretMatches(rawSecret, apiKeyEntry);

            log.info("API key secret matched={}", secretMatched);

            if (!apiKeyEntry.enabled() || !secretMatched) {
                log.error("API key validation failed. keyId={}", keyId);
                throw new BadRequestException("Invalid or missing API Key");
            }

            log.info("API KEY VALIDATION PASSED. keyId={}", keyId);

            // Rate limiting
            log.info("Checking rate limit for keyId={}", keyId);

            RateLimitResult rateLimitResult =
                    rateLimiter.check(
                            "apiKey" + keyId,
                            requestsPerMinute,
                            60
                    );

            log.info("Rate limit result: {}", rateLimitResult);

            if (!rateLimitResult.isAllowed()) {

                log.warn("Too many requests. keyId={}", keyId);

                throw new RateLimitException(
                        "Too many requests",
                        rateLimitResult.retryAfterSeconds()
                );
            }

            log.info("RATE LIMIT PASSED. keyId={}", keyId);

            response.setHeader(
                    "X-RateLimit-Limit",
                    String.valueOf(requestsPerMinute)
            );

            response.setHeader(
                    "X-RateLimit-Remaining",
                    String.valueOf(rateLimitResult.remaining())
            );

            // Set Spring Security authentication
            var auth = new UsernamePasswordAuthenticationToken(
                    keyId,
                    null,
                    List.of(new SimpleGrantedAuthority("API_KEY_ROLE"))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            log.info("Spring Security authentication set");

            // Set merchant context
            merchantContext.setMerchantId(apiKeyEntry.merchantId());
            merchantContext.setKeyId(apiKeyEntry.keyId());

            log.info(
                    "MerchantContext set. merchantId={}, keyId={}",
                    apiKeyEntry.merchantId(),
                    apiKeyEntry.keyId()
            );

            log.info("PASSING REQUEST TO CONTROLLER");

            filterChain.doFilter(request, response);

            log.info("REQUEST RETURNED FROM CONTROLLER");

        } catch (Exception e) {

            log.error(
                    "API KEY FILTER FAILED. request={}, error={}",
                    request.getRequestURI(),
                    e.getMessage(),
                    e
            );

            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    e
            );
        }

        log.info("========== API KEY FILTER END ==========");
    }

    private ApiKeyCacheEntry loadAndCache(String keyId) {

        log.info("Loading API key from database. keyId={}", keyId);

        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId)
                .orElse(null);

        if (apiKey == null) {
            log.warn("No API key found in database. keyId={}", keyId);
            return null;
        }

        log.info(
                "API key found in database. keyId={}, merchantId={}, enabled={}",
                apiKey.getKeyId(),
                apiKey.getMerchant().getId(),
                apiKey.isEnabled()
        );

        ApiKeyCacheEntry apiKeyCacheEntry =
                new ApiKeyCacheEntry(
                        apiKey.getKeyId(),
                        apiKey.getKeySecretHash(),
                        apiKey.getPreviousKeySecretHash(),
                        apiKey.getGracePeriodExpiresAt(),
                        apiKey.getMerchant().getId(),
                        apiKey.getEnvironment(),
                        apiKey.isEnabled()
                );

        apiKeyCache.put(keyId, apiKeyCacheEntry);

        log.info("API key stored in cache. keyId={}", keyId);

        return apiKeyCacheEntry;
    }

    private boolean secretMatches(
            String rawSecret,
            ApiKeyCacheEntry apiKey) {

        log.info("Checking API key secret");

        if (BCRYPT.matches(rawSecret, apiKey.keySecretHash())) {
            log.info("Current API key secret matched");
            return true;
        }

        boolean previousSecretMatched =
                apiKey.isInGracePeriod()
                        && apiKey.previousKeySecretHash() != null
                        && BCRYPT.matches(
                        rawSecret,
                        apiKey.previousKeySecretHash()
                );

        if (previousSecretMatched) {
            log.info("Previous API key secret matched during grace period");
        } else {
            log.warn("API key secret did not match");
        }

        return previousSecretMatched;
    }

    private String[] decode(String header) {

        String encoded =
                header.substring(BASIC_PREFIX.length());

        String decoded =
                new String(
                        Base64.getDecoder().decode(encoded),
                        StandardCharsets.UTF_8
                );

        int colon = decoded.indexOf(":");

        if (colon < 1) {
            return null;
        }

        return new String[]{
                decoded.substring(0, colon),
                decoded.substring(colon + 1)
        };
    }
}







//package com.codingshuttle.razorpay.merchant.security;
//
//import com.codingshuttle.razorpay.common.exception.RateLimitException;
//import com.codingshuttle.razorpay.common.ratelimit.RateLimitResult;
//import com.codingshuttle.razorpay.common.ratelimit.RateLimiter;
//import com.codingshuttle.razorpay.merchant.cache.ApiKeyCache;
//import com.codingshuttle.razorpay.merchant.cache.ApiKeyCacheEntry;
//import com.codingshuttle.razorpay.merchant.entity.ApiKey;
//import com.codingshuttle.razorpay.merchant.repository.ApiKeyRepository;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
////import lombok.Value;
//import org.springframework.beans.factory.annotation.Value;  // ✅
//import lombok.extern.slf4j.Slf4j;
//import org.apache.coyote.BadRequestException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import org.springframework.web.servlet.HandlerExceptionResolver;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//import java.util.Base64;
//import java.util.List;
//import java.util.UUID;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
//
//    private static final String BASIC_PREFIX = "Basic ";
//    private final ApiKeyRepository apiKeyRepository;
//    private final MerchantContext merchantContext;
//    private final HandlerExceptionResolver handlerExceptionResolver;
//
//    private final ApiKeyCache apiKeyCache;
//    private final RateLimiter rateLimiter;
//
////    @Value("${app.rate-limit.use-case.api-key.requests-per-minute:60}")
////    private Integer requestsPerMinute;
//
//    @Value("${app.rate-limit.use-case.api-key.requests-per-minute:60}")
//    private Integer requestsPerMinute;
//    private final BCryptPasswordEncoder BCRYPT= new BCryptPasswordEncoder();
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        log.info("Incoming request: {}", request.getRequestURI());
//        try {
//            String header = request.getHeader("Authorization");
//            if (header == null || !header.startsWith("Basic")) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//            //Authorization: Basic key _asdffghh:secret_agghfasd
//            //Authorization: Basic ASDENGB EKRkjnvdjbffjv
//
//            String[] credentials = decode(header);
//            if (credentials == null) {
//                throw new BadRequestException("Malformed API Key Header");
//            }
//
//            String keyId = credentials[0];
//            String rawSecret = credentials[1];
//            //when data found in the redis cache then good, if not found then we can check in db and then put it in cache
//            ApiKeyCacheEntry apiKeyEntry = apiKeyCache.get(keyId).orElseGet(() -> loadAndCache(keyId));
//
////            ApiKey apiKey = apiKeyRepository.findByKeyId(keyId)
////                    .orElseThrow(() -> new BadRequestException("Invalid or missing API Key"));
//// here we are making db call, after redis, we do not need to make db call,
////                    we can check in redis cache first, if not found then we can check in db and then put it in cache
//
//            if (apiKeyEntry==null ||!apiKeyEntry.enabled() || !secretMatches(rawSecret, apiKeyEntry)) {
//                throw new BadRequestException("Invalid or missing API Key");
//            }
//
//            RateLimitResult rateLimitResult = rateLimiter.check("apiKey"+keyId, requestsPerMinute, 60); // 100 requests per minute
//            if(!rateLimitResult.isAllowed()) {
//                log.warn("To many requests keyId={}",keyId);
//                throw new RateLimitException("Too many requests", rateLimitResult.retryAfterSeconds());
//            }
//            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
//            response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimitResult.remaining()));
//
//
//            var auth = new UsernamePasswordAuthenticationToken(keyId, null,
//                    List.of(new SimpleGrantedAuthority("API_KEY_ROLE"))
//            );
//
//            SecurityContextHolder.getContext().setAuthentication(auth);
//
//            merchantContext.setMerchantId(apiKeyEntry.merchantId());
//            merchantContext.setKeyId(apiKeyEntry.keyId());
//
//            filterChain.doFilter(request, response);
//
//        }
//        catch (Exception e) {
//            handlerExceptionResolver.resolveException(request, response, null, e);
//        }
//    }
//
//    private ApiKeyCacheEntry loadAndCache(String keyId) {
//        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId)
//                .orElse(null);
//
//        if(apiKey == null) {
//            return null;
//        }
//
//        ApiKeyCacheEntry apiKeyCacheEntry = new ApiKeyCacheEntry(
//                apiKey.getKeyId(),
//                apiKey.getKeySecretHash(),
//                apiKey.getPreviousKeySecretHash(),
//                apiKey.getGracePeriodExpiresAt(),
//                apiKey.getMerchant().getId(),
//                apiKey.getEnvironment(),
//                apiKey.isEnabled()
//        );
//
//        apiKeyCache.put(keyId, apiKeyCacheEntry);
//        return apiKeyCacheEntry;
//    }
//
//    private boolean secretMatches(String rawSecret, ApiKeyCacheEntry apiKey) {
//
//        if (BCRYPT.matches(rawSecret, apiKey.keySecretHash())) {
//            return true;
//        }
//
//        return apiKey.isInGracePeriod() && apiKey.previousKeySecretHash()!=null
//                && BCRYPT.matches(rawSecret, apiKey.previousKeySecretHash());
//}
//
//    private String[] decode(String header){
//        String encoded=header.substring(BASIC_PREFIX.length());
//        String decoded=new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
//
//
//        int colon=decoded.indexOf(":");
//        if(colon<1) return null;
//        return new String[]{decoded.substring(0,colon),decoded.substring(colon+1)};
//
//    }
//}
