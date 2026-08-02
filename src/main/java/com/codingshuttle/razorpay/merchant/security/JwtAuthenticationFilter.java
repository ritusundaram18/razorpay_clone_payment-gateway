package com.codingshuttle.razorpay.merchant.security;


import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final MerchantContext merchantContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Incoming request: {}", request.getRequestURI());

        try {
            final String authorizationHeader = request.getHeader("Authorization");
            log.info("Authorization header: {}", authorizationHeader);//this
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer")) {
                log.info("No Authorization header. Calling filterChain.doFilter()");//this

                filterChain.doFilter(request, response);
                log.info("Returned from filterChain.doFilter()");//this
                log.info("Response status: {}", response.getStatus());//this
                return;
            }

            String jwtToken = authorizationHeader.substring("Bearer ".length());

            Claims claims = jwtUtil.verifyAccessToken(jwtToken);

            if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + jwtUtil.extractRole(claims)))
                );

                SecurityContextHolder.getContext().setAuthentication(auth);

                merchantContext.setMerchantId(UUID.fromString(jwtUtil.extractMerchantId(claims)));
//                merchantContext.setMerchantId(jwtUtil.extractMerchantId(claims));

            }
            log.info("JWT verified. Calling filterChain.doFilter()");

            filterChain.doFilter(request, response);
            log.info("Returned from filterChain.doFilter()");
            log.info("Response status: {}", response.getStatus());
        } catch (Exception e) {
            log.error("Exception in JwtAuthenticationFilter", e);//this
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}
