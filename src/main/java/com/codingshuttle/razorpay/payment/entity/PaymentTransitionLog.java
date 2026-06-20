package com.codingshuttle.razorpay.payment.entity;

import com.codingshuttle.razorpay.common.enums.PaymentEvent;
import com.codingshuttle.razorpay.common.enums.PaymentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "payment_transition_log")
public class PaymentTransitionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch= FetchType.LAZY,optional=false)
    @JoinColumn(name = "payment_id",nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name="from_status",length = 20)
    private PaymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "event", nullable = false, length = 30)
    private PaymentEvent event;

    @Enumerated(EnumType.STRING)
    @Column(name="to_status",nullable = false,length = 20)
    private PaymentStatus toStatus;


    @Column(name = "actor",length = 50)
    private  String actor; // Could be system, user, or external service

    @Column(name = "occurred_at",nullable = false)
    private LocalDateTime occuredAt;

}
