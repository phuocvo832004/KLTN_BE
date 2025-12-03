package com.fourj.kltn_be.controller;

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
            OrderDTO order = orderService.getOrderById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            String paymentMethod = order.getPaymentMethod();
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Payment method is not set for this order");
                errorResponse.put("orderId", order.getId());
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (!"PAYOS".equalsIgnoreCase(paymentMethod.trim())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Payment method is not PayOS");
                errorResponse.put("currentPaymentMethod", paymentMethod);
                errorResponse.put("orderId", order.getId());
                return ResponseEntity.badRequest().body(errorResponse);
            }

            PaymentLinkRequest payOSRequest = new PaymentLinkRequest(
                    order.getId(),
                    "Thanh toán đơn hàng #" + order.getId(),
                    order.getTotalAmount(),
                    order.getItems().stream()
                            .map(orderItem -> new PaymentLinkRequest.PaymentItem(
                                    orderItem.getProduct() != null ? orderItem.getProduct().getTitle() : "Product",
                                    orderItem.getQuantity(),
                                    orderItem.getUnitPrice()
                            ))
                            .collect(Collectors.toList()),
                    request.getReturnUrl(),
                    request.getCancelUrl()
            );

            PaymentLinkResponse response = payOSService.createPaymentLink(payOSRequest);

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
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", response.getDesc() != null ? response.getDesc() : "Failed to create payment link"));
            }
        } catch (Exception e) {
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

