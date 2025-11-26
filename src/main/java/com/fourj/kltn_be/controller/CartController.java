package com.fourj.kltn_be.controller;

import com.fourj.kltn_be.dto.AddToCartRequest;
import com.fourj.kltn_be.dto.CartDTO;
import com.fourj.kltn_be.dto.CartItemDTO;
import com.fourj.kltn_be.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartController {
    private final CartService cartService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<CartDTO> getActiveCart(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(cartService.getOrCreateActiveCart(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/user/{userId}/items")
    public ResponseEntity<CartItemDTO> addToCart(@PathVariable Long userId, @RequestBody AddToCartRequest request) {
        try {
            return ResponseEntity.ok(cartService.addToCart(userId, request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{cartId}/items")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.getCartItems(cartId));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartItemDTO> updateCartItemQuantity(@PathVariable Long itemId, @RequestParam Integer quantity) {
        try {
            CartItemDTO updatedItem = cartService.updateCartItemQuantity(itemId, quantity);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeFromCart(@PathVariable Long itemId) {
        try {
            CartDTO updatedCart = cartService.removeFromCart(null, itemId);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartDTO> clearCart(@PathVariable Long cartId) {
        try {
            CartDTO clearedCart = cartService.clearCart(cartId);
            return ResponseEntity.ok(clearedCart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{cartId}/checkout")
    public ResponseEntity<CartDTO> checkoutCart(@PathVariable Long cartId) {
        try {
            CartDTO checkedOutCart = cartService.checkoutCart(cartId);
            return ResponseEntity.ok(checkedOutCart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

