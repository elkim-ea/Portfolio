package com.matchaworld.backend.service.company;

import java.util.List;

import com.matchaworld.backend.dto.response.company.CompanyResponse;

public interface CompanyService {

    // 카테고리명(선도 / 후원)으로 기업 목록 조회
    List<CompanyResponse> getCompaniesByCategory(String categoryName);
}
