package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemsResponseDTO {
    private List<CartProductDTO> products;
    private Meta meta;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private BigDecimal total;
    }
    
    public static CartItemsResponseDTO of(List<CartProductDTO> products) {
        CartItemsResponseDTO response = new CartItemsResponseDTO();
        response.setProducts(products);
        
        BigDecimal totalPrice = products.stream()
                .map(p -> {
                    BigDecimal price = p.getUnitPrice() != null ? p.getUnitPrice() : BigDecimal.ZERO;
                    int qty = p.getQuantity() != null ? p.getQuantity() : 0;
                    return price.multiply(BigDecimal.valueOf(qty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        response.setMeta(new Meta(totalPrice));
        return response;
    }
}

