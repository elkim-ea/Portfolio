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
@Table(name = "USER_QUEST", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"USER_ID", "QUEST_ID"})
})
public class UserQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UQ_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QUEST_ID", nullable = false)
    private Quest quest;

    // @Builder.Default
    // @Enumerated(EnumType.STRING)
    // @Column(name = "STATUS", columnDefinition = "ENUM('PENDING', 'SUCCESS', 'FAILED')", nullable = false)
    // private Status status = Status.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false) // columnDefinition을 제거하세요!
    private Status status = Status.PENDING;

    @Builder.Default
    @Column(name = "ATTEMPT_COUNT", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "STARTED_AT")
    private LocalDateTime startedAt;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

    // Enum 정의
    public enum Status {
        PENDING,
        SUCCESS,
        FAILED
    }
}
