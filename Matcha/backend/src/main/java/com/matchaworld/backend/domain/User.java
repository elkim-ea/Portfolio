package com.matchaworld.backend.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "USER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(name = "NICKNAME", nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "CHARACTER", columnDefinition = "TEXT", nullable = false)
    private String character;

    @Builder.Default
    @Column(name = "ESG_SCORE", nullable = false)
    private Integer esgScore = 0;

    @Builder.Default
    @Column(name = "E_SCORE", nullable = false)
    private Integer eScore = 0;

    @Builder.Default
    @Column(name = "S_SCORE", nullable = false)
    private Integer sScore = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", columnDefinition = "ENUM('USER', 'ADMIN')", nullable = false)
    private Role role = Role.USER;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    // Enum 정의
    public enum Role {
        USER,
        ADMIN
    }
}
