package com.codingshuttle.razorpay.common.enums;

public enum PaymentEvent {
    AUTHORIZE_ATTEMPT,
    AUTHORIZE_SUCCESS,

    AUTHORIZE_FAIL,
    CAPTURE_REQUEST,
    CAPTURE_SUCCESS,
    REFUND_INIT, REFUND_COMPLETED, SETTLE, CANCEL,
    CAPTURE_TIMEOUT
}
