package com.fourj.kltn_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_preferences")
@IdClass(UserPreferenceId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Id
    @Column(name = "pref_key", nullable = false, length = 255)
    private String prefKey;

    @Column(name = "pref_value", length = 255)
    private String prefValue;
}

