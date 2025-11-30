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

            if (!"PAYOS".equalsIgnoreCase(order.getPaymentMethod())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Payment method is not PayOS"));
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

    @GetMapping("/order/{orderId}/status")
    public ResponseEntity<?> getOrderPaymentStatus(@PathVariable Long orderId) {
        try {
            OrderDTO order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (order.getPaymentLinkId() == null) {
                return ResponseEntity.ok(Map.of(
                        "status", "NOT_INITIATED",
                        "message", "Payment link not created"
                ));
            }

            if ("PAID".equals(order.getPaymentStatus()) || "COMPLETED".equals(order.getStatus())) {
                return ResponseEntity.ok(Map.of(
                        "status", "PAID",
                        "orderStatus", order.getStatus(),
                        "message", "Payment completed"
                ));
            }

            if (order.getPaymentCode() != null) {
                try {
                    Long orderCode = Long.parseLong(order.getPaymentCode());
                    PaymentStatusResponse response = payOSService.getPaymentStatus(orderCode);
                    if (response.getCode() == 0 && response.getData() != null) {
                        String paymentStatus = "00".equals(response.getData().getCode()) ? "PAID" : "PENDING";
                        orderService.updatePaymentStatus(orderId, paymentStatus);
                        return ResponseEntity.ok(Map.of(
                                "status", paymentStatus,
                                "orderStatus", order.getStatus(),
                                "amount", response.getData().getAmount()
                        ));
                    }
                } catch (NumberFormatException e) {
                }
            }

            return ResponseEntity.ok(Map.of(
                    "status", order.getPaymentStatus() != null ? order.getPaymentStatus() : "PENDING",
                    "orderStatus", order.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

