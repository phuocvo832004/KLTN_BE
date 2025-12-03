package com.fourj.kltn_be.service;

import com.fourj.kltn_be.dto.AddToCartRequest;
import com.fourj.kltn_be.dto.CartDTO;
import com.fourj.kltn_be.dto.CartItemDTO;
import com.fourj.kltn_be.dto.CartItemsResponseDTO;
import com.fourj.kltn_be.dto.CartProductDTO;
import com.fourj.kltn_be.dto.ProductDTO;
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

import java.math.BigDecimal;
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
            cart.getCartItems().size();
            return convertToDTO(cart);
        }
        
        User user = userRepository.findByUserId(userId)
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
        
        BigDecimal unitPrice = request.getUnitPrice();
        if (unitPrice == null) {
            unitPrice = (product.getSale() != null && product.getSale().compareTo(BigDecimal.ZERO) > 0) 
                    ? product.getSale() 
                    : product.getPrice();
        }
        
        Integer quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId());
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(unitPrice);
            cart.getCartItems().add(cartItem);
        }
        
        CartItem saved = cartItemRepository.save(cartItem);
        return convertItemToDTO(saved);
    }

    @Transactional
    public CartDTO removeFromCart(Long cartId, Long itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        Long actualCartId = cartItem.getCart().getId();
        
        if (cartId != null && !cartId.equals(actualCartId)) {
            throw new RuntimeException("Cart item does not belong to the specified cart");
        }
        
        Cart cart = cartRepository.findByIdWithItems(actualCartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        if (cart.getCartItems() != null) {
            cart.getCartItems().remove(cartItem);
        }
        cartItemRepository.deleteById(itemId);
        
        Cart updatedCart = cartRepository.findByIdWithItems(actualCartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return convertToDTO(updatedCart);
    }

    @Transactional
    public CartItemDTO updateCartItemQuantity(Long cartId, Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        Long actualCartId = item.getCart().getId();
        if (cartId != null && !cartId.equals(actualCartId)) {
            throw new RuntimeException("Cart item does not belong to the specified cart");
        }
        
        if (quantity == null) {
            item.setQuantity(item.getQuantity() + 1);
        } else {
            item.setQuantity(quantity);
        }
        CartItem saved = cartItemRepository.save(item);
        return convertItemToDTO(saved);
    }

    @Transactional
    public CartDTO clearCart(Long cartId) {
        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        if (cart.getCartItems() != null) {
            cart.getCartItems().clear();
        }
        cartItemRepository.deleteByCartId(cartId);
        
        Cart clearedCart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return convertToDTO(clearedCart);
    }

    public List<CartItemDTO> getCartItems(Long cartId) {
        return cartItemRepository.findByCartId(cartId).stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
    }

    public CartItemsResponseDTO getCartItemsWithProducts(Long cartId) {
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        List<CartProductDTO> products = items.stream()
                .map(this::convertToCartProductDTO)
                .collect(Collectors.toList());
        return CartItemsResponseDTO.of(products);
    }

    public void validateCartOwnership(Long cartId, Long userId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        if (!cart.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Cart does not belong to user");
        }
    }

    private CartProductDTO convertToCartProductDTO(CartItem item) {
        ProductDTO productDTO = productService.getProductById(item.getProduct().getId()).orElse(null);
        if (productDTO != null) {
            return CartProductDTO.fromProductDTO(productDTO, item.getQuantity(), item.getUnitPrice(), item.getId());
        }
        CartProductDTO dto = new CartProductDTO();
        dto.setCartItemId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        return dto;
    }

    private CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getUserId());
        dto.setStatus(cart.getStatus());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
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


