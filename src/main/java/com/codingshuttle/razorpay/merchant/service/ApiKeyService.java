package com.codingshuttle.razorpay.merchant.service;

//import com.codingshuttle.razorpay.merchant.dto.request.CreateApiKeyRequest;
//import com.codingshuttle.razorpay.merchant.dto.response.ApiKeyCreateResponse;
//import com.codingshuttle.razorpay.merchant.dto.response.ApiKeyResponse;

import com.codingshuttle.razorpay.merchant.dto.Response.ApiKeyCreateResponse;
import com.codingshuttle.razorpay.merchant.dto.request.CreateApiKeyRequest;

import java.util.UUID;

public interface ApiKeyService {

    ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request);
//
//    List<ApiKeyResponse> listByMerchant(UUID merchantId);
//
//    void revoke(UUID merchantId, UUID keyId);
//
//    @Nullable ApiKeyCreateResponse rotate(UUID merchantId, UUID keyId);
}

