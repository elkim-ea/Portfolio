package com.matchaworld.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.matchaworld.backend.domain.Quest;
import com.matchaworld.backend.domain.Quest.Type;
import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.domain.UserQuest;

public interface UserQuestRepository extends JpaRepository<UserQuest, Long> {

        // 유저 ID로 모든 퀘스트 조회
        List<UserQuest> findByUser_Id(Long userId);

        // 유저 ID와 퀘스트 ID로 단일 조회
        @Query("SELECT uq FROM UserQuest uq WHERE uq.user.id = :userId AND uq.quest.id = :questId")
        Optional<UserQuest> findByUserIdAndQuestId(
                @Param("userId") Long userId,
                @Param("questId") Long questId
        );

        // 유저 ID와 퀘스트 타입으로 조회
        @Query("SELECT uq FROM UserQuest uq WHERE uq.user.id = :userId AND uq.quest.type = :type")
        List<UserQuest> findByUserIdAndQuestType(
                @Param("userId") Long userId,
                @Param("type") Type type
        );

        // 모든 퀘스트 조회 (유저 ID 기준)
        List<UserQuest> findAllByUserId(Long userId);

        // 퀘스트 타입으로 전체 조회
        @Query("SELECT uq FROM UserQuest uq WHERE uq.quest.type = :type")
        List<UserQuest> findByQuestType(@Param("type") Quest.Type type);

        // 특정 유저가 특정 퀘스트를 이미 가지고 있는지 확인
        boolean existsByUserIdAndQuestId(Long userId, Long questId);

        // 특정 사용자의 퀘스트 중 지정된 퀘스트 제목과 상태(SUCCESS 등)에 해당하는 개수를 반환
        long countByUserAndQuest_TitleAndStatus(User user, String title, UserQuest.Status status);

}
