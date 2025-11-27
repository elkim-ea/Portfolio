package com.matchaworld.backend.controller.admin;

import com.matchaworld.backend.domain.Title;
import com.matchaworld.backend.dto.request.admin.TitleCreateRequest;
import com.matchaworld.backend.dto.request.admin.TitleUpdateRequest;
import com.matchaworld.backend.dto.response.auth.ApiResponse;
import com.matchaworld.backend.dto.response.PaginatedResponse;
import com.matchaworld.backend.service.admin.AdminTitleService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/titles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminTitleController {

    private final AdminTitleService adminTitleService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllTitles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
            Page<Title> titlePage = adminTitleService.searchTitles(keyword, pageable);
            
            PaginatedResponse<Title> response = PaginatedResponse.of(titlePage);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("칭호 목록 조회 성공")
                    .data(response)
                    .build());
        } catch (Exception e) {
            log.error("칭호 목록 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("칭호 목록 조회에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createTitle(@Valid @RequestBody TitleCreateRequest request) {
        try {
            Title createdTitle = adminTitleService.createTitle(request);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("칭호가 생성되었습니다.")
                    .data(createdTitle)
                    .build());
        } catch (Exception e) {
            log.error("칭호 생성 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("칭호 생성에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateTitle(
            @PathVariable Long id,
            @Valid @RequestBody TitleUpdateRequest request
    ) {
        try {
            log.info("updated data={}",request);
            Title updatedTitle = adminTitleService.updateTitle(id, request);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("칭호가 수정되었습니다.")
                    .data(updatedTitle)
                    .build());
        } catch (IllegalArgumentException e) {
            // 비즈니스 로직 에러 - 400 Bad Request
            log.warn("칭호 수정 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()  // ✅ 400 상태 코드
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())  // "이미 존재하는 칭호명입니다: XXX"
                            .build());
                            
        } catch (EntityNotFoundException e) {
            // 리소스를 찾을 수 없음 - 404 Not Found
            log.warn("칭호 수정 실패 - 칭호를 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)  // ✅ 404 상태 코드
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
                            
        } catch (Exception e) {
            // 예상치 못한 에러 - 500 Internal Server Error
            log.error("칭호 수정 실패 - 서버 에러", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)  // ✅ 500 상태 코드
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("칭호 수정 중 오류가 발생했습니다.")
                            .build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteTitle(@PathVariable Long id) {
        try {
            adminTitleService.deleteTitle(id);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("칭호가 삭제되었습니다.")
                    .build());
        } catch (Exception e) {
            log.error("칭호 삭제 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("칭호 삭제에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }
}
