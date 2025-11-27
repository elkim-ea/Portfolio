package com.matchaworld.backend.dto.response.dailyscore;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DailyScore - 날짜별 ESG 점수 응답용 DTO
 */
@Data
@AllArgsConstructor
public class DailyScoreResponse {

    private String date;  // YYYY-MM-DD
    private int score;    // 해당 날짜의 점수
}
