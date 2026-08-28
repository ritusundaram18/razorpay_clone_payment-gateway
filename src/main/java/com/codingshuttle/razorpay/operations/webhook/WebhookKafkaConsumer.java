package com.codingshuttle.razorpay.operations.webhook;

import com.codingshuttle.razorpay.common.dto.WebhookTarget;
import com.codingshuttle.razorpay.common.enums.WebhookEventStatus;
import com.codingshuttle.razorpay.merchant.api.MerchantLookupService;
import com.codingshuttle.razorpay.operations.entity.WebhookEvent;
import com.codingshuttle.razorpay.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;
import tools.jackson.databind.ObjectMapper;
import com.codingshuttle.razorpay.common.utl.SignerUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookKafkaConsumer {
    private final MerchantLookupService merchantLookupService;
    private final ObjectMapper objectMapper;
    private final SignerUtil signerUtil;

        private final WebhookEventRepository webhookEventRepository;
    private final WebhookRetryQueue retryQueue;
    private final WebhookDlqRecorder dlqRecorder;
    @KafkaListener(topics = {
            "${app.kafka.topics.payments:payments.events}",
            "${app.kafka.topics.orders:orders.events}",
            "${app.kafka.topics.refunds:refunds.events}",
            "${app.kafka.topics.settlements:settlements.events}"
    })

    public void onWebhookEvent(ConsumerRecord<String, Map<String, Object>> record, Acknowledgment ack) {
        try {
            log.info("========== WEBHOOK CONSUMER STARTED ==========");
            log.info("Topic: {}", record.topic());
            log.info("Offset: {}", record.offset());
            log.info("Kafka record value: {}", record.value());

        Map<String, Object> envelope = record.value();
        Map<String, Object> data = (Map<String, Object>) envelope.get("data");
        String eventType = (String) envelope.get("eventType");


            log.info("Event type: {}", eventType);
            log.info("Data: {}", data);

        Object merchantIdRaw = data.get("merchantId");
            log.info("merchantIdRaw: {}", merchantIdRaw);
        if (merchantIdRaw == null) {
            log.warn("No merchantId was found, skipping event: {}", eventType);
            ack.acknowledge();
            return;
        }

        UUID merchantId = UUID.fromString(merchantIdRaw.toString());

        List<WebhookTarget> targets = merchantLookupService.getActiveConfigsForEvent(merchantId, eventType);
        if (targets.isEmpty()) {
            log.debug("No webhook target was found, skipping event: {}", eventType);
            ack.acknowledge();
            return;
        }
        Map<String, Object> signatureData = Map.of("event", eventType, "payload", data);
        String signatureJson = objectMapper.writeValueAsString(signatureData);
            log.info("Webhook targets found: {}", targets.size());

        for (WebhookTarget target : targets) {
            log.info("Creating WebhookEvent for targetUrl={}",
                    target.targetUrl());

            String signature = signerUtil.sign(signatureJson, target.webhookSecret());

            WebhookEvent webhookEvent = WebhookEvent.builder()
                    .merchantId(merchantId)
                    .eventType(eventType)
                    .payload(data)
                    .targetUrl(target.targetUrl())
                    .signature(signature)
                    .status(WebhookEventStatus.PENDING)
                    .nextRetryAt(LocalDateTime.now())
                    .build();
            log.info("Before WebhookEvent save: {}", webhookEvent);

            webhookEvent = webhookEventRepository.save(webhookEvent);

            log.info("AFTER WebhookEvent save. ID={}",
                    webhookEvent.getId());

            retryQueue.enqueue(webhookEvent.getId(), webhookEvent.getNextRetryAt());
            log.info("Created a webhook event with id: {}", webhookEvent.getId());
        }
            ack.acknowledge();
        }
        catch (DataAccessException | CannotCreateTransactionException dbDown) {
            log.error("Webhook consumer failed due to DB down, Could not process the record, offset: {}", record.offset(), dbDown);
        }
        catch (Exception logicError) {
            log.error("Webhook consumer failed due to logical error, Could not process the record, offset: {}", record.offset(), logicError);
            dlqRecorder.recordConsumerFailed(record, logicError.getMessage());
            ack.acknowledge();
            //TODO: check exception for ack
        }
    }
}