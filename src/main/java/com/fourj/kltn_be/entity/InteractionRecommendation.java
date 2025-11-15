package com.fourj.kltn_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interaction_recommendations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteractionRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interaction_id", nullable = false)
    private Integer interactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interaction_id", insertable = false, updatable = false)
    private Interaction interaction;

    @Column(name = "product_id", length = 255)
    private String productId;
}

