package com.matchaworld.backend.dto.request.my;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PasswordUpdateRequest {
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;

    @Pattern(
        regexp = "^(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
        message = "비밀번호는 8자 이상, 특수문자를 포함해야 합니다."
    )
    private String newPassword;
}