package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long userId;
    private String message;
    private String response;
    private String productIds;
    private LocalDateTime createdAt;
    private String intent;
    private String context;
    private List<String> productIdList;
}

