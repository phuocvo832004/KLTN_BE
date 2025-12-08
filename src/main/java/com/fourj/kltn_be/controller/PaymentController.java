package com.fourj.kltn_be.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourj.kltn_be.dto.*;
import com.fourj.kltn_be.service.OrderService;
import com.fourj.kltn_be.service.PayOSService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {
    private final PayOSService payOSService;
    private final OrderService orderService;

    @PostMapping("/create-link")
    public ResponseEntity<?> createPaymentLink(@RequestBody PaymentLinkRequest request) {
        try {
            // Log request của client
            System.out.println("👉 Incoming createPaymentLink request: " + request);

            // Lấy order
            OrderDTO order = orderService.getOrderById(Long.valueOf(request.getOrderCode()))
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Check phương thức thanh toán
            if (!"PAYOS".equalsIgnoreCase(order.getPaymentMethod())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Payment method is not PayOS",
                        "orderCode", order.getId()
                ));
            }

            // Build payload tối thiểu gửi sang PayOS
            PaymentLinkRequest payOSRequest = new PaymentLinkRequest(
                    order.getId(),
                    order.getTotalAmount().longValue(),
                    "Thanh toán đơn hàng #" + order.getId(),
                    request.getReturnUrl(),
                    request.getCancelUrl()
            );

            // Log JSON payload thật sự dùng để ký
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String json = mapper.writeValueAsString(payOSRequest);
            System.out.println("📦 PayOS JSON payload (before signature): " + json);

            // Gửi sang PayOS
            PaymentLinkResponse response = payOSService.createPaymentLink(payOSRequest);

            // Log response
            System.out.println("📥 PayOS response: " + response);

            if (response.getCode() == 0 && response.getData() != null) {
                orderService.updatePaymentInfo(
                        order.getId(),
                        response.getData().getPaymentLinkId(),
                        response.getData().getStatus(),
                        response.getData().getOrderCode().toString()
                );

                return ResponseEntity.ok(Map.of(
                        "checkoutUrl", response.getData().getCheckoutUrl(),
                        "paymentLinkId", response.getData().getPaymentLinkId(),
                        "orderCode", response.getData().getOrderCode(),
                        "qrCode", response.getData().getQrCode()
                ));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error",
                            response.getDesc() != null ? response.getDesc() :
                                    "Failed to create payment link"));

        } catch (Exception e) {
            System.out.println("🔥 Error creating payment link: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody PaymentWebhookPayload payload) {
        try {
            if (!payOSService.verifyWebhookSignature(payload)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid signature"));
            }

            if ("00".equals(payload.getData().getCode())) {
                Long orderCode = Long.parseLong(payload.getData().getOrderCode());
                orderService.completePayment(orderCode, payload.getData().getReference());
                return ResponseEntity.ok(Map.of("code", "00", "desc", "Success"));
            } else {
                return ResponseEntity.ok(Map.of("code", payload.getData().getCode(), "desc", payload.getData().getDesc()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status/{orderCode}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long orderCode) {
        try {
            PaymentStatusResponse response = payOSService.getPaymentStatus(orderCode);
            if (response.getCode() == 0 && response.getData() != null) {
                return ResponseEntity.ok(Map.of(
                        "orderCode", response.getData().getOrderCode(),
                        "status", response.getData().getCode(),
                        "amount", response.getData().getAmount(),
                        "description", response.getData().getDescription()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", response.getDesc() != null ? response.getDesc() : "Failed to get payment status"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

