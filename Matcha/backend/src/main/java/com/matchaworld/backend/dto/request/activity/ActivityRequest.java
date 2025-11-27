package com.matchaworld.backend.dto.request.activity;

import lombok.Getter;
import lombok.NoArgsConstructor;

// '나의 활동' 데이터를 조회할 때 필요한 요청 DTO
@Getter
@NoArgsConstructor
public class ActivityRequest {
    private Long userId;     // 사용자 ID
    private String category; // "E" or "S" 구분 (선택적)

    // 추후 필터 조건(예: 기간, 카테고리 등) 추가 가능
}
