package com.matchaworld.backend.service.admin;

import com.matchaworld.backend.domain.Quest;
import com.matchaworld.backend.dto.request.admin.QuestCreateRequest;
import com.matchaworld.backend.dto.request.admin.QuestUpdateRequest;
import com.matchaworld.backend.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminQuestService {

    private final QuestRepository questRepository;

    /**
     * 퀘스트 목록 조회 (검색, 필터링, 페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<Quest> searchQuests(String keyword, String authType, String type, Pageable pageable) {
        Specification<Quest> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 키워드 검색 (제목 또는 설명)
            if (keyword != null && !keyword.isEmpty()) {
                Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), 
                    "%" + keyword.toLowerCase() + "%"
                );
                Predicate descPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), 
                    "%" + keyword.toLowerCase() + "%"
                );
                predicates.add(criteriaBuilder.or(titlePredicate, descPredicate));
            }

            // 타입 필터 (DAILY, WEEKLY, SEASON)
            if (type != null && !type.isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    root.get("type"), 
                    Quest.Type.valueOf(type)
                ));
            }

            // 인증 타입 필터 (IMAGE, TEXT)
            if (authType != null && !authType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    root.get("authType"), 
                    Quest.AuthType.valueOf(authType)
                ));
            }

            // 활성화 상태 필터
            // if (status != null) {
            //     predicates.add(criteriaBuilder.equal(root.get("isActive"), status));
            // }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return questRepository.findAll(spec, pageable);
    }

    /**
     * 퀘스트 ID로 조회
     */
    @Transactional(readOnly = true)
    public Quest getQuestById(Long id) {
        return questRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("퀘스트를 찾을 수 없습니다. ID: " + id));
    }

    /**
     * 퀘스트 생성
     */
    @Transactional
    public Quest createQuest(QuestCreateRequest request) {
        Quest quest = Quest.builder()
                .adminId(request.getAdminId())
                .title(request.getTitle())
                .description(request.getDescription())
                .rewardScore(request.getRewardScore())
                .category(Quest.Category.valueOf(request.getCategory()))
                .type(Quest.Type.valueOf(request.getType()))
                .authType(Quest.AuthType.valueOf(request.getAuthType()))
                .isActive(request.getIsActive())
                .maxAttempts(request.getMaxAttempts())
                .conditionJson(request.getConditionJson())
                .createdAt(LocalDateTime.now())
                .build();

        Quest savedQuest = questRepository.save(quest);
        log.info("퀘스트 생성됨: ID={}, 제목={}", savedQuest.getId(), savedQuest.getTitle());
        
        return savedQuest;
    }

    /**
     * 퀘스트 수정
     */
    @Transactional
    public Quest updateQuest(Long id, QuestUpdateRequest request) {
        Quest quest = getQuestById(id);

        quest.setTitle(request.getTitle());
        quest.setDescription(request.getDescription());
        quest.setRewardScore(request.getRewardScore());
        quest.setCategory(Quest.Category.valueOf(request.getCategory()));
        quest.setType(Quest.Type.valueOf(request.getType()));
        quest.setAuthType(Quest.AuthType.valueOf(request.getAuthType()));
        quest.setIsActive(request.getIsActive());
        quest.setMaxAttempts(request.getMaxAttempts());
        quest.setConditionJson(request.getConditionJson());

        Quest updatedQuest = questRepository.save(quest);
        log.info("퀘스트 수정됨: ID={}, 제목={}", updatedQuest.getId(), updatedQuest.getTitle());
        
        return updatedQuest;
    }

    /**
     * 퀘스트 삭제
     */
    @Transactional
    public void deleteQuest(Long id) {
        Quest quest = getQuestById(id);
        questRepository.delete(quest);
        log.info("퀘스트 삭제됨: ID={}, 제목={}", id, quest.getTitle());
    }

    /**
     * 퀘스트 활성화 상태 토글
     */
    @Transactional
    public Quest toggleActive(Long id) {
        Quest quest = getQuestById(id);
        quest.setIsActive(!quest.getIsActive());
        
        Quest updatedQuest = questRepository.save(quest);
        log.info("퀘스트 활성화 상태 변경: ID={}, 활성화={}", id, updatedQuest.getIsActive());
        
        return updatedQuest;
    }

    /**
     * 전체 퀘스트 수 조회
     */
    @Transactional(readOnly = true)
    public long getTotalQuestCount() {
        return questRepository.count();
    }

    /**
     * 활성화된 퀘스트 수 조회
     */
    @Transactional(readOnly = true)
    public long getActiveQuestCount() {
        return questRepository.countByIsActive(true);
    }
}