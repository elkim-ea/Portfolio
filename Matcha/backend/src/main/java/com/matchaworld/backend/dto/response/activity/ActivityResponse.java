// 
package com.matchaworld.backend.dto.response.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.matchaworld.backend.domain.LifeLog;
import com.matchaworld.backend.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * ✅ '나의 활동' 페이지 응답 DTO (최종 통합 버전)
 * - totalScore: E + S 계산값
 * - esgScore: DB의 USER.ESG_SCORE 값 (보상 포함)
 */
@Getter
@Builder
public class ActivityResponse {

    /** ✅ 사용자 점수 정보 **/
    private final int totalScore;   // E + S 계산된 총합
    private final int esgScore;     // DB에서 불러온 ESG_SCORE (보상 반영용)

    @JsonProperty("eScore")
    private final int eScore;

    @JsonProperty("sScore")
    private final int sScore;

    private final String characterUrl;

    /** ✅ 차트 및 로그 데이터 **/
    private final ChartData eWeeklyData;
    private final List<LogSummary> eRecentLogs;
    private final ChartData sMonthlyData;
    private final List<LogSummary> sRecentLogs;

    /**
     * ✅ 정적 팩토리 메서드
     * - User + 로그 데이터 조합으로 DTO 생성
     */
    public static ActivityResponse from(
            User user,
            ChartData eWeekly,
            List<LogSummary> eLogs,
            ChartData sMonthly,
            List<LogSummary> sLogs
    ) {
        String characterPath = user.getCharacter();
        String fullCharacterUrl = (characterPath != null && characterPath.startsWith("/uploads/"))
                ? characterPath
                : "/uploads/character/" + (characterPath != null ? characterPath : "flower.png");

        // ✅ totalScore = E + S, esgScore = DB값 그대로
        int eScore = user.getEScore() != null ? user.getEScore() : 0;
        int sScore = user.getSScore() != null ? user.getSScore() : 0;
        int totalScore = eScore + sScore;
        int esgScore = user.getEsgScore() != null ? user.getEsgScore() : totalScore;

        return ActivityResponse.builder()
                .totalScore(totalScore)
                .esgScore(esgScore)
                .eScore(eScore)
                .sScore(sScore)
                .characterUrl(fullCharacterUrl)
                .eWeeklyData(eWeekly)
                .eRecentLogs(eLogs)
                .sMonthlyData(sMonthly)
                .sRecentLogs(sLogs)
                .build();
    }

    /** ✅ 내부 클래스들 **/
    @Getter
    public static class ChartData {
        private final List<String> labels;
        private final List<Integer> scores;

        public ChartData(List<String> labels, List<Integer> scores) {
            this.labels = labels;
            this.scores = scores;
        }
    }

    @Getter
    public static class LogSummary {
        private final Long logId;
        private final String content;
        private final int esgScoreEffect;
        private final String loggedAt;

        private LogSummary(Long logId, String content, int esgScoreEffect, String loggedAt) {
            this.logId = logId;
            this.content = content;
            this.esgScoreEffect = esgScoreEffect;
            this.loggedAt = loggedAt;
        }

        /** ✅ 엔티티 기반 변환 */
        public static LogSummary fromEntity(LifeLog lifeLog) {
            int scoreEffect = lifeLog.getEsgScoreEffect() != null
                    ? lifeLog.getEsgScoreEffect().intValue()
                    : 0;
            return new LogSummary(
                    lifeLog.getId(),
                    lifeLog.getContent(),
                    scoreEffect,
                    lifeLog.getLoggedAt().format(DateTimeFormatter.ISO_LOCAL_DATE)
            );
        }

        /** ✅ Map 기반 변환 (서비스 레이어에서 Map 반환 시 대응) */
        public static LogSummary fromMap(Map<String, Object> map) {
            Long id = map.get("logId") == null ? null : ((Number) map.get("logId")).longValue();
            String content = map.get("content") == null ? "" : String.valueOf(map.get("content"));
            int effect = map.get("esgScoreEffect") == null ? 0 : ((Number) map.get("esgScoreEffect")).intValue();
            String loggedAt = map.get("loggedAt") == null ? "" : String.valueOf(map.get("loggedAt"));
            return new LogSummary(id, content, effect, loggedAt);
        }
    }
}
