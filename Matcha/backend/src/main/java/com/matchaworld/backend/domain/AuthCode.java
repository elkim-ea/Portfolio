package com.matchaworld.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "AUTH_CODE")
public class AuthCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUTH_ID")
    private Long id;

    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;

    @Column(name = "AUTH_CODE", nullable = false, length = 6)
    private String authCode;

    @Column(name = "EXPIRY_TIME", nullable = false)
    private LocalDateTime expiryTime;

    @Builder.Default
    @Column(name = "IS_VERIFIED", nullable = false)
    private Boolean isVerified = false;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}