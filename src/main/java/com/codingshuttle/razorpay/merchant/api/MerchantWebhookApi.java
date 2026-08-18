package com.codingshuttle.razorpay.merchant.api;

import com.codingshuttle.razorpay.common.dto.WebhookTarget;

import java.util.List;
import java.util.UUID;

//import com.codingshuttle.razorpay.common.dto.WebhookTarget;


public interface MerchantWebhookApi {

    List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId, String eventType);

}
