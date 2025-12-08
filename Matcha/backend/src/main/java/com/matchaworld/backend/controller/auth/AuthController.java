package com.matchaworld.backend.controller.auth;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matchaworld.backend.domain.Terms;
import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.dto.request.auth.EmailRequest;
import com.matchaworld.backend.dto.request.auth.LoginRequest;
import com.matchaworld.backend.dto.request.auth.ResetPasswordRequest;
import com.matchaworld.backend.dto.request.auth.SignupRequest;
import com.matchaworld.backend.dto.request.auth.VerifyCodeRequest;
import com.matchaworld.backend.dto.response.auth.ApiResponse;
import com.matchaworld.backend.dto.response.auth.LoginResponse;
import com.matchaworld.backend.dto.response.auth.TermsResponse;
import com.matchaworld.backend.service.auth.TermsService;
import com.matchaworld.backend.service.auth.UserService;
import com.matchaworld.backend.service.auth.VerificationService;
import com.matchaworld.backend.service.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://127.0.0.1:5173",
    "http://localhost:3000",
    "http://127.0.0.1:3000",
    "http://34.64.88.163",
    "http://34.64.80.19"
})
public class AuthController {
    
    private final VerificationService verificationService;
    private final UserService userService;
    private final JwtService JwtService;

    private final TermsService termsService;
    
    /**
     * 회원가입용 인증번호 전송
     */
    @PostMapping("/signup/send-code")
    public ResponseEntity<ApiResponse> sendSignupCode(@Valid @RequestBody EmailRequest request) {
        try {
            log.info("회원가입 인증번호 전송 요청: {}", request.getEmail());
            
            // 이메일 중복 체크
            if (userService.isEmailExists(request.getEmail())) {
                return ResponseEntity.ok(
                    ApiResponse.builder()
                        .success(false)
                        .message("이미 가입된 이메일입니다.")
                        .build()
                );
            }
            
            verificationService.sendSignupVerificationCode(request.getEmail());
            
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(true)
                    .message("인증번호가 이메일로 전송되었습니다.")
                    .build()
            );
        } catch (Exception e) {
            log.error("인증번호 전송 실패: {}", request.getEmail(), e);
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(false)
                    .message("인증번호 전송에 실패했습니다. 다시 시도해주세요.")
                    .build()
            );
        }
    }
    
    /**
     * 비밀번호 재설정용 인증번호 전송
     */
    @PostMapping("/password-reset/send-code")
    public ResponseEntity<ApiResponse> sendPasswordResetCode(@Valid @RequestBody EmailRequest request) {
        try {
            log.info("비밀번호 재설정 인증번호 전송 요청: {}", request.getEmail());
            
            // 사용자 존재 여부 확인
            if (!userService.isEmailExists(request.getEmail())) {
                return ResponseEntity.ok(
                    ApiResponse.builder()
                        .success(false)
                        .message("가입되지 않은 이메일입니다.")
                        .build()
                );
            }
            
            verificationService.sendPasswordResetCode(request.getEmail());
            
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(true)
                    .message("인증번호가 이메일로 전송되었습니다.")
                    .build()
            );
        } catch (Exception e) {
            log.error("인증번호 전송 실패: {}", request.getEmail(), e);
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(false)
                    .message("인증번호 전송에 실패했습니다. 다시 시도해주세요.")
                    .build()
            );
        }
    }
    
    /**
     * 인증번호 확인
     */
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        try {
            log.info("인증번호 확인 요청: {}", request.getEmail());
            
            boolean isValid = verificationService.verifyCode(request.getEmail(), request.getCode());
            
            if (isValid) {
                return ResponseEntity.ok(
                    ApiResponse.builder()
                        .success(true)
                        .message("인증이 완료되었습니다.")
                        .build()
                );
            } else {
                return ResponseEntity.ok(
                    ApiResponse.builder()
                        .success(false)
                        .message("인증번호가 일치하지 않거나 만료되었습니다.")
                        .build()
                );
            }
        } catch (Exception e) {
            log.error("인증번호 확인 실패: {}", request.getEmail(), e);
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(false)
                    .message("인증번호 확인에 실패했습니다.")
                    .build()
            );
        }
    }
    
    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody SignupRequest request) {
        try {
            log.info("회원가입 요청: {}", request.getEmail());
            
            // 이메일 인증 여부 확인
            if (!verificationService.isEmailVerified(request.getEmail())) {
                return ResponseEntity.ok(
                    ApiResponse.builder()
                        .success(false)
                        .message("이메일 인증을 먼저 완료해주세요.")
                        .build()
                );
            }
            
            // 닉네임 중복 체크
            if (userService.isNicknameExists(request.getNickname())) {
                return ResponseEntity.ok(
                    ApiResponse.builder()
                        .success(false)
                        .message("이미 사용 중인 닉네임입니다.")
                        .build()
                );
            }
            
            // 회원가입 처리 (약관 동의 포함)
            User user = userService.signup(
                request.getNickname(),
                request.getEmail(),
                request.getPassword(),
                request.getAgreedTermsIds()
            );
            
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(true)
                    .message("회원가입이 완료되었습니다.")
                    .build()
            );
        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("회원가입 실패: {}", request.getEmail(), e);
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(false)
                    .message("회원가입에 실패했습니다.")
                    .build()
            );
        }
    }
    
    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("로그인 요청: {}", request.getEmail());
            
            // 로그인 처리
            User user = userService.login(request.getEmail(), request.getPassword());
            
            // JWT 토큰 생성
            String token = JwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
            );
            
            return ResponseEntity.ok(
                LoginResponse.builder()
                    .success(true)
                    .message("로그인 성공")
                    .token(token)
                    .user(LoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .role(user.getRole().name())
                        .build())
                    .build()
            );
        } catch (IllegalArgumentException e) {
            log.warn("로그인 실패: {}", e.getMessage());
            return ResponseEntity.ok(
                LoginResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("로그인 실패: {}", request.getEmail(), e);
            return ResponseEntity.ok(
                LoginResponse.builder()
                    .success(false)
                    .message("로그인에 실패했습니다.")
                    .build()
            );
        }
    }
    
    /**
     * 비밀번호 재설정
     */
    @PostMapping("/password-reset")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            log.info("비밀번호 재설정 요청: {}", request.getEmail());
            
            // 이메일 인증 여부 확인
            if (!verificationService.isEmailVerified(request.getEmail())) {
                return ResponseEntity.ok(
                    ApiResponse.builder()
                        .success(false)
                        .message("이메일 인증을 먼저 완료해주세요.")
                        .build()
                );
            }
            
            // 비밀번호 재설정
            userService.resetPassword(request.getEmail(), request.getNewPassword());
            
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(true)
                    .message("비밀번호가 재설정되었습니다.")
                    .build()
            );
        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 재설정 실패: {}", e.getMessage());
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("비밀번호 재설정 실패: {}", request.getEmail(), e);
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(false)
                    .message("비밀번호 재설정에 실패했습니다.")
                    .build()
            );
        }
    }


    /**
     * 모든 약관 조회
     */
    @GetMapping("/terms")
    public ResponseEntity<ApiResponse> getAllTerms() {
        try {
            List<Terms> terms = termsService.getAllTerms();
            List<TermsResponse> termsDTOs = terms.stream()
                    .map(TermsResponse::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(true)
                    .message("약관 조회 성공")
                    .data(termsDTOs)
                    .build()
            );
        } catch (Exception e) {
            log.error("약관 조회 실패", e);
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(false)
                    .message("약관 조회에 실패했습니다.")
                    .build()
            );
        }
    }
    
    /**
     * 필수 약관만 조회
     */
    @GetMapping("/terms/required")
    public ResponseEntity<ApiResponse> getRequiredTerms() {
        try {
            List<Terms> terms = termsService.getRequiredTerms();
            List<TermsResponse> termsDTOs = terms.stream()
                    .map(TermsResponse::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(true)
                    .message("필수 약관 조회 성공")
                    .data(termsDTOs)
                    .build()
            );
        } catch (Exception e) {
            log.error("필수 약관 조회 실패", e);
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(false)
                    .message("필수 약관 조회에 실패했습니다.")
                    .build()
            );
        }
    }
    
    /**
     * 특정 약관 상세 조회
     */
    @GetMapping("/terms/{id}")
    public ResponseEntity<ApiResponse> getTermsById(@PathVariable Long id) {
        try {
            Terms terms = termsService.getTermsById(id);
            TermsResponse termsDTO = TermsResponse.from(terms);
            
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(true)
                    .message("약관 조회 성공")
                    .data(termsDTO)
                    .build()
            );
        } catch (Exception e) {
            log.error("약관 조회 실패: id={}", id, e);
            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(false)
                    .message("약관 조회에 실패했습니다.")
                    .build()
            );
        }
    }












}