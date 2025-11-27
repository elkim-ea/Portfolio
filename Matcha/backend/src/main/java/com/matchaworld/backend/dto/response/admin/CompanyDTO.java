package com.matchaworld.backend.dto.response.admin;

import com.matchaworld.backend.domain.Company;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CompanyDTO {
    private Long id;
    private String companyName;
    private List<String> categories;
    private String companyLogo;
    private String companyWebsiteUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static CompanyDTO from(Company company) {
        return CompanyDTO.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .categories(new ArrayList<>(company.getCategoryNames()))
                .companyLogo(company.getCompanyLogo())
                .companyWebsiteUrl(company.getCompanyWebsiteUrl())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}