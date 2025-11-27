package com.matchaworld.backend.service.auth;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matchaworld.backend.domain.Terms;
import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.domain.UserTerms;
import com.matchaworld.backend.repository.TermsRepository;
import com.matchaworld.backend.repository.UserTermsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TermsService {
    
    private final TermsRepository termsRepository;
    private final UserTermsRepository userTermsRepository;
    
    /**
     * 모든 약관 조회
     */
    @Transactional(readOnly = true)
    public List<Terms> getAllTerms() {
        return termsRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * 필수 약관만 조회
     */
    @Transactional(readOnly = true)
    public List<Terms> getRequiredTerms() {
        return termsRepository.findByIsRequiredTrueOrderByCreatedAtDesc();
    }
    
    /**
     * 약관 ID로 조회
     */
    @Transactional(readOnly = true)
    public Terms getTermsById(Long id) {
        return termsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("약관을 찾을 수 없습니다."));
    }
    
    /**
     * 사용자의 약관 동의 내역 저장
     */
    @Transactional
    public void saveUserTermsAgreement(User user, List<Long> agreedTermsIds) {
        if (agreedTermsIds == null || agreedTermsIds.isEmpty()) {
            throw new IllegalArgumentException("동의한 약관이 없습니다.");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        for (Long termId : agreedTermsIds) {
            Terms terms = termsRepository.findById(termId)
                    .orElseThrow(() -> new IllegalArgumentException("약관을 찾을 수 없습니다: " + termId));
            
            UserTerms userTerms = UserTerms.builder()
                    .user(user)
                    .terms(terms)
                    .isAgreed(true)
                    .agreedAt(now)
                    .build();
            
            userTermsRepository.save(userTerms);
            log.info("약관 동의 저장: userId={}, termId={}, title={}", 
                    user.getId(), terms.getId(), terms.getTitle());
        }
    }
    
    /**
     * 사용자의 약관 동의 내역 조회
     */
    @Transactional(readOnly = true)
    public List<UserTerms> getUserTermsAgreements(User user) {
        return userTermsRepository.findByUser(user);
    }
    
    /**
     * 필수 약관 모두 동의했는지 확인
     */
    @Transactional(readOnly = true)
    public boolean hasAgreedToRequiredTerms(List<Long> agreedTermsIds) {
        List<Terms> requiredTerms = getRequiredTerms();
        
        if (requiredTerms.isEmpty()) {
            return true; // 필수 약관이 없으면 통과
        }
        
        if (agreedTermsIds == null || agreedTermsIds.isEmpty()) {
            return false;
        }
        
        // 모든 필수 약관이 동의 목록에 포함되어 있는지 확인
        for (Terms requiredTerm : requiredTerms) {
            if (!agreedTermsIds.contains(requiredTerm.getId())) {
                log.warn("필수 약관 미동의: {}", requiredTerm.getTitle());
                return false;
            }
        }
        
        return true;
    }
}