package com.fourj.kltn_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_message_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_message_id", nullable = false)
    private Long chatMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id", insertable = false, updatable = false)
    private ChatMessage chatMessage;

    @Column(name = "product_id", length = 255)
    private String productId;
}

