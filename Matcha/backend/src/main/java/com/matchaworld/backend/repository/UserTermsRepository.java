package com.matchaworld.backend.repository;

import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.domain.UserTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTermsRepository extends JpaRepository<UserTerms, Long> {
    
    // 특정 사용자의 모든 약관 동의 내역 조회
    List<UserTerms> findByUser(User user);
    
    // 특정 사용자의 약관 동의 내역 조회 (userId로)
    List<UserTerms> findByUserId(Long userId);
}
