package com.matchaworld.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.matchaworld.backend.domain.Terms;

@Repository
public interface TermsRepository extends JpaRepository<Terms, Long> {
    
    // 모든 약관 조회 (최신순)
    List<Terms> findAllByOrderByCreatedAtDesc();
    
    // 필수 약관만 조회
    List<Terms> findByIsRequiredTrueOrderByCreatedAtDesc();
    
    // 제목으로 약관 조회
    Optional<Terms> findByTitle(String title);
}
