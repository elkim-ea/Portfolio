package com.matchaworld.backend.service.lifelog;

import com.matchaworld.backend.domain.LifeLog;
import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.dto.request.lifelog.LifeLogRequest;
import com.matchaworld.backend.dto.response.lifelog.LifeLogResponse;
import com.matchaworld.backend.dto.response.ai.AiResult;
import com.matchaworld.backend.repository.LifeLogRepository;
import com.matchaworld.backend.repository.UserRepository;
import com.matchaworld.backend.service.ai.AIService;
import com.matchaworld.backend.service.quest.QuestMatcherFacade;
import com.matchaworld.backend.service.quest.QuestMatcherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LifeLogService {

    private final AIService aiService;
    private final QuestMatcherService questMatcherService;
    private final QuestMatcherFacade questMatcherFacade;
    private final LifeLogRepository lifeLogRepository;
    private final UserRepository userRepository;

    /** ✅ 날짜별 기록 조회 */
    public List<LifeLogResponse> getLogs(Long userId, String date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<LifeLog> logs;
        if (date != null) {
            LocalDate target = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            LocalDateTime start = target.atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            logs = lifeLogRepository.findByUserIdAndLoggedAtBetween(userId, start, end);
        } else {
            logs = lifeLogRepository.findByUserOrderByLoggedAtDesc(user);
        }

        return logs.stream()
                .map(LifeLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 1단계: 기록 저장 (DB 트랜잭션 내부)
     * - 단순히 사용자의 입력을 저장만 함.
     * - 트랜잭션이 끝난 후 AI 분석을 별도 메서드에서 수행.
     */
    public void addLifeLogWithAiAndQuest(Long userId, com.matchaworld.backend.dto.request.lifelog.LifeLogRequest request) {
        // 1️⃣ 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("❌ 사용자 정보를 찾을 수 없습니다."));

        String content = request.getContent();

        // 2️⃣ LifeLog 생성 및 임시 저장 (카테고리는 임시로 E)
        LifeLog lifelog = LifeLog.builder()
                .user(user)
                .content(content)
                .category(LifeLog.Category.E)
                .loggedAt(LocalDateTime.now())
                .esgScoreEffect(BigDecimal.ONE)
                .build();

        lifeLogRepository.save(lifelog);
        lifeLogRepository.flush(); // <- 즉시 반영, 락 지속시간 단축
        log.info("📝 [기록 저장 완료] userId={}, content={}, 초기점수={}", userId, content, lifelog.getEsgScoreEffect());

        // 3️⃣ 트랜잭션 종료 후 AI 분석 및 퀘스트 매칭 수행
        processAiAndQuestAsync(lifelog.getId(), content, userId);
    }

    /**
     * ✅ 2단계: AI 분석 및 퀘스트 매칭
     * - 별도 트랜잭션에서 수행되어 락 충돌 방지.
     */
    // @Async
    @Transactional
    public void processAiAndQuestAsync(Long logId, String content, Long userId) {
        try {
            // 1️⃣ 기록 재조회
            LifeLog lifelog = lifeLogRepository.findById(logId)
                    .orElseThrow(() -> new RuntimeException("❌ 기록을 찾을 수 없습니다."));
            log.info("🔎 [1단계] 재조회 직후 점수 = {}", lifelog.getEsgScoreEffect());

            // 2️⃣ AI 분석 호출
            AiResult result = aiService.analyzeTextJackson(content);
            log.info("📜 [2단계] AI 분석 결과 수신 → category={}, confidence={}",
                    result.getCategory(), result.getConfidence());

            // ✅ rawText 누락 방지
            if (result.getRawText() == null || result.getRawText().isBlank()) {
                result.setRawText(content);
                log.warn("⚠️ [보정] AI rawText 누락 감지 → 원문으로 재설정됨: {}", content);
            }

            // 3️⃣ 카테고리 반영 전 점수 확인
            log.info("🔎 [3단계] AI 결과 반영 전 점수 = {}", lifelog.getEsgScoreEffect());

            // 4️⃣ AI 결과를 기록에 반영
            LifeLog.Category detectedCategory = switch (result.getCategory().toUpperCase()) {
                case "E" -> LifeLog.Category.E;
                case "S" -> LifeLog.Category.S;
                default -> LifeLog.Category.E;
            };
            lifelog.setCategory(detectedCategory);
            log.info("🔎 [4단계] 카테고리 반영 후 ({}), 점수 = {}", detectedCategory, lifelog.getEsgScoreEffect());

            // ✅ 점수는 항상 ONE으로 고정
            lifelog.setEsgScoreEffect(BigDecimal.ONE);
            log.info("✅ [5단계] 점수 고정 적용 후 점수 = {}", lifelog.getEsgScoreEffect());

            // ⚠️ confidence는 점수로 사용하지 않음
            log.info("🧠 [AI confidence 로그 전용] confidence={}", result.getConfidence());

            lifeLogRepository.save(lifelog);
            log.info("💾 [3단계] 기록 카테고리 반영 후 DB 저장 완료: {} (카테고리={}, 점수={})",
                lifelog.getId(), detectedCategory, lifelog.getEsgScoreEffect());

            // ⚡ DB 재조회
            var reloaded = lifeLogRepository.findById(lifelog.getId()).get();
            log.info("🧾 [확인] DB 반영 상태 → category={}, score={}",
                    reloaded.getCategory(), reloaded.getEsgScoreEffect());

            // 수정 후
            questMatcherFacade.execute(userId, result);
            log.info("🎯 [4단계] 퀘스트 매칭 완료 후, User 점수 상태를 확인합니다.");

            // 6️⃣ 사용자 ESG 점수 합산
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("❌ 사용자 정보를 찾을 수 없습니다."));

            User userAfterQuest = userRepository.findById(userId).get();
            log.info("💰 [확인] 퀘스트 완료 직후 User 상태 → E={}, S={}, ESG={}",
                    userAfterQuest.getEScore(), userAfterQuest.getSScore(), userAfterQuest.getEsgScore());

            var eSum = lifeLogRepository.sumScoreByCategory(userId, LifeLog.Category.E);
            var sSum = lifeLogRepository.sumScoreByCategory(userId, LifeLog.Category.S);

            int eScore = eSum != null ? eSum.intValue() : 0;
            int sScore = sSum != null ? sSum.intValue() : 0;

            // int questScore = user.getEsgScore() != null ? user.getEsgScore() : 0;
            int total = eScore + sScore;

            user.setEScore(eScore);
            user.setSScore(sScore);
            user.setEsgScore(total);
            // user.setEsgScore(questScore);

            userRepository.save(user);

            // ⚡ DB 반영 확인
            User checkFinal = userRepository.findById(userId).get();
            log.info("🌱 [최종 확인] DB 반영 상태 → E={}, S={}, ESG={}",
                    checkFinal.getEScore(), checkFinal.getSScore(), checkFinal.getEsgScore());

        } catch (Exception e) {
            log.error("⚠️ [AI/퀘스트 처리 실패] userId={}, error={}", userId, e.getMessage());
        }
    }

    // @Transactional
    // public LifeLogResponse addLifeLog(Long userId, LifeLogRequest request) {
    //     User user = userRepository.findById(userId)
    //             .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    //     LifeLog log = LifeLog.builder()
    //             .user(user)
    //             .content(request.getContent())
    //             .category(request.getCategory())
    //             .esgScoreEffect(request.getEsgScoreEffect() != null ? request.getEsgScoreEffect() : java.math.BigDecimal.ONE)
    //             .loggedAt(LocalDateTime.now())
    //             .build();

    //     lifeLogRepository.save(log);

    //     // ESG 점수 업데이트
    //     updateUserScores(user);

    //     return LifeLogResponse.fromEntity(log);
    // }

    /** ✅ 기록 수정 */
    @Transactional
    public LifeLogResponse updateLifeLog(Long userId, Long logId, LifeLogRequest request) {
        LifeLog log = lifeLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));
        if (!log.getUser().getId().equals(userId)) {
            throw new IllegalStateException("다른 사용자의 기록을 수정할 수 없습니다.");
        }

        log.setContent(request.getContent());
        log.setCategory(request.getCategory());
        // log.setEsgScoreEffect(request.getEsgScoreEffect());

        if (request.getEsgScoreEffect() != null) {
        log.setEsgScoreEffect(request.getEsgScoreEffect());
    }
        lifeLogRepository.save(log);
        updateUserScores(log.getUser());

        return LifeLogResponse.fromEntity(log);
    }

    /** ✅ 기록 삭제 */
    @Transactional
    public void deleteLifeLog(Long userId, Long logId) {
        LifeLog log = lifeLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));
        if (!log.getUser().getId().equals(userId)) {
            throw new IllegalStateException("다른 사용자의 기록을 삭제할 수 없습니다.");
        }

        lifeLogRepository.delete(log);
        updateUserScores(log.getUser());
    }

    /** ✅ 사용자 ESG 점수 자동 재계산 */
    private void updateUserScores(User user) {
        var eSum = lifeLogRepository.sumScoreByCategory(user.getId(), LifeLog.Category.E);
        var sSum = lifeLogRepository.sumScoreByCategory(user.getId(), LifeLog.Category.S);

        int eScore = eSum != null ? eSum.intValue() : 0;
        int sScore = sSum != null ? sSum.intValue() : 0;
        int total = eScore + sScore;

        user.setEScore(eScore);
        user.setSScore(sScore);
        user.setEsgScore(total);

        userRepository.save(user);
    }
}