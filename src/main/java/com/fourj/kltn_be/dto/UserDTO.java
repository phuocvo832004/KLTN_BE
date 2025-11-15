package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String username;
    private String preferences;
    private LocalDateTime createdAt;
    private String email;
    private OffsetDateTime updatedAt;
}

