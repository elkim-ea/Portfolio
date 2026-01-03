package com.matchaworld.backend.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
@Table(name = "CERTIFICATION")
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CERT_ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UQ_ID", nullable = false)
    private UserQuest userQuest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "AUTH_TYPE", columnDefinition = "ENUM('IMAGE', 'TEXT')", nullable = false)
    private AuthType authType;

    @Column(name = "AUTH_CONTENT", columnDefinition = "TEXT", nullable = false)
    private String authContent;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "VALIDATION_STATUS", columnDefinition = "ENUM('PENDING', 'SUCCESS', 'FAILED')", nullable = false)
    private ValidationStatus validationStatus = ValidationStatus.PENDING;

    @Column(name = "VALIDATED_AT")
    private LocalDateTime validatedAt;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "MODEL_TYPE", columnDefinition = "ENUM('OPENAPI', 'HUGGING')", nullable = false)
    private ModelType modelType;

    @Column(name = "CONFIDENCE_SCORE", nullable = false)
    private Double confidenceScore;

    // Enum 정의
    public enum AuthType {
        IMAGE,
        TEXT
    }

    public enum ValidationStatus {
        PENDING,
        SUCCESS,
        FAILED
    }

    public enum ModelType {
        OPENAPI,
        HUGGING
    }
}
