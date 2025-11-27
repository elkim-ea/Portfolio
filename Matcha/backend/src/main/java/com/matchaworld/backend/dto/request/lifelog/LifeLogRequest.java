package com.matchaworld.backend.dto.request.lifelog;

import java.math.BigDecimal;

import com.matchaworld.backend.domain.LifeLog.Category;

import lombok.*;

/**
 * LifeLog - 사용자의 활동 기록 요청 DTO (POST/PUT)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LifeLogRequest {
    private String content;                   // 활동 내용
    private Category category;                  // 카테고리 (E, S)
    private BigDecimal esgScoreEffect;        // 점수 효과 (선택적, 기본값 가능)
}
