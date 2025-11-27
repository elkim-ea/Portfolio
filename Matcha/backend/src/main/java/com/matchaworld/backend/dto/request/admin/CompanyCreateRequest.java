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
public class CompanyCreateRequest {
    
    @NotBlank(message = "기업명은 필수입니다.")
    private String companyName;
    
    @NotBlank(message = "로고 URL은 필수입니다.")
    private String companyLogo;
    
    @NotBlank(message = "웹사이트 URL은 필수입니다.")
    private String companyWebsiteUrl;
}