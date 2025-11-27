package com.matchaworld.backend.dto.response.company;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CompanyResponse {

    private Long companyId;          // 회사 ID
    private String companyName;      // 회사 이름
    private String companyLogo;      // 회사 로고 (이미지 URL)
    private String companyWebsiteUrl;// 회사 웹사이트 URL
    private LocalDateTime createdAt; // 생성일시
}
