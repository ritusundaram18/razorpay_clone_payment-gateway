package com.codingshuttle.razorpay.vault.service.impl;

import com.codingshuttle.razorpay.common.entity.Money;
import com.codingshuttle.razorpay.common.enums.CardBrand;
import com.codingshuttle.razorpay.common.exception.ResourceNotFoundException;
import com.codingshuttle.razorpay.common.utl.RandomizerUtil;
import com.codingshuttle.razorpay.processor.dto.PaymentProcessorRequest;
import com.codingshuttle.razorpay.processor.dto.PaymentProcessorResponse;
import com.codingshuttle.razorpay.processor.strategy.PaymentProcessorRouter;
import com.codingshuttle.razorpay.vault.config.VaultEncryptionConfig;
import com.codingshuttle.razorpay.vault.dto.request.TokenizeRequest;
import com.codingshuttle.razorpay.vault.dto.response.TokenizeResponse;
import com.codingshuttle.razorpay.vault.entity.CardToken;
import com.codingshuttle.razorpay.vault.entity.VaultCard;
import com.codingshuttle.razorpay.vault.repository.CardTokenRepository;
import com.codingshuttle.razorpay.vault.repository.VaultCardRepository;
import com.codingshuttle.razorpay.vault.service.VaultService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.keygen.KeyGenerators;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {

    private final CardTokenRepository cardTokenRepository;
    private final VaultCardRepository vaultCardRepository;
    private final BytesEncryptor dekEncrypter;
    private final PaymentProcessorRouter paymentProcessorRouter;
    @Override
    @Transactional
    public TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId) {

        String lastFour=request.pan().substring(request.pan().length()-4);
        String bin=request.pan().substring(0,6);
        CardBrand cardBrand=detectBrand(request.pan());

        byte[] dek=KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan= VaultEncryptionConfig.panEncrypter(dek).encrypt(request.pan().getBytes(
                StandardCharsets.UTF_8
        ));
        byte[] encryptedDek=dekEncrypter.encrypt(dek);

        VaultCard vaultCard = vaultCardRepository.save(VaultCard.builder()
                .brand(cardBrand)
                .expiryYear(request.expiryYear().toString())
                .expiryMonth(request.expiryMonth().toString())
                .bin(bin)
                .lastFour(lastFour)
                .encryptedDek(encryptedDek)
                .encryptedPan(encryptedPan)
                .cardholderName(request.cardHolderName())
                .build());
        String token="tok"+ RandomizerUtil.randomBase64(32);
        cardTokenRepository.save(CardToken.builder()
                .vaultCard(vaultCard)
                .token(token)
                .customer(request.customerId())
                .merchant(merchantId)
                .build());

        return new TokenizeResponse(token,lastFour,cardBrand,request.expiryMonth(),request.expiryYear());
    }

    @Override
    @Transactional
    public PaymentProcessorResponse charge(UUID paymentId,String token, Money amount, Map<String, Object> methodDetails) {
       CardToken cardToken=cardTokenRepository.findByTokenAndRevokedAtIsNull(token).orElseThrow(() -> new ResourceNotFoundException("CardToken",token));

       VaultCard vaultCard=cardToken.getVaultCard();
       byte[] panBytes=null;
       try {
           byte[] dek = dekEncrypter.decrypt(vaultCard.getEncryptedDek());
           panBytes = VaultEncryptionConfig.panEncrypter(dek).decrypt(vaultCard.getEncryptedPan());

           String pan = new String(panBytes, StandardCharsets.UTF_8);
           String expiry = vaultCard.getExpiryMonth() + "/" + vaultCard.getExpiryYear();

           PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.card
                   (paymentId, pan, expiry, amount, methodDetails);

           PaymentProcessorResponse response = paymentProcessorRouter.charge(paymentProcessorRequest);

           log.info("Vault charge registered, token={}***", token.substring(0, 4));

           Arrays.fill(panBytes, (byte) 0);

           return response;
       }catch (Exception e){
           log.warn("Vault charge failed, token={}***, error={}", token.substring(0, 4), e.getMessage());
           return new PaymentProcessorResponse.Failure("VAULT_CHARGE_FAILED", e.getMessage());
       } finally {
           if (panBytes != null) Arrays.fill(panBytes, (byte) 0);
       }
    }


    private CardBrand detectBrand(String pan){

        if(pan.startsWith("4")){
            return CardBrand.VISA;
        }else if(pan.startsWith("5")){
            return CardBrand.MASTERCARD;
        }else if(pan.startsWith("37") || pan.startsWith("34")){
            return CardBrand.AMEX;
        }else{
            return CardBrand.RUPAY;
        }

    }
}
