package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemsResponseDTO {
    private List<CartProductDTO> products;
    
    public static CartItemsResponseDTO of(List<CartProductDTO> products) {
        CartItemsResponseDTO response = new CartItemsResponseDTO();
        response.setProducts(products);
        return response;
    }
}

