package com.matchaworld.backend.controller.my;

import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.dto.request.my.PasswordUpdateRequest;
import com.matchaworld.backend.dto.request.my.ProfileUpdateRequest;
import com.matchaworld.backend.dto.response.auth.ApiResponse;
import com.matchaworld.backend.dto.response.my.UserProfileResponse;
import com.matchaworld.backend.service.my.UserMyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://127.0.0.1:5173",
    "http://localhost:3000",
    "http://127.0.0.1:3000"
})
public class UserMyController {

    private final UserMyService userMyService;

    /**
     * 마이페이지 정보 조회
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getProfile(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            UserProfileResponse profile = userMyService.getUserProfile(userId);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("프로필 조회 성공")
                    .data(profile)
                    .build());
        } catch (Exception e) {
            log.error("프로필 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("프로필 조회에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    /**
     * 닉네임 변경
     */
    @PutMapping("/profile/nickname")
    public ResponseEntity<ApiResponse> updateNickname(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            userMyService.updateNickname(userId, request.getCurrentPassword(), request.getNewNickname());

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("닉네임이 변경되었습니다.")
                    .build());
        } catch (Exception e) {
            log.error("닉네임 변경 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())    
                    .build());
        }
    }

    /**
     * 비밀번호 변경
     */
    @PutMapping("/profile/password")
    public ResponseEntity<ApiResponse> updatePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordUpdateRequest request
    ) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            userMyService.updatePassword(userId, request.getCurrentPassword(), request.getNewPassword());

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("비밀번호가 변경되었습니다.")
                    .build());
        } catch (Exception e) {
            log.error("비밀번호 변경 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/profile")
    public ResponseEntity<ApiResponse> deleteAccount(
            Authentication authentication,
            @RequestBody PasswordUpdateRequest request
    ) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            userMyService.deleteAccount(userId, request.getCurrentPassword());

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("회원 탈퇴가 완료되었습니다.")
                    .build());
        } catch (Exception e) {
            log.error("회원 탈퇴 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * 대표 칭호 설정
     */
    @PutMapping("/profile/title/{titleId}")
    public ResponseEntity<ApiResponse> setMainTitle(
            Authentication authentication,
            @PathVariable Long titleId
    ) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            userMyService.setMainTitle(userId, titleId);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("대표 칭호가 설정되었습니다.")
                    .build());
        } catch (Exception e) {
            log.error("대표 칭호 설정 실패", e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }
}
