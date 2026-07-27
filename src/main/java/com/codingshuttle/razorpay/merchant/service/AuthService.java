package com.codingshuttle.razorpay.merchant.service;

import com.codingshuttle.razorpay.merchant.dto.Response.LoginResponse;
import com.codingshuttle.razorpay.merchant.dto.Response.MerchantResponse;
import com.codingshuttle.razorpay.merchant.dto.request.LoginRequest;
import com.codingshuttle.razorpay.merchant.dto.request.MerchantSignupRequest;

public interface AuthService  {

    MerchantResponse signup(MerchantSignupRequest request);

    LoginResponse login(LoginRequest request);
}
