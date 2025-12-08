package com.fourj.kltn_be.controller;

import com.fourj.kltn_be.dto.AddToCartRequest;
import com.fourj.kltn_be.dto.UpdateQuantityRequest;
import com.fourj.kltn_be.dto.CartDTO;
import com.fourj.kltn_be.dto.CartItemDTO;
import com.fourj.kltn_be.service.CartService;
import com.fourj.kltn_be.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartDTO getActiveCart() {
        Long userId = SecurityUtil.getCurrentUserId();
        return cartService.getOrCreateActiveCart(userId);
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemDTO> addToCart(@RequestBody AddToCartRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        CartItemDTO item = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PatchMapping("/items/{itemId}")
    public CartItemDTO updateCartItemQuantity(
            @PathVariable Long itemId,
            @RequestBody UpdateQuantityRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        return cartService.updateCartItemQuantity(userId, itemId, request.getQuantity());
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromCart(@PathVariable Long itemId) {
        Long userId = SecurityUtil.getCurrentUserId();
        cartService.removeFromCart(userId, itemId);
    }

    @DeleteMapping("/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart() {
        Long userId = SecurityUtil.getCurrentUserId();
        cartService.clearCart(userId);
    }
}
