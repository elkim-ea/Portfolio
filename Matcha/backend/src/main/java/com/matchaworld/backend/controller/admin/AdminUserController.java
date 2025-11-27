package com.matchaworld.backend.controller.admin;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.dto.response.PaginatedResponse;
import com.matchaworld.backend.dto.response.auth.ApiResponse;
import com.matchaworld.backend.service.admin.AdminUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,           // 추가
            @RequestParam(defaultValue = "desc") String sortDirection,         // 추가
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role
    ) {
        try {
            log.info(keyword);
            // 동적 정렬 설정
            Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<User> userPage = adminUserService.searchUsers(keyword, role, pageable);
            
            PaginatedResponse<User> response = PaginatedResponse.<User>of(userPage);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("사용자 목록 조회 성공")
                    .data(response)
                    .build());
        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("사용자 목록 조회에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        try {
            User user = adminUserService.getUserById(id);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("사용자 조회 성공")
                    .data(user)
                    .build());
        } catch (Exception e) {
            log.error("사용자 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("사용자 조회에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateUser(
            @PathVariable Long id,
            @RequestBody User userDto
    ) {
        try {
            User updatedUser = adminUserService.updateUser(id, userDto);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("사용자 정보가 수정되었습니다.")
                    .data(updatedUser)
                    .build());
        } catch (Exception e) {
            log.error("사용자 수정 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("사용자 수정에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        try {
            adminUserService.deleteUser(id);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("사용자가 삭제되었습니다.")
                    .build());
        } catch (Exception e) {
            log.error("사용자 삭제 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("사용자 삭제에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse> changeRole(
            @PathVariable Long id,
            @RequestParam String role,
            Authentication authentication
    ) {
        try {
            Long currentUserId = (Long) authentication.getPrincipal();

            if (id.equals(currentUserId)) {
                log.warn("사용자가 자신의 권한을 변경하려고 시도: userId={}", currentUserId);

                User.Role newRole = User.Role.valueOf(role.toUpperCase());
                User updatedUser = adminUserService.changeUserRole(id, newRole);
                
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("권한이 변경되었습니다. 다시 로그인해주세요.")
                        .data(Map.of(  
                            "user", updatedUser,
                            "requiresRelogin", true  // 재로그인 필요 플래그
                        ))
                        .build());
            }

            User.Role newRole = User.Role.valueOf(role.toUpperCase());
            User updatedUser = adminUserService.changeUserRole(id, newRole); 

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("사용자 권한이 변경되었습니다.")
                    .data(updatedUser)
                    .build());
        } catch (IllegalArgumentException e) {
            log.error("잘못된 권한 값: {}", role);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("잘못된 권한 값입니다.")
                    .build());
        } catch (Exception e) {
            log.error("사용자 권한 변경 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("사용자 권한 변경에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }
}