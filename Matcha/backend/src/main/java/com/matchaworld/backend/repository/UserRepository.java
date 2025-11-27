package com.matchaworld.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.matchaworld.backend.domain.User;

import jakarta.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
        // ✅ E, S, ESG 점수를 즉시 반영 (flush + commit)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE User u SET u.eScore = :e, u.sScore = :s, u.esgScore = :total WHERE u.id = :userId")
    void updateScores(@Param("userId") Long userId,
                      @Param("e") int e,
                      @Param("s") int s,
                      @Param("total") int total);
    
    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);
    
    // 닉네임으로 사용자 찾기
    Optional<User> findByNickname(String nickname);
    
    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);
    
    // 닉네임 존재 여부 확인
    boolean existsByNickname(String nickname);

    // 권한별 사용자 수 조회
    long countByRole(User.Role role);

    List<User> findAllByOrderByEsgScoreDesc();

    long countByEsgScoreGreaterThan(Integer score);
}