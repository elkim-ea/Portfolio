package com.matchaworld.backend.service.auth;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matchaworld.backend.domain.AuthCode;
import com.matchaworld.backend.repository.AuthCodeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {
    
    private final AuthCodeRepository authCodeRepository;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();
    
    @Value("${app.verification.code-expiration-minutes:3}")
    private int codeExpirationMinutes;
    
    /**
     * 회원가입용 인증번호 생성 및 전송
     */
    @Transactional
    public void sendSignupVerificationCode(String email) {
        // 기존 미인증 코드 무효화
        authCodeRepository.deleteUnverifiedByEmail(email);
        
        // 새 인증번호 생성
        String code = generateVerificationCode();
        
        // DB에 저장
        AuthCode authCode = AuthCode.builder()
                .email(email)
                .authCode(code)
                .expiryTime(LocalDateTime.now().plusMinutes(codeExpirationMinutes))
                .isVerified(false)
                .build();
        
        authCodeRepository.save(authCode);
        
        // 이메일 전송
        emailService.sendSignupVerificationEmail(email, code);
        
        log.info("회원가입 인증번호 전송 완료: email={}, code={}", email, code);
    }
    
    /**
     * 비밀번호 재설정용 인증번호 생성 및 전송
     */
    @Transactional
    public void sendPasswordResetCode(String email) {
        // 기존 미인증 코드 무효화
        authCodeRepository.deleteUnverifiedByEmail(email);
        
        // 새 인증번호 생성
        String code = generateVerificationCode();
        
        // DB에 저장
        AuthCode authCode = AuthCode.builder()
                .email(email)
                .authCode(code)
                .expiryTime(LocalDateTime.now().plusMinutes(codeExpirationMinutes))
                .isVerified(false)
                .build();
        
        authCodeRepository.save(authCode);
        
        // 이메일 전송
        emailService.sendPasswordResetEmail(email, code);
        
        log.info("비밀번호 재설정 인증번호 전송 완료: email={}, code={}", email, code);
    }
    
    /**
     * 인증번호 검증
     */
    @Transactional
    public boolean verifyCode(String email, String code) {
        Optional<AuthCode> optionalAuthCode = authCodeRepository
                .findTopByEmailAndAuthCodeOrderByCreatedAtDesc(email, code);
        
        if (optionalAuthCode.isEmpty()) {
            log.warn("인증번호를 찾을 수 없음: email={}, code={}", email, code);
            return false;
        }
        
        AuthCode authCode = optionalAuthCode.get();
        
        // 이미 인증된 코드
        if (authCode.getIsVerified()) {
            log.warn("이미 사용된 인증번호: email={}", email);
            return false;
        }
        
        // 만료된 코드
        if (authCode.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("만료된 인증번호: email={}, expiryTime={}", email, authCode.getExpiryTime());
            return false;
        }
        
        // 인증 성공
        authCode.setIsVerified(true);
        authCodeRepository.save(authCode);
        
        log.info("인증번호 검증 성공: email={}", email);
        return true;
    }
    
    /**
     * 이메일 인증 여부 확인
     */
    public boolean isEmailVerified(String email) {
        return authCodeRepository.existsVerifiedByEmail(email, LocalDateTime.now());
    }
    
    /**
     * 6자리 랜덤 인증번호 생성
     */
    private String generateVerificationCode() {
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }
    
    /**
     * 만료된 인증번호 정리 (스케줄러에서 호출)
     */
    @Transactional
    public void cleanupExpiredCodes() {
        authCodeRepository.deleteExpiredCodes(LocalDateTime.now());
        log.info("만료된 인증번호 정리 완료");
    }
}