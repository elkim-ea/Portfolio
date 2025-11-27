package com.matchaworld.backend.service.quest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.matchaworld.backend.dto.response.ai.AiResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestMatcherFacade {

    private final QuestMatcherService questMatcherService;

    /**
     * ✅ 별도의 트랜잭션으로 퀘스트 매칭을 실행한다.
     * - LifeLogService와 분리된 커넥션으로 실행되므로 락 충돌 방지
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(Long userId, AiResult result) {
        log.info("🚀 [Facade] REQUIRES_NEW 트랜잭션 시작 (userId={})", userId);
        questMatcherService.matchAndComplete(userId, result);
        log.info("✅ [Facade] 퀘스트 매칭 완료 (userId={})", userId);
    }
}