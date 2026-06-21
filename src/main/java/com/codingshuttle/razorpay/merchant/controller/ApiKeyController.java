package com.codingshuttle.razorpay.merchant.controller;


import com.codingshuttle.razorpay.merchant.dto.Response.ApiKeyCreateResponse;
import com.codingshuttle.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.codingshuttle.razorpay.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/api-keys")
@RequiredArgsConstructor

public class ApiKeyController {


    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> create(@PathVariable UUID merchantId,
                                                       @Valid @RequestBody CreateApiKeyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiKeyService.create(merchantId, request));
    }

//        @GetMapping
//        public ResponseEntity<List<ApiKeyResponse>> listByMerchant(@PathVariable UUID merchantId) {
//            return ResponseEntity.ok(apiKeyService.listByMerchant(merchantId));
//        }
//
//        @DeleteMapping("/keyId")
//        public ResponseEntity<Void> revoke(@PathVariable UUID merchantId, @PathVariable UUID keyId) {
//            apiKeyService.revoke(merchantId, keyId);
//            return ResponseEntity.noContent().build();
//        }
//
//        @PostMapping("/{keyId}/rotate")
//        public ResponseEntity<ApiKeyCreateResponse> rotateKey(@PathVariable UUID merchantId, @PathVariable UUID keyId) {
//            return ResponseEntity.ok(apiKeyService.rotate(merchantId, keyId));
//        }
//
//

}
