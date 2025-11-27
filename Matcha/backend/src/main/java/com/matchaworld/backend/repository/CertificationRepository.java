package com.matchaworld.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.matchaworld.backend.domain.Certification;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    // ✅ 필요 시 사용자별 인증 로그 조회
    Certification findByUser_Id(Long userId);

    // ✅ 특정 퀘스트 인증 내역 조회 (선택)
    Certification findByUserQuest_Id(Long userQuestId);
}
