package com.matchaworld.backend.service.admin;

import com.matchaworld.backend.domain.Title;
import com.matchaworld.backend.dto.request.admin.TitleCreateRequest;
import com.matchaworld.backend.dto.request.admin.TitleUpdateRequest;
import com.matchaworld.backend.repository.TitleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminTitleService {

    private final TitleRepository titleRepository;

    /**
     * 칭호 목록 조회 (검색, 페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<Title> searchTitles(String keyword, Pageable pageable) {
        Specification<Title> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 키워드 검색 (칭호명 또는 설명)
            if (keyword != null && !keyword.isEmpty()) {
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), 
                    "%" + keyword.toLowerCase() + "%"
                );
                Predicate descPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), 
                    "%" + keyword.toLowerCase() + "%"
                );
                predicates.add(criteriaBuilder.or(namePredicate, descPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return titleRepository.findAll(spec, pageable);
    }

    /**
     * 칭호 ID로 조회
     */
    @Transactional(readOnly = true)
    public Title getTitleById(Long id) {
        return titleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("칭호를 찾을 수 없습니다. ID: " + id));
    }

    /**
     * 칭호 생성
     */
    @Transactional
    public Title createTitle(TitleCreateRequest request) {
        // 칭호명 중복 체크
        if (titleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 칭호명입니다: " + request.getName());
        }

        Title title = Title.builder()
                .name(request.getName())
                .description(request.getDescription())
                .conditionJson(request.getConditionJson())
                .build();

        Title savedTitle = titleRepository.save(title);
        log.info("칭호 생성됨: ID={}, 이름={}", savedTitle.getId(), savedTitle.getName());
        
        return savedTitle;
    }

    /**
     * 칭호 수정
     */
    @Transactional
    public Title updateTitle(Long id, TitleUpdateRequest request) {
        Title title = getTitleById(id);

        // 칭호명이 변경되었고, 이미 존재하는 칭호명인 경우
        if (!title.getName().equals(request.getName()) && 
            titleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 칭호명입니다: " + request.getName());
            
        }

        title.setName(request.getName());
        title.setDescription(request.getDescription());

        if (request.getConditionJson() != null) {
            validateJson(request.getConditionJson());
            title.setConditionJson(request.getConditionJson());
        }else{
            throw new IllegalArgumentException("JSON이 올바른 형식이 아닙니다.: " + request.getName());
        }

        Title updatedTitle = titleRepository.save(title);
        log.info("칭호 수정됨: ID={}, 이름={}, JSON={}", updatedTitle.getId(), updatedTitle.getName(), updatedTitle.getConditionJson());
        return updatedTitle;
    }

    /**
     * 칭호 삭제
     */
    @Transactional
    public void deleteTitle(Long id) {
        Title title = getTitleById(id);
        
        // 사용자가 보유한 칭호인지 체크 (필요시)
        // if (userTitleRepository.existsByTitleId(id)) {
        //     throw new IllegalArgumentException("사용자가 보유한 칭호는 삭제할 수 없습니다.");
        // }
        
        titleRepository.delete(title);
        log.info("칭호 삭제됨: ID={}, 이름={}", id, title.getName());
    }

    /**
     * 전체 칭호 수 조회
     */
    @Transactional(readOnly = true)
    public long getTotalTitleCount() {
        return titleRepository.count();
    }

    /**
     * 칭호명 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean isNameExists(String name) {
        return titleRepository.existsByName(name);
    }


    // JSON 형식 검증
    private void validateJson(String json) {
        try {
            if (json != null && !json.isEmpty()) {
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 JSON 형식입니다: " + e.getMessage());
        }
    }

}