package com.fourj.kltn_be.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourj.kltn_be.config.PayOSConfig;
import com.fourj.kltn_be.dto.PaymentLinkRequest;
import com.fourj.kltn_be.dto.PaymentLinkResponse;
import com.fourj.kltn_be.dto.PaymentStatusResponse;
import com.fourj.kltn_be.dto.PaymentWebhookPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
public class PayOSService {
    private final WebClient webClient;
    private final String checksumKey;
    private final ObjectMapper objectMapper;

    public PayOSService(PayOSConfig payOSConfig) {
        this.checksumKey = payOSConfig.getChecksumKey();
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl(payOSConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-client-id", payOSConfig.getClientId())
                .defaultHeader("x-api-key", payOSConfig.getApiKey())
                .build();
    }

    public PaymentLinkResponse createPaymentLink(PaymentLinkRequest request) {
        try {
            Map<String, Object> payload = new TreeMap<>();
            payload.put("orderCode", request.getOrderId().intValue());
            payload.put("amount", request.getAmount().intValue());
            payload.put("description", request.getDescription());
            
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                List<Map<String, Object>> items = new ArrayList<>();
                for (PaymentLinkRequest.PaymentItem item : request.getItems()) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("name", item.getName());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("price", item.getPrice().intValue());
                    items.add(itemMap);
                }
                payload.put("items", items);
            }
            
            payload.put("returnUrl", request.getReturnUrl());
            payload.put("cancelUrl", request.getCancelUrl());

            String signature = generateSignature(payload);
            payload.put("signature", signature);

            PaymentLinkResponse response = webClient.post()
                    .uri("/v2/payment-requests")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(PaymentLinkResponse.class)
                    .block();

            if (response != null && response.getCode() == 0) {
                return response;
            } else {
                String errorMsg = response != null ? response.getDesc() : "Unknown error";
                log.error("Failed to create payment link: {}", errorMsg);
                throw new RuntimeException("Failed to create payment link: " + errorMsg);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating payment link", e);
            throw new RuntimeException("Failed to create payment link: " + e.getMessage(), e);
        }
    }

    public PaymentStatusResponse getPaymentStatus(Long orderCode) {
        try {
            PaymentStatusResponse response = webClient.get()
                    .uri("/v2/payment-requests/{orderCode}", orderCode)
                    .retrieve()
                    .bodyToMono(PaymentStatusResponse.class)
                    .block();

            if (response != null && response.getCode() == 0) {
                return response;
            } else {
                String errorMsg = response != null ? response.getDesc() : "Unknown error";
                log.error("Failed to get payment status: {}", errorMsg);
                throw new RuntimeException("Failed to get payment status: " + errorMsg);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting payment status", e);
            throw new RuntimeException("Failed to get payment status: " + e.getMessage(), e);
        }
    }

    public boolean verifyWebhookSignature(PaymentWebhookPayload payload) {
        try {
            if (payload == null || payload.getData() == null || payload.getSignature() == null) {
                return false;
            }

            Map<String, Object> dataMap = buildWebhookDataMap(payload.getData());
            String expectedSignature = generateSignature(dataMap);
            return expectedSignature.equals(payload.getSignature());
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    private Map<String, Object> buildWebhookDataMap(PaymentWebhookPayload.PaymentWebhookData data) {
        Map<String, Object> dataMap = new TreeMap<>();
        if (data.getOrderCode() != null) dataMap.put("orderCode", data.getOrderCode());
        if (data.getAmount() != null) dataMap.put("amount", data.getAmount().intValue());
        if (data.getDescription() != null) dataMap.put("description", data.getDescription());
        if (data.getAccountNumber() != null) dataMap.put("accountNumber", data.getAccountNumber());
        if (data.getReference() != null) dataMap.put("reference", data.getReference());
        if (data.getTransactionDateTime() != null) dataMap.put("transactionDateTime", data.getTransactionDateTime());
        if (data.getCurrency() != null) dataMap.put("currency", data.getCurrency());
        if (data.getPaymentLinkId() != null) dataMap.put("paymentLinkId", data.getPaymentLinkId());
        if (data.getCode() != null) dataMap.put("code", data.getCode());
        if (data.getDesc() != null) dataMap.put("desc", data.getDesc());
        if (data.getCounterAccountBankId() != null) dataMap.put("counterAccountBankId", data.getCounterAccountBankId());
        if (data.getCounterAccountBankName() != null) dataMap.put("counterAccountBankName", data.getCounterAccountBankName());
        if (data.getCounterAccountName() != null) dataMap.put("counterAccountName", data.getCounterAccountName());
        if (data.getCounterAccountNumber() != null) dataMap.put("counterAccountNumber", data.getCounterAccountNumber());
        if (data.getVirtualAccountName() != null) dataMap.put("virtualAccountName", data.getVirtualAccountName());
        if (data.getVirtualAccountNumber() != null) dataMap.put("virtualAccountNumber", data.getVirtualAccountNumber());
        return dataMap;
    }

    private String generateSignature(Map<String, Object> data) {
        try {
            String jsonString = objectMapper.writeValueAsString(data);
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    checksumKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(jsonString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Error generating signature", e);
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
}
