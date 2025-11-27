package com.matchaworld.backend.dto.response.quest;

import java.time.LocalDateTime;

import com.matchaworld.backend.domain.Quest;
import com.matchaworld.backend.domain.UserQuest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QuestResponse {

    private Long userQuestId;  // ✅ UserQuest의 PK (UQ_ID)
    private Long questId;
    private Long adminId;
    private String title;
    private String description;
    private Integer rewardScore;
    private Quest.Type type;
    private Quest.AuthType authType;
    private Quest.Category category;
    private Boolean isActive;
    private Integer maxAttempts;
    private String conditionJson;
    private LocalDateTime createdAt;
    private UserQuest.Status status;

    // 진행 중인 횟수
    private Integer attemptCount; // (현재 진행된 횟수)

    // JPQL new(...) 전용 생성자
    public QuestResponse(
        Long questId,
        Long adminId,
        String title,
        String description,
        Integer rewardScore,
        Quest.Type type,
        Quest.AuthType authType,
        Quest.Category category,
        Boolean isActive,
        Integer maxAttempts,
        String conditionJson,
        LocalDateTime createdAt,
        UserQuest.Status status
    ) {
        this.questId = questId;
        this.adminId = adminId;
        this.title = title;
        this.description = description;
        this.rewardScore = rewardScore;
        this.type = type;
        this.authType = authType;
        this.category = category;
        this.isActive = isActive;
        this.maxAttempts = maxAttempts;
        this.conditionJson = conditionJson;
        this.createdAt = createdAt;
        this.status = status;
        this.attemptCount = 0; // 기본값
    }
}
