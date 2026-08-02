package com.codingshuttle.razorpay.merchant.controller;

import com.codingshuttle.razorpay.merchant.dto.Response.LoginResponse;
import com.codingshuttle.razorpay.merchant.dto.Response.MerchantResponse;
import com.codingshuttle.razorpay.merchant.dto.request.LoginRequest;
import com.codingshuttle.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.codingshuttle.razorpay.merchant.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup123")
    public ResponseEntity<MerchantResponse> signup(@RequestBody @Valid MerchantSignupRequest request) {
        log.info("Inside signup controller");

        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }
}