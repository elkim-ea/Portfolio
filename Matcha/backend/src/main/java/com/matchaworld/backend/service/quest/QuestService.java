package com.matchaworld.backend.service.quest;

import java.util.List;
import java.util.Map;

import com.matchaworld.backend.dto.response.quest.QuestResponse;
import com.matchaworld.backend.dto.response.quest.QuestSubmitResponse;


public interface QuestService {

    // 오늘의 퀘스트 조회
    QuestResponse getTodayQuest(Long userId);

    // 주간 퀘스트 조회
    List<QuestResponse> getWeeklyQuests(Long userId);

    // 시즌 퀘스트 조회
    List<QuestResponse> getSeasonQuests(Long userId);

    // 퀘스트 제출
    QuestSubmitResponse submitQuest(Long questId, Long userId);

    // 유저 진행 중인 퀘스트 목록
    List<QuestResponse> getUserQuests(Long userId);

    // 메인 페이지용 통합 퀘스트 조회
    Map<String, Object> getMainQuests(Long userId);

    // MAX_ATTEMPTS 도달 시 SUCCESS 처리
    void checkAndCompleteQuest(Long userQuestId);

    // 퀘스트 기간 만료 시 자동 비활성화
    void deactivateExpiredQuests();

    // 시즌 종료 시 사용자 퀘스트 초기화
    void resetSeasonUserQuests();
}
