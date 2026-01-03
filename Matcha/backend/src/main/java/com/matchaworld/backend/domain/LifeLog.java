package com.matchaworld.backend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "LIFE_LOG")
public class LifeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOG_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "CONTENT", nullable = false, length = 255)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORY", length = 2, nullable = false)
    private Category category;

    @Column(name = "LOGGED_AT", nullable = false)
    private LocalDateTime loggedAt;

    @Column(name = "ESG_SCORE_EFFECT", precision = 3, scale = 2, nullable = false)
    private BigDecimal esgScoreEffect;

    // ✅ 안전 장치
    // 어떤 서비스에서 잘못 set 하더라도 DB 반영 전에 무조건 1.00으로 고정됨
    @PrePersist
    @PreUpdate
    public void ensureFixedScore() {
        if (this.esgScoreEffect == null || this.esgScoreEffect.compareTo(BigDecimal.ONE) != 0) {
            this.esgScoreEffect = BigDecimal.ONE;
        }
    }

    // Enum 정의
    public enum Category {
        E,
        S
    }
}
