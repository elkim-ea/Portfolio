package com.matchaworld.backend.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matchaworld.backend.domain.Quest;
import com.matchaworld.backend.dto.request.admin.QuestCreateRequest;
import com.matchaworld.backend.dto.request.admin.QuestUpdateRequest;
import com.matchaworld.backend.dto.response.PaginatedResponse;
import com.matchaworld.backend.dto.response.auth.ApiResponse;
import com.matchaworld.backend.service.admin.AdminQuestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin/quests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminQuestController {

    private final AdminQuestService adminQuestService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllQuests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String authType,
            @RequestParam(required = false) String type
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Quest> questPage = adminQuestService.searchQuests(keyword, authType, type, pageable);
            
            PaginatedResponse<Quest> response = PaginatedResponse.of(questPage);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("퀘스트 목록 조회 성공")
                    .data(response)
                    .build());
        } catch (Exception e) {
            log.error("퀘스트 목록 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("퀘스트 목록 조회에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getQuestById(@PathVariable Long id) {
        try {
            Quest quest = adminQuestService.getQuestById(id);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("퀘스트 조회 성공")
                    .data(quest)
                    .build());
        } catch (Exception e) {
            log.error("퀘스트 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("퀘스트 조회에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createQuest(@Valid @RequestBody QuestCreateRequest request) {
        try {
            Quest createdQuest = adminQuestService.createQuest(request);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("퀘스트가 생성되었습니다.")
                    .data(createdQuest)
                    .build());
        } catch (Exception e) {
            log.error("퀘스트 생성 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("퀘스트 생성에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateQuest(
            @PathVariable Long id,
            @Valid @RequestBody QuestUpdateRequest request
    ) {
        try {
            Quest updatedQuest = adminQuestService.updateQuest(id, request);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("퀘스트가 수정되었습니다.")
                    .data(updatedQuest)
                    .build());
        } catch (Exception e) {
            log.error("퀘스트 수정 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("퀘스트 수정에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteQuest(@PathVariable Long id) {
        try {
            adminQuestService.deleteQuest(id);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("퀘스트가 삭제되었습니다.")
                    .build());
        } catch (Exception e) {
            log.error("퀘스트 삭제 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("퀘스트 삭제에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse> toggleActive(@PathVariable Long id) {
        try {
            Quest updatedQuest = adminQuestService.toggleActive(id);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("퀘스트 상태가 변경되었습니다.")
                    .data(updatedQuest)
                    .build());
        } catch (Exception e) {
            log.error("퀘스트 상태 변경 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("퀘스트 상태 변경에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }
}