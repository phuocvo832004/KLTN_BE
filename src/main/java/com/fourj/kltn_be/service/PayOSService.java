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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
public class PayOSService {
    private final PayOSConfig payOSConfig;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public PayOSService(PayOSConfig payOSConfig) {
        this.payOSConfig = payOSConfig;
        this.webClient = WebClient.builder()
                .baseUrl(payOSConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-client-id", payOSConfig.getClientId())
                .defaultHeader("x-api-key", payOSConfig.getApiKey())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public PaymentLinkResponse createPaymentLink(PaymentLinkRequest request) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("orderCode", request.getOrderId().intValue());
            payload.put("amount", request.getAmount().intValue());
            payload.put("description", request.getDescription());
            payload.put("items", request.getItems().stream().map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("name", item.getName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("price", item.getPrice().intValue());
                return itemMap;
            }).toList());
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
                throw new RuntimeException("Failed to create payment link: " + (response != null ? response.getDesc() : "Unknown error"));
            }
        } catch (Exception e) {
            log.error("Error creating payment link", e);
            throw new RuntimeException("Failed to create payment link", e);
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
                throw new RuntimeException("Failed to get payment status: " + (response != null ? response.getDesc() : "Unknown error"));
            }
        } catch (Exception e) {
            log.error("Error getting payment status", e);
            throw new RuntimeException("Failed to get payment status", e);
        }
    }

    public boolean verifyWebhookSignature(PaymentWebhookPayload payload) {
        try {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("orderCode", payload.getData().getOrderCode());
            dataMap.put("amount", payload.getData().getAmount().intValue());
            dataMap.put("description", payload.getData().getDescription());
            dataMap.put("accountNumber", payload.getData().getAccountNumber());
            dataMap.put("reference", payload.getData().getReference());
            dataMap.put("transactionDateTime", payload.getData().getTransactionDateTime());
            dataMap.put("currency", payload.getData().getCurrency());
            dataMap.put("paymentLinkId", payload.getData().getPaymentLinkId());
            dataMap.put("code", payload.getData().getCode());
            dataMap.put("desc", payload.getData().getDesc());
            if (payload.getData().getCounterAccountBankId() != null) {
                dataMap.put("counterAccountBankId", payload.getData().getCounterAccountBankId());
            }
            if (payload.getData().getCounterAccountBankName() != null) {
                dataMap.put("counterAccountBankName", payload.getData().getCounterAccountBankName());
            }
            if (payload.getData().getCounterAccountName() != null) {
                dataMap.put("counterAccountName", payload.getData().getCounterAccountName());
            }
            if (payload.getData().getCounterAccountNumber() != null) {
                dataMap.put("counterAccountNumber", payload.getData().getCounterAccountNumber());
            }
            if (payload.getData().getVirtualAccountName() != null) {
                dataMap.put("virtualAccountName", payload.getData().getVirtualAccountName());
            }
            if (payload.getData().getVirtualAccountNumber() != null) {
                dataMap.put("virtualAccountNumber", payload.getData().getVirtualAccountNumber());
            }

            String expectedSignature = generateSignature(dataMap);
            return expectedSignature.equals(payload.getSignature());
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    private String generateSignature(Map<String, Object> data) {
        try {
            // Sắp xếp keys theo thứ tự alphabet để đảm bảo signature nhất quán
            Map<String, Object> sortedData = new TreeMap<>(data);
            
            String jsonString = objectMapper.writeValueAsString(sortedData);
            
            // Log để debug (có thể xóa sau)
            log.debug("JSON string for signature: {}", jsonString);
            
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    payOSConfig.getChecksumKey().getBytes(StandardCharsets.UTF_8),
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

