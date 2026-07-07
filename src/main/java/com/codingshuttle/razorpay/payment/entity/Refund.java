package com.codingshuttle.razorpay.payment.entity;

import com.codingshuttle.razorpay.common.entity.BaseEntity;
import com.codingshuttle.razorpay.common.entity.Money;
import com.codingshuttle.razorpay.common.enums.RefunsdStatus;
import com.codingshuttle.razorpay.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Refund extends BaseEntity {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private UUID uuid;

    @ManyToOne(fetch= FetchType.LAZY,optional=false)
    @JoinColumn(name="payment_id",nullable = false)
    private Payment payment;

    @JoinColumn(nullable = false)
    private UUID merchantId;
    @Embedded
    private Money amount;


    @Enumerated(EnumType.STRING)
    @JoinColumn(nullable = false)
    private RefunsdStatus status=RefunsdStatus.PENDING;


    @Column(length = 100)
    private String bankReference;

    @Column(length = 100)
    private String errorCode;
    @Column(length = 100)
    private String errorDescription;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String,Object> notes;

    private LocalDateTime processdAt;

}
