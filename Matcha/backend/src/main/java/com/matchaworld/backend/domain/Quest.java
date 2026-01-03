package com.matchaworld.backend.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@EntityListeners(AuditingEntityListener.class)
@Table(name = "QUEST")
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QUEST_ID")
    private Long id;

    @Column(name = "ADMIN_ID", nullable = true)
    private Long adminId; // USER 테이블의 USER_ID 참조

    @Column(name = "TITLE", nullable = false, length = 100)
    private String title;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "REWARD_SCORE", nullable = false)
    private Integer rewardScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", columnDefinition = "ENUM('DAILY', 'WEEKLY', 'SEASON')", nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(name = "AUTH_TYPE", columnDefinition = "ENUM('IMAGE', 'TEXT')", nullable = false)
    private AuthType authType;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORY", columnDefinition = "ENUM('E', 'S')", nullable = false)
    private Category category;

    @Builder.Default
    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "MAX_ATTEMPTS", nullable = false)
    private Integer maxAttempts = 1;

    @Column(name = "CONDITION_JSON", columnDefinition = "JSON")
    private String conditionJson; // JSON 타입

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Enum 정의
    public enum Type {
        DAILY,
        WEEKLY,
        SEASON;
    }

    public enum AuthType {
        IMAGE,
        TEXT
    }

    // ESG CATEGORY
    public enum Category {
        E,  // Environment (환경)
        S   // Social (사회)
    }
}
