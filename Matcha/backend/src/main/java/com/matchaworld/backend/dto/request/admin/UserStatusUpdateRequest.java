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
public class UserStatusUpdateRequest {
    
    @NotBlank(message = "상태는 필수입니다.")
    private String status; // ACTIVE, INACTIVE, BANNED
}