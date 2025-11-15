package com.fourj.kltn_be.service;

import com.fourj.kltn_be.dto.CreateOrderRequest;
import com.fourj.kltn_be.dto.OrderDTO;
import com.fourj.kltn_be.dto.OrderItemDTO;
import com.fourj.kltn_be.entity.Order;
import com.fourj.kltn_be.entity.OrderItem;
import com.fourj.kltn_be.entity.Product;
import com.fourj.kltn_be.entity.User;
import com.fourj.kltn_be.repository.OrderItemRepository;
import com.fourj.kltn_be.repository.OrderRepository;
import com.fourj.kltn_be.repository.ProductRepository;
import com.fourj.kltn_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (var itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));
            
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            
            totalAmount = totalAmount.add(itemRequest.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            order.getOrderItems().add(item);
        }
        
        order.setTotalAmount(totalAmount);
        Order saved = orderRepository.save(order);
        return convertToDTO(saved);
    }

    public List<OrderDTO> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<OrderDTO> getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToDTO);
    }

    @Transactional
    public Optional<OrderDTO> updateOrderStatus(Long orderId, String status) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(status);
                    Order saved = orderRepository.save(order);
                    return convertToDTO(saved);
                });
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getUserId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setItems(order.getOrderItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    private OrderItemDTO convertItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setOrderId(item.getOrder().getId());
        dto.setProductId(item.getProduct().getId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setProduct(productService.getProductById(item.getProduct().getId()).orElse(null));
        return dto;
    }
}

