package com.matchaworld.backend.controller.company;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matchaworld.backend.dto.response.auth.ApiResponse;
import com.matchaworld.backend.dto.response.company.CompanyResponse;
import com.matchaworld.backend.service.JwtService;
import com.matchaworld.backend.service.company.CompanyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // 개발 환경 CORS 허용
public class CompanyController {

    private final CompanyService companyService;
    private final JwtService jwtService;

    // JWT → userId 추출 (필요 시 추후 사용)
    private Long extractUserIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header가 유효하지 않습니다.");
        }
        String token = authorizationHeader.substring(7);
        return jwtService.extractUserId(token);
    }

    // ESG 선도 기업 조회 (LEADER 카테고리)
    @GetMapping("/leading")
    public ResponseEntity<ApiResponse> getLeadingCompanies(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        List<CompanyResponse> companies = companyService.getCompaniesByCategory("LEADER");

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("ESG 선도 기업 조회 성공")
                .data(companies)
                .build();

        return ResponseEntity.ok(response);
    }

    // ESG 후원 기업 조회 (SPONSOR 카테고리)
    @GetMapping("/sponsor")
    public ResponseEntity<ApiResponse> getSponsorCompanies(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        List<CompanyResponse> companies = companyService.getCompaniesByCategory("SPONSOR");

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("ESG 후원 기업 조회 성공")
                .data(companies)
                .build();

        return ResponseEntity.ok(response);
    }
}
