package com.matchaworld.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USER_TERMS", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"USER_ID", "TERM_ID"})
})
public class UserTerms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TERM_ID", nullable = false)
    private Terms terms;

    @Column(name = "IS_AGREED", nullable = false)
    private Boolean isAgreed;

    @Column(name = "AGREED_AT", nullable = false)
    private LocalDateTime agreedAt;
}