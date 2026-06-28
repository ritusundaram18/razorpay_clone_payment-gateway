package com.codingshuttle.razorpay.vault.entity;

import com.codingshuttle.razorpay.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "vault_card")
public class VaultCard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,length =4)
    private String lastFour;

    @Column(nullable = false,length =6)private String bin;

   @Column(nullable = false)
   private  byte[] encryptedPan;

    @Column(nullable = false)
    private byte[] encryptedDek;

    @Column(nullable = false)
    private String brand;
    @Column(nullable = false)
    private String expiryMonth;
    @Column(nullable = false)
    private String expiryYear;
    @Column(nullable = false)
    private  String cardholderName;

    private LocalDateTime deletedAt;


}
