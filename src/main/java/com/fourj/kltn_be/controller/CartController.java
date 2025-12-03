package com.fourj.kltn_be.controller;

import com.fourj.kltn_be.dto.AddToCartRequest;
import com.fourj.kltn_be.dto.CartDTO;
import com.fourj.kltn_be.dto.CartItemDTO;
import com.fourj.kltn_be.dto.CartItemsResponseDTO;
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
    public ResponseEntity<CartDTO> getActiveCart() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            return ResponseEntity.ok(cartService.getOrCreateActiveCart(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemDTO> addToCart(@RequestBody AddToCartRequest request) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            return ResponseEntity.ok(cartService.addToCart(userId, request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{cartId}/items")
    public ResponseEntity<CartItemsResponseDTO> getCartItems(@PathVariable Long cartId) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            cartService.validateCartOwnership(cartId, userId);
            return ResponseEntity.ok(cartService.getCartItemsWithProducts(cartId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PatchMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartItemDTO> updateCartItemQuantity(@PathVariable Long cartId, @PathVariable Long itemId, @RequestParam(required = false) Integer quantity) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            cartService.validateCartOwnership(cartId, userId);
            CartItemDTO updatedItem = cartService.updateCartItemQuantity(cartId, itemId, quantity);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartDTO> removeFromCart(@PathVariable Long cartId, @PathVariable Long itemId) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            cartService.validateCartOwnership(cartId, userId);
            CartDTO updatedCart = cartService.removeFromCart(cartId, itemId);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartDTO> clearCart(@PathVariable Long cartId) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            cartService.validateCartOwnership(cartId, userId);
            CartDTO clearedCart = cartService.clearCart(cartId);
            return ResponseEntity.ok(clearedCart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

