package com.matchaworld.backend.mapper;

import com.matchaworld.backend.domain.Quest;
import com.matchaworld.backend.domain.UserQuest;
import com.matchaworld.backend.dto.response.quest.QuestResponse;

public class QuestMapper {

    // Quest 단독 매핑 (UserQuest 없이)
    public static QuestResponse toResponse(Quest quest) {
        if (quest == null) {
            return null;
        }

        return QuestResponse.builder()
                .questId(quest.getId())
                .adminId(quest.getAdminId())
                .title(quest.getTitle())
                .description(quest.getDescription())
                .rewardScore(quest.getRewardScore())
                .type(quest.getType())
                .authType(quest.getAuthType())
                .category(quest.getCategory())
                .isActive(quest.getIsActive())
                .maxAttempts(quest.getMaxAttempts())
                .conditionJson(quest.getConditionJson())
                .createdAt(quest.getCreatedAt())
                .status(null)              // 단독 퀘스트에는 상태 없음
                .attemptCount(0)           // 누적 횟수는 기본 0
                .build();
    }

    // UserQuest 포함 매핑 (상태 + 진행 횟수)
    public static QuestResponse toResponse(UserQuest uq) {
        if (uq == null || uq.getQuest() == null) {
            return null;
        }

        Quest quest = uq.getQuest();

        return QuestResponse.builder()
                .userQuestId(uq.getId())  // ✅ UserQuest PK 설정
                .questId(quest.getId())
                .adminId(quest.getAdminId())
                .title(quest.getTitle())
                .description(quest.getDescription())
                .rewardScore(quest.getRewardScore())
                .type(quest.getType())
                .authType(quest.getAuthType())
                .category(quest.getCategory())
                .isActive(quest.getIsActive())
                .maxAttempts(quest.getMaxAttempts())
                .conditionJson(quest.getConditionJson())
                .createdAt(quest.getCreatedAt())
                .status(uq.getStatus())
                .attemptCount(uq.getAttemptCount())  // ✅ 핵심 추가
                .build();
    }
}
