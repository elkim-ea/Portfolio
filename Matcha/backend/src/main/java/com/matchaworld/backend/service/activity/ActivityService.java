package com.matchaworld.backend.service.activity;

import com.matchaworld.backend.domain.LifeLog;
import com.matchaworld.backend.dto.response.dailyscore.DailyScoreResponse;
import com.matchaworld.backend.repository.LifeLogRepository;
import com.matchaworld.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final LifeLogRepository lifeLogRepository;
    private final UserRepository userRepository;

    /**
     * ✅ 사용자별 E/S/ESG 점수 계산
     */
    public Map<String, Integer> calculateUserScores(Long userId) {
        BigDecimal eTotal = lifeLogRepository.sumScoreByCategory(userId, LifeLog.Category.E);
        BigDecimal sTotal = lifeLogRepository.sumScoreByCategory(userId, LifeLog.Category.S);

        int eScore = eTotal != null ? eTotal.intValue() : 0;
        int sScore = sTotal != null ? sTotal.intValue() : 0;
        int esgScore = eScore + sScore;

        Map<String, Integer> scores = new HashMap<>();
        scores.put("eScore", eScore);
        scores.put("sScore", sScore);
        scores.put("esgScore", esgScore);
        return scores;
    }

    /**
     * ✅ USER 테이블의 점수를 즉시 갱신
     */
    @Transactional
    public void updateUserScores(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Integer> scores = calculateUserScores(userId);

        int eScore = scores.get("eScore");
        int sScore = scores.get("sScore");

        // ✅ 기존 퀘스트 점수(보상 포함)는 유지
        int currentEsg = user.getEsgScore() != null ? user.getEsgScore() : 0;

        // ✅ ESG_SCORE를 다시 계산하지 않고, 기존 값을 유지
        user.setEScore(eScore);
        user.setSScore(sScore);
        user.setEsgScore(currentEsg);
        userRepository.saveAndFlush(user);
    }


    /**
     * ✅ 기간별 점수 (차트용)
     */
    public List<DailyScoreResponse> getScoresByPeriod(
            Long userId,
            LifeLog.Category category,
            LocalDateTime start,
            LocalDateTime end
    ) {
        List<LifeLog> logs = lifeLogRepository.findLogsInPeriod(userId, category, start, end);

        Map<LocalDate, BigDecimal> grouped = new TreeMap<>();
        for (LifeLog log : logs) {
            LocalDate date = log.getLoggedAt().toLocalDate();
            BigDecimal effect = Optional.ofNullable(log.getEsgScoreEffect()).orElse(BigDecimal.ZERO);
            grouped.merge(date, effect, BigDecimal::add);
        }

        return grouped.entrySet().stream()
                .map(e -> new DailyScoreResponse(e.getKey().toString(), e.getValue().intValue()))
                .collect(Collectors.toList());
    }

    /**
     * ✅ 최근 로그 (E/S 구분) - E: 최근 7일 - S: 최근 30일
     */
    public List<Map<String, Object>> getRecentLogs(Long userId, LifeLog.Category category, int limit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = (category == LifeLog.Category.E)
                ? now.minusDays(7)
                : now.minusDays(30);

        List<LifeLog> logs = lifeLogRepository.findLogsInPeriod(userId, category, start, now);

        return logs.stream()
                .sorted(Comparator.comparing(LifeLog::getLoggedAt).reversed())
                .limit(limit)
                .map(log -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("logId", log.getId());
                    map.put("content", log.getContent());
                    map.put("loggedAt", log.getLoggedAt().toLocalDate().toString());
                    map.put("category", log.getCategory().name());
                    map.put("esgScoreEffect", log.getEsgScoreEffect());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * ✅ 매일 자정에 전체 사용자 ESG 점수 자동 업데이트
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void updateAllUserScores() {
        userRepository.findAll().forEach(user -> updateUserScores(user.getId()));
        System.out.println("[Scheduler] ✅ 모든 사용자 ESG 점수 자동 업데이트 완료");
    }
}
