package com.codingshuttle.razorpay.merchant.entity;

import com.codingshuttle.razorpay.common.enums.Environment;
import com.codingshuttle.razorpay.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
@Entity
@Table(name = "api_user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AppUser {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="merchant_id")
    private Merchant merchant;

    @Column(unique = true,nullable = false)
    private String email;
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

}
