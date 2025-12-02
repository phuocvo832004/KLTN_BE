package com.fourj.kltn_be.service;

import com.fourj.kltn_be.config.PayOSConfig;
import com.fourj.kltn_be.dto.PaymentLinkRequest;
import com.fourj.kltn_be.dto.PaymentLinkResponse;
import com.fourj.kltn_be.dto.PaymentStatusResponse;
import com.fourj.kltn_be.dto.PaymentWebhookPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
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

    @Autowired
    public PayOSService(PayOSConfig payOSConfig, ObjectMapper objectMapper) {
        this.checksumKey = payOSConfig.getChecksumKey();
        this.webClient = WebClient.builder()
                .baseUrl(payOSConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-client-id", payOSConfig.getClientId())
                .defaultHeader("x-api-key", payOSConfig.getApiKey())
                .build();
        this.objectMapper = objectMapper;
    }

    public PaymentLinkResponse createPaymentLink(PaymentLinkRequest request) {
        try {
            Map<String, Object> payload = new TreeMap<>();
            payload.put("orderCode", request.getOrderId().intValue());
            payload.put("amount", request.getAmount().intValue());
            payload.put("description", request.getDescription());
            payload.put("returnUrl", request.getReturnUrl());
            payload.put("cancelUrl", request.getCancelUrl());

            List<Map<String, Object>> items = null;
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                items = new ArrayList<>();
                for (PaymentLinkRequest.PaymentItem item : request.getItems()) {
                    Map<String, Object> itemMap = new TreeMap<>();
                    itemMap.put("name", item.getName());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("price", item.getPrice().intValue());
                    items.add(itemMap);
                }
                payload.put("items", items);
            }

            Map<String, Object> signatureData = new TreeMap<>();
            signatureData.put("amount", request.getAmount().intValue());
            signatureData.put("cancelUrl", request.getCancelUrl());
            signatureData.put("description", request.getDescription());
            signatureData.put("orderCode", request.getOrderId().intValue());
            signatureData.put("returnUrl", request.getReturnUrl());

            if (items != null && !items.isEmpty()) {
                String itemsJson = objectMapper.writeValueAsString(items);
                signatureData.put("items", itemsJson);
            }

            String signature = generateSignature(signatureData);
            payload.put("signature", signature);

            log.debug("Payment link request - orderCode: {}, amount: {}, signature: {}", 
                    request.getOrderId(), request.getAmount(), signature);
            log.debug("Signature data map: {}", signatureData);
            if (items != null && !items.isEmpty()) {
                log.debug("Items JSON for signature: {}", objectMapper.writeValueAsString(items));
            }

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
            StringBuilder rawData = new StringBuilder();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (rawData.length() > 0) {
                    rawData.append("&");
                }
                String value;
                if (entry.getValue() == null) {
                    value = "";
                } else {
                    value = entry.getValue().toString();
                }
                String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
                rawData.append(entry.getKey()).append("=").append(encodedValue);
            }

            String rawDataString = rawData.toString();
            log.debug("Raw data for signature: {}", rawDataString);

            if (checksumKey == null || checksumKey.trim().isEmpty()) {
                throw new RuntimeException("Checksum key is not configured");
            }

            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    checksumKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(rawDataString.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(hash);
            
            log.debug("Generated signature: {}", signature);
            return signature;
        } catch (Exception e) {
            log.error("Error generating signature", e);
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
}