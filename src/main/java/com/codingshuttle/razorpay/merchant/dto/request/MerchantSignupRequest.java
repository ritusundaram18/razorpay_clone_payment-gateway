package com.codingshuttle.razorpay.merchant.dto.request;

import com.codingshuttle.razorpay.common.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MerchantSignupRequest(
        @NotNull(message = "name is required")
        @Size(max = 50, message = "name must be less than 50 characters")
        String name,
        @Email
        @NotNull(message = "email is required")
        String email,
        @NotNull(message = "password is required")
        @Size(min = 8, message = "password must be at least 8 characters")
        String password,
        @Size(max = 100, message = "business name must be less than 50 characters")
        String businessName,
        BusinessType businessType) {

}
