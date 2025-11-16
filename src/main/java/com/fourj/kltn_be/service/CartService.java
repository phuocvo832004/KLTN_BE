package com.fourj.kltn_be.service;

import com.fourj.kltn_be.dto.AddToCartRequest;
import com.fourj.kltn_be.dto.CartDTO;
import com.fourj.kltn_be.dto.CartItemDTO;
import com.fourj.kltn_be.entity.Cart;
import com.fourj.kltn_be.entity.CartItem;
import com.fourj.kltn_be.entity.Product;
import com.fourj.kltn_be.entity.User;
import com.fourj.kltn_be.repository.CartItemRepository;
import com.fourj.kltn_be.repository.CartRepository;
import com.fourj.kltn_be.repository.ProductRepository;
import com.fourj.kltn_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Transactional
    public CartDTO getOrCreateActiveCart(Long userId) {
        Optional<Cart> existingCart = cartRepository.findByUserIdAndStatusWithItems(userId, "ACTIVE");
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            // Ensure cart items are initialized
            cart.getCartItems().size();
            return convertToDTO(cart);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setStatus("ACTIVE");
        Cart saved = cartRepository.save(cart);
        return convertToDTO(saved);
    }

    @Transactional
    public CartItemDTO addToCart(Long userId, AddToCartRequest request) {
        CartDTO cartDTO = getOrCreateActiveCart(userId);
        Cart cart = cartRepository.findByIdWithItems(cartDTO.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId());
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUnitPrice(request.getUnitPrice());
        }
        
        CartItem saved = cartItemRepository.save(cartItem);
        return convertItemToDTO(saved);
    }

    @Transactional
    public void removeFromCart(Long cartId, Long itemId) {
        if (!cartItemRepository.existsById(itemId)) {
            throw new RuntimeException("Cart item not found");
        }
        cartItemRepository.deleteById(itemId);
    }

    @Transactional
    public void updateCartItemQuantity(Long itemId, Integer quantity) {
        cartItemRepository.findById(itemId).ifPresent(item -> {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        });
    }

    @Transactional
    public void clearCart(Long cartId) {
        cartItemRepository.deleteByCartId(cartId);
    }

    @Transactional
    public void checkoutCart(Long cartId) {
        cartRepository.findById(cartId).ifPresent(cart -> {
            cart.setStatus("CHECKED_OUT");
            cartRepository.save(cart);
        });
    }

    public List<CartItemDTO> getCartItems(Long cartId) {
        return cartItemRepository.findByCartId(cartId).stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
    }

    private CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getUserId());
        dto.setStatus(cart.getStatus());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        // Ensure cart items are loaded and convert to DTO
        List<CartItem> items = cart.getCartItems() != null ? cart.getCartItems() : List.of();
        dto.setItems(items.stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    private CartItemDTO convertItemToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setCartId(item.getCart().getId());
        dto.setProductId(item.getProduct().getId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setProduct(productService.getProductById(item.getProduct().getId()).orElse(null));
        return dto;
    }
}

