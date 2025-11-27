package com.matchaworld.backend.dto.response.ranking;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Ranking - 사용자 ESG 순위 응답 DTO
 * 사용처: /api/ranking/global, /api/ranking/me
 */
@Data
@AllArgsConstructor
public class RankingResponse {
    private int rank;         // 순위
    private String nickname;  // 사용자 닉네임
    private int score;        // ESG 종합 점수
}
