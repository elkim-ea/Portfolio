package com.matchaworld.backend.controller.admin;

import com.matchaworld.backend.domain.Company;
import com.matchaworld.backend.dto.response.auth.ApiResponse;
import com.matchaworld.backend.dto.response.PaginatedResponse;
import com.matchaworld.backend.dto.response.admin.CompanyDTO;
import com.matchaworld.backend.service.admin.AdminCompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminCompanyController {

    private final AdminCompanyService adminCompanyService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Company> companyPage = adminCompanyService.searchCompanies(keyword, category, pageable);
            
            Page<CompanyDTO> dtoPage = companyPage.map(CompanyDTO::from);
            PaginatedResponse<CompanyDTO> response = PaginatedResponse.of(dtoPage);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("기업 목록 조회 성공")
                    .data(response)
                    .build());
        } catch (Exception e) {
            log.error("목록 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("기업 목록 조회에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCompanyById(@PathVariable Long id) {
        try {
            Company company = adminCompanyService.getCompanyById(id);
            CompanyDTO dto = CompanyDTO.from(company);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("기업 조회 성공")
                    .data(dto)
                    .build());
        } catch (Exception e) {
            log.error("기업 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("기업 조회에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    /**
     * 기업 생성 (파일 업로드 포함)
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> createCompany(
            @RequestParam("companyName") String companyName,
            @RequestParam("categories") List<String> categories,
            @RequestParam(value = "companyWebsiteUrl", required = false) String companyWebsiteUrl,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            @RequestParam(value = "companyLogo", required = false) String companyLogo
    ) {
        try {
            Company createdCompany = adminCompanyService.createCompany(
                    companyName,
                    categories,
                    companyWebsiteUrl,
                    logoFile,
                    companyLogo
            );

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("기업이 생성되었습니다.")
                    .data(CompanyDTO.from(createdCompany))
                    .build());
        } catch (Exception e) {
            log.error("기업 생성 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("기업 생성에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    /**
     * 기업 수정 (파일 업로드 포함)
     */
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> updateCompany(
            @PathVariable Long id,
            @RequestParam("companyName") String companyName,
            @RequestParam("categories") List<String> categories,
            @RequestParam(value = "companyWebsiteUrl", required = false) String companyWebsiteUrl,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            @RequestParam(value = "companyLogo", required = false) String companyLogo
    ) {
        try {
            Company updatedCompany = adminCompanyService.updateCompany(
                    id,
                    companyName,
                    categories,
                    companyWebsiteUrl,
                    logoFile,
                    companyLogo
            );

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("기업 정보가 수정되었습니다.")
                    .data(CompanyDTO.from(updatedCompany))
                    .build());
        } catch (Exception e) {
            log.error("기업 수정 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("기업 수정에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCompany(@PathVariable Long id) {
        try {
            adminCompanyService.deleteCompany(id);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("기업이 삭제되었습니다.")
                    .build());
        } catch (Exception e) {
            log.error("기업 삭제 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("기업 삭제에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }
}