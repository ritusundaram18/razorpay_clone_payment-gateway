package com.codingshuttle.razorpay.vault.entity;

import com.codingshuttle.razorpay.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class CardToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;
    @Column(nullable = false,length = 50)
    private  String token;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @Column(name = "vault_card_id",nullable = false)
    private  VaultCard vaultCard;

    @Column(nullable = false)
    private  UUID customer;
    @Column(nullable = false)
    private  UUID merchant;

    private LocalDateTime revokedAt;

}
