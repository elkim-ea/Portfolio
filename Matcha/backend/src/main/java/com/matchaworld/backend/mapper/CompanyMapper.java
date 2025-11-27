package com.matchaworld.backend.mapper;

import com.matchaworld.backend.domain.Company;
import com.matchaworld.backend.dto.response.company.CompanyResponse;

public class CompanyMapper {

    // ✅ 서버 기본 URL (환경 변수나 properties로 빼도 됨)
    private static final String BASE_URL = "http://localhost:8080";

    public static CompanyResponse toResponse(Company company) {
        String logoPath = company.getCompanyLogo();

        // ✅ 상대경로일 경우, BASE_URL 붙여서 절대경로로 변환
        if (logoPath != null && !logoPath.startsWith("http")) {
            // 앞에 "/"가 없으면 붙여주기
            if (!logoPath.startsWith("/")) {
                logoPath = "/" + logoPath;
            }
            logoPath = BASE_URL + logoPath;
        }

        return CompanyResponse.builder()
                .companyId(company.getId())
                .companyName(company.getCompanyName())
                .companyLogo(logoPath) // 보정된 절대경로 사용
                .companyWebsiteUrl(company.getCompanyWebsiteUrl())
                .createdAt(company.getCreatedAt())
                .build();
    }
}
