package com.matchaworld.backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestUpdateRequest {
    
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    
    @NotBlank(message = "설명은 필수입니다.")
    private String description;
    
    @NotNull(message = "보상 점수는 필수입니다.")
    @Positive(message = "보상 점수는 양수여야 합니다.")
    private Integer rewardScore;
    
    @NotBlank(message = "ESG 타입은 필수입니다.")
    private String category; // E, S

    @NotBlank(message = "타입은 필수입니다.")
    private String type;
    
    @NotBlank(message = "인증 타입은 필수입니다.")
    private String authType;
    
    @NotNull(message = "활성화 여부는 필수입니다.")
    private Boolean isActive;
    
    @NotNull(message = "최대 시도 횟수는 필수입니다.")
    @Positive(message = "최대 시도 횟수는 양수여야 합니다.")
    private Integer maxAttempts;
    
    private String conditionJson;
}