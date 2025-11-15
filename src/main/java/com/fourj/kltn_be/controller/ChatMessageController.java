package com.fourj.kltn_be.controller;

import com.fourj.kltn_be.dto.ChatMessageDTO;
import com.fourj.kltn_be.entity.ChatMessage;
import com.fourj.kltn_be.entity.User;
import com.fourj.kltn_be.repository.ChatMessageRepository;
import com.fourj.kltn_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat-messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatMessageController {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatMessageDTO>> getUserChatMessages(@PathVariable Long userId) {
        List<ChatMessage> messages = chatMessageRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<ChatMessageDTO> dtos = messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<ChatMessageDTO> createChatMessage(@RequestBody ChatMessageDTO chatMessageDTO) {
        ChatMessage message = new ChatMessage();
        if (chatMessageDTO.getUserId() != null) {
            User user = userRepository.findByUserId(chatMessageDTO.getUserId())
                    .orElse(null);
            message.setUser(user);
        }
        message.setMessage(chatMessageDTO.getMessage());
        message.setResponse(chatMessageDTO.getResponse());
        message.setProductIds(chatMessageDTO.getProductIds());
        message.setIntent(chatMessageDTO.getIntent());
        message.setContext(chatMessageDTO.getContext());
        
        ChatMessage saved = chatMessageRepository.save(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    private ChatMessageDTO convertToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setUserId(message.getUser() != null ? message.getUser().getUserId() : null);
        dto.setMessage(message.getMessage());
        dto.setResponse(message.getResponse());
        dto.setProductIds(message.getProductIds());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setIntent(message.getIntent());
        dto.setContext(message.getContext());
        return dto;
    }
}

