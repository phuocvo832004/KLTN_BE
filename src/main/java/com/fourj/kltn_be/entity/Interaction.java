package com.fourj.kltn_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interaction_id")
    private Integer interactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "query", nullable = false, columnDefinition = "text")
    private String query;

    @Column(name = "response", columnDefinition = "text")
    private String response;

    @Column(name = "recommended_ids", columnDefinition = "_varchar")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] recommendedIds;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Note: 'id' column exists in DB but is not the primary key
    // JPA will not manage this field since it's not @Id
    @Column(name = "id", insertable = false, updatable = false)
    private Long id;

    @OneToMany(mappedBy = "interaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InteractionRecommendation> interactionRecommendations = new ArrayList<>();
}
