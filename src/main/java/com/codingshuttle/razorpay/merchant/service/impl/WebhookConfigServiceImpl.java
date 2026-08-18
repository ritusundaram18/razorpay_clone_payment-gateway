package com.codingshuttle.razorpay.merchant.service.impl;



import com.codingshuttle.razorpay.common.exception.ResourceNotFoundException;
import com.codingshuttle.razorpay.common.utl.RandomizerUtil;
import com.codingshuttle.razorpay.merchant.api.MerchantWebhookApi;
import com.codingshuttle.razorpay.merchant.dto.Response.WebhookConfigResponse;
import com.codingshuttle.razorpay.common.dto.WebhookTarget;
import com.codingshuttle.razorpay.merchant.dto.request.UpdateWebhookConfigRequest;
import com.codingshuttle.razorpay.merchant.entity.Merchant;
import com.codingshuttle.razorpay.merchant.entity.MerchantWebhookConfig;
import com.codingshuttle.razorpay.merchant.mapper.WebhookConfigMapper;
import com.codingshuttle.razorpay.merchant.repository.MerchantRepository;
import com.codingshuttle.razorpay.merchant.repository.WebhookConfigRepository;
import com.codingshuttle.razorpay.merchant.service.WebhookConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookConfigServiceImpl implements WebhookConfigService, MerchantWebhookApi {


    private final MerchantRepository merchantRepository;
    private final WebhookConfigRepository merchantWebhookConfigRepository;
//    private final BytesEncryptor bytesEncryptor;
//@Qualifier("dekEncrypter")
private final BytesEncryptor bytesEncryptor;


    private final WebhookConfigMapper webhookConfigMapper;

//    private final MerchantRepository merchantRepository;
//    private final WebhookConfigRepository merchantWebhookConfigRepository;
//
//    private final BytesEncryptor bytesEncryptor;
//
//    private final WebhookConfigMapper webhookConfigMapper;

//    public WebhookConfigServiceImpl(
//            MerchantRepository merchantRepository,
//            WebhookConfigRepository merchantWebhookConfigRepository,
//            @Qualifier("dekEncrypter") BytesEncryptor bytesEncryptor,
//            WebhookConfigMapper webhookConfigMapper) {
//
//        this.merchantRepository = merchantRepository;
//        this.merchantWebhookConfigRepository = merchantWebhookConfigRepository;
//        this.bytesEncryptor = bytesEncryptor;
//        this.webhookConfigMapper = webhookConfigMapper;
//    }

    @Override
        public WebhookConfigResponse create(UUID merchantId, UpdateWebhookConfigRequest request) {

            Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId));
//
        String rawSecret = RandomizerUtil.randomBase64(32);
        byte[] rawSecretBytes = rawSecret.getBytes(StandardCharsets.UTF_8);

        String encryptedSecret = Base64.getEncoder().encodeToString
                (bytesEncryptor.encrypt(rawSecretBytes));
//
        MerchantWebhookConfig config = MerchantWebhookConfig.builder()
                .merchant(merchant)
                .targetUrl(request.targetUrl())
                .enabled(true)
                .eventTypes(request.eventTypes())
                .webhookSecret(encryptedSecret)
                .build();
//
        config = merchantWebhookConfigRepository.save(config);
//
//        return webhookConfigMapper.toResponse(config, rawSecret);
        WebhookConfigResponse response =
                webhookConfigMapper.toResponse(config, rawSecret);

        log.info("Webhook response = {}", response);

        return response;
    }
//
    @Override
    public List<WebhookConfigResponse> list(UUID merchantId) {//thiosefessewfnssohsbssjthrthrnhorhnothhhtthggrggenlrgrrejtgrgtjhuhgrgbrtbgrtgerohp
        // egefgfgmkegbng'eropgnvgerbegg
        return merchantWebhookConfigRepository.findByMerchant_Id(merchantId).stream()
                .map(config -> webhookConfigMapper.toResponse(config, null))
                .toList();
    }

    @Override
    public WebhookConfigResponse getById(UUID merchantId, UUID configId) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        return webhookConfigMapper.toResponse(config, null);
    }

    @Override
    @Transactional
    public WebhookConfigResponse update(UUID merchantId, UUID configId, UpdateWebhookConfigRequest request) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        config.setTargetUrl(request.targetUrl());
        config.setEventTypes(request.eventTypes());
        log.info("Merchant webhook config updated id={} merchantId={}", configId, merchantId);
        return webhookConfigMapper.toResponse(config, null);
    }

    @Override
    @Transactional
    public void delete(UUID merchantId, UUID configId) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        merchantWebhookConfigRepository.delete(config);
        log.info("Merchant webhook config deleted id={} merchantId={}", configId, merchantId);
    }

    private MerchantWebhookConfig requireOwnedConfig(UUID merchantId, UUID configId) {
        return merchantWebhookConfigRepository.findByIdAndMerchant_Id(configId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantWebhookConfig", configId));
    }
//
    @Override
    public List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId, String eventType) {
        return merchantWebhookConfigRepository.findByMerchant_IdAndEnabledTrue(merchantId).stream()
                .filter(config -> config.isSubscribedTo(eventType))
                .map(config -> {
                    byte[] cipherBytes = Base64.getDecoder().decode(config.getWebhookSecret());
                    byte[] decryptedSecretBytes = bytesEncryptor.decrypt(cipherBytes);
//                    String toStore=Base64.getEncoder().enc
                    return new WebhookTarget(config.getId(), config.getTargetUrl(),
                            new String(decryptedSecretBytes, StandardCharsets.UTF_8));
                })
                .toList();
    }
}

















