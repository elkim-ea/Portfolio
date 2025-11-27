package com.matchaworld.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.matchaworld.backend.domain.Quest;
import com.matchaworld.backend.domain.Quest.Type;
import com.matchaworld.backend.dto.response.quest.QuestResponse;

public interface QuestRepository
        extends JpaRepository<Quest, Long>, JpaSpecificationExecutor<Quest> {

    // Enum 기반 기본 조회
    Optional<Quest> findFirstByType(Type type);

    // 타입별 전체 조회
    List<Quest> findAllByType(Type type);

    // 활성화 여부 포함 조회 (랜덤 지급용)
    List<Quest> findByTypeAndIsActive(Type type, boolean isActive);

    // 유저에게 부여된 퀘스트 전체 조회 (진행중 + 완료 포함)
    @Query("""
    SELECT new com.matchaworld.backend.dto.response.quest.QuestResponse(
        q.id,
        q.adminId,
        q.title,
        q.description,
        q.rewardScore,
        q.type,
        q.authType,
        q.category,
        q.isActive,
        q.maxAttempts,
        q.conditionJson,
        q.createdAt,
        uq.status
    )
    FROM Quest q
    JOIN UserQuest uq
      ON uq.quest.id = q.id
     AND uq.user.id = :userId
    WHERE q.type = :type
      AND uq.status IN ('PENDING', 'SUCCESS')
    """)
    List<QuestResponse> findVisibleQuestsByType(
            @Param("userId") Long userId,
            @Param("type") Type type
    );

    // 오늘의 퀘스트 (일일 단위)
    @Query("""
    SELECT new com.matchaworld.backend.dto.response.quest.QuestResponse(
        q.id,
        q.adminId,
        q.title,
        q.description,
        q.rewardScore,
        q.type,
        q.authType,
        q.category,
        q.isActive,
        q.maxAttempts,
        q.conditionJson,
        q.createdAt,
        uq.status
    )
    FROM Quest q
    JOIN UserQuest uq
      ON uq.quest.id = q.id
     AND uq.user.id = :userId
    WHERE q.type = 'DAILY'
      AND uq.status = 'PENDING'
    """)
    List<QuestResponse> findActiveDailyQuests(@Param("userId") Long userId);

    // 주간 퀘스트 조회
    default List<QuestResponse> findVisibleWeeklyQuests(Long userId) {
        return findVisibleQuestsByType(userId, Type.WEEKLY);
    }

    // 시즌 퀘스트 조회
    default List<QuestResponse> findVisibleSeasonQuests(Long userId) {
        return findVisibleQuestsByType(userId, Type.SEASON);
    }

    // 활성화된 퀘스트 수 확인
    long countByIsActive(boolean isActive);

    List<Quest> findByIsActive(Boolean isActive);
}
