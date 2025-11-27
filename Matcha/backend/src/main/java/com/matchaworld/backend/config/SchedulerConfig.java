package com.matchaworld.backend.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.matchaworld.backend.service.auth.VerificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {
    
    private final VerificationService verificationService;
    
    /**
     * 매일 새벽 3시에 만료된 인증번호 정리
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredCodes() {
        log.info("만료된 인증번호 정리 스케줄러 시작");
        verificationService.cleanupExpiredCodes();
        log.info("만료된 인증번호 정리 스케줄러 종료");
    }
}