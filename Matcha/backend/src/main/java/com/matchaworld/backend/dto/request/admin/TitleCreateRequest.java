package com.matchaworld.backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TitleCreateRequest {
    
    @NotBlank(message = "칭호명은 필수입니다.")
    private String name;
    
    @NotBlank(message = "설명은 필수입니다.")
    private String description;
    
    @NotBlank(message = "조건 JSON은 필수입니다.")
    private String conditionJson;
}