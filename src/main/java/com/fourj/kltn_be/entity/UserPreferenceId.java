package com.fourj.kltn_be.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceId implements Serializable {
    private Long userId;  // Must match the field name in UserPreference entity
    private String prefKey;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPreferenceId that = (UserPreferenceId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(prefKey, that.prefKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, prefKey);
    }
}

