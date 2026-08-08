package com.codingshuttle.razorpay.merchant.repository;

import com.codingshuttle.razorpay.merchant.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByEmail(String email);
    Optional<Customer> findByIdAndMerchantId(UUID customerId, UUID merchantId);

    Optional<Customer>findByMerchant_IdAndEmail(UUID merchantId, String email);
}
