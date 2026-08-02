package com.codingshuttle.razorpay.merchant.controller;


import com.codingshuttle.razorpay.merchant.dto.Response.ApiKeyCreateResponse;
import com.codingshuttle.razorpay.merchant.dto.Response.ApiKeyResponse;
import com.codingshuttle.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.codingshuttle.razorpay.merchant.security.MerchantContext;
import com.codingshuttle.razorpay.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/api-keys")
@RequiredArgsConstructor
@Slf4j

public class ApiKeyController {


    private final ApiKeyService apiKeyService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> create(
            @Valid @RequestBody CreateApiKeyRequest request) {

        log.info("Inside ApiKeyController#create");

        ApiKeyCreateResponse response =
                apiKeyService.create(merchantContext.getMerchantId(), request);

        log.info("Returning response: {}", response);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

        @GetMapping
        public ResponseEntity<List<ApiKeyResponse>> listByMerchant() {
            return ResponseEntity.ok(apiKeyService.listByMerchant(merchantContext.getMerchantId()));
        }
//
        @DeleteMapping("/keyId")
        public ResponseEntity<Void> revoke( @PathVariable UUID keyId) {
            apiKeyService.revoke(merchantContext.getMerchantId(), keyId);
            return ResponseEntity.noContent().build();
        }
//
        @PostMapping("/{keyId}/rotate")
        public ResponseEntity<ApiKeyCreateResponse> rotateKey(@PathVariable UUID keyId) {
            return ResponseEntity.ok(apiKeyService.rotate(merchantContext.getMerchantId(), keyId));
        }
//
//

}
