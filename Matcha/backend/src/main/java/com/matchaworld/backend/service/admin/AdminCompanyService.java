package com.matchaworld.backend.service.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.matchaworld.backend.domain.Company;
import com.matchaworld.backend.domain.EsgCategory;
import com.matchaworld.backend.repository.CompanyRepository;
import com.matchaworld.backend.repository.EsgCategoryRepository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCompanyService {

    private final CompanyRepository companyRepository;
    private final EsgCategoryRepository esgCategoryRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 기업 목록 조회 (검색, 카테고리 필터, 페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<Company> searchCompanies(String keyword, String categoryName, Pageable pageable) {
        Specification<Company> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Fetch Join으로 카테고리 미리 로드
            if (query != null && query.getResultType() != Long.class) { // count 쿼리 실행 시 fetch join으로 인한 Hibernate 오류 방지
                root.fetch("categories", JoinType.LEFT);
                query.distinct(true); // 중복 제거
            }

            // 키워드 검색 (기업명)
            if (keyword != null && !keyword.isEmpty()) {
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("companyName")),
                        "%" + keyword.toLowerCase() + "%"
                );
                predicates.add(namePredicate);
            }

            // 카테고리 필터
            if (categoryName != null && !categoryName.isEmpty()) {
                Join<Object, Object> categoryJoin = root.join("categories", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(
                        categoryJoin.get("categoryName"),
                        categoryName
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Company> companies = companyRepository.findAll(spec, pageable);

        // 각 기업의 카테고리를 명시적으로 로드 (Lazy Loading 방지)
        companies.getContent().forEach(company -> {
            company.getCategories().size(); // 강제 초기화
        });

        return companies;
    }

    /**
     * 기업 ID로 조회
     */
    @Transactional(readOnly = true)
    public Company getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("기업을 찾을 수 없습니다. ID: " + id));

        // 카테고리를 미리 로드 (Lazy Loading 문제 방지)
        company.getCategories().size();

        log.info("기업 조회: ID={}, 이름={}, 카테고리={}",
                company.getId(),
                company.getCompanyName(),
                company.getCategoryNames());

        return company;
    }

    /**
     * 기업 생성 (파일 업로드 포함)
     */
    @Transactional
    public Company createCompany(
            String companyName,
            List<String> categoryNames,
            String companyWebsiteUrl,
            MultipartFile logoFile,
            String companyLogoUrl
    ) {
        // 기업명 중복 체크
        if (companyRepository.existsByCompanyName(companyName)) {
            throw new IllegalArgumentException("이미 존재하는 기업명입니다: " + companyName);
        }

        String logoUrl = companyLogoUrl;

        // 파일 업로드가 있는 경우
        if (logoFile != null && !logoFile.isEmpty()) {
            logoUrl = uploadImage(logoFile, "logo");
        }

        // 카테고리 찾기 또는 생성
        Set<EsgCategory> categories = new HashSet<>();
        for (String categoryName : categoryNames) {
            EsgCategory category = findOrCreateCategory(categoryName);
            categories.add(category);
        }

        Company company = Company.builder()
                .companyName(companyName)
                .companyLogo(logoUrl)
                .companyWebsiteUrl(companyWebsiteUrl)
                .categories(categories)
                .build();

        Company savedCompany = companyRepository.save(company);

        // 저장 후 카테고리 정보 로드
        savedCompany.getCategories().size();

        log.info("기업 생성됨: ID={}, 이름={}, 카테고리={}",
                savedCompany.getId(), savedCompany.getCompanyName(), categoryNames);

        return savedCompany;
    }

    /**
     * 기업 수정 (파일 업로드 포함)
     */
    @Transactional
    public Company updateCompany(
            Long id,
            String companyName,
            List<String> categoryNames,
            String companyWebsiteUrl,
            MultipartFile logoFile,
            String companyLogoUrl
    ) {
        Company company = getCompanyById(id);

        // 기업명이 변경되었고, 이미 존재하는 기업명인 경우
        if (!company.getCompanyName().equals(companyName)
                && companyRepository.existsByCompanyName(companyName)) {
            throw new IllegalArgumentException("이미 존재하는 기업명입니다: " + companyName);
        }

        String logoUrl = companyLogoUrl;

        // 새 파일 업로드가 있는 경우
        if (logoFile != null && !logoFile.isEmpty()) {
            // 기존 파일 삭제
            if (company.getCompanyLogo() != null && company.getCompanyLogo().contains("/uploads/")) {
                deleteImageByUrl(company.getCompanyLogo());
            }

            logoUrl = uploadImage(logoFile, "logo");
        }

        // 카테고리 업데이트
        Set<EsgCategory> categories = new HashSet<>();
        for (String categoryName : categoryNames) {
            EsgCategory category = findOrCreateCategory(categoryName);
            categories.add(category);
        }

        company.setCompanyName(companyName);
        company.setCompanyLogo(logoUrl);
        company.setCompanyWebsiteUrl(companyWebsiteUrl);
        company.getCategories().clear();
        company.getCategories().addAll(categories);

        Company updatedCompany = companyRepository.save(company);

        // 업데이트 후 카테고리 정보 로드
        updatedCompany.getCategories().size();

        log.info("기업 수정됨: ID={}, 이름={}, 카테고리={}",
                updatedCompany.getId(), updatedCompany.getCompanyName(), categoryNames);

        return updatedCompany;
    }

    /**
     * 기업 삭제
     */
    @Transactional
    public void deleteCompany(Long id) {
        Company company = getCompanyById(id);

        // COMPANY_CATEGORY 관계를 먼저 명시적으로 해제
        // 이 작업이 JPA에게 중간 테이블 레코드만 삭제하도록 지시
        if (company.getCategories() != null) {
            company.getCategories().clear();
        }

        // 로고 파일 삭제
        if (company.getCompanyLogo() != null && company.getCompanyLogo().contains("/uploads/")) {
            deleteImageByUrl(company.getCompanyLogo());
        }

        companyRepository.delete(company);
        log.info("기업 삭제됨: ID={}, 이름={}", id, company.getCompanyName());
    }

    /**
     * 카테고리 찾기 또는 생성
     */
    private EsgCategory findOrCreateCategory(String categoryName) {
        return esgCategoryRepository.findByCategoryName(categoryName)
                .orElseGet(() -> {
                    EsgCategory newCategory = EsgCategory.builder()
                            .categoryName(categoryName)
                            .build();
                    EsgCategory saved = esgCategoryRepository.save(newCategory);
                    log.info("새 카테고리 생성: {}", categoryName);
                    return saved;
                });
    }

    /**
     * 이미지 파일 업로드
     */
    private String uploadImage(MultipartFile file, String type) {
        try {
            // 파일 검증
            if (!file.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
            }

            // 디렉토리 생성
            Path typePath = Paths.get(uploadDir, type);
            if (!Files.exists(typePath)) {
                Files.createDirectories(typePath);
            }

            // 파일명 생성
            String originalFilename = file.getOriginalFilename();
            // String extension = originalFilename != null ? 
            //         originalFilename.substring(originalFilename.lastIndexOf(".")) : ".png";
            String filename = UUID.randomUUID().toString() + "_" + originalFilename;

            // 파일 저장
            Path filePath = typePath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // String fileUrl = baseUrl + "/uploads/" + type + "/" + filename;
            String fileUrl = baseUrl + "/uploads/" + type + "/" + filename;
            log.info("파일 업로드 성공: {}", fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * URL로 이미지 파일 삭제
     */
    private void deleteImageByUrl(String url) {
        try {
            if (url != null && url.contains("/uploads/")) {
                String relativePath = url.substring(url.indexOf("/uploads/") + 9);
                Path filePath = Paths.get(uploadDir, relativePath);

                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("파일 삭제 성공: {}", relativePath);
                }
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패", e);
        }
    }

    @Transactional(readOnly = true)
    public long getTotalCompanyCount() {
        return companyRepository.count();
    }

    @Transactional(readOnly = true)
    public boolean isCompanyNameExists(String companyName) {
        return companyRepository.existsByCompanyName(companyName);
    }
}
