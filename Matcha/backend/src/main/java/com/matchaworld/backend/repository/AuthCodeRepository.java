package com.matchaworld.backend.repository;

import com.matchaworld.backend.domain.AuthCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AuthCodeRepository extends JpaRepository<AuthCode, Long> {
    
    /**
     * 이메일과 인증번호로 가장 최근 인증코드 찾기
     */
    Optional<AuthCode> findTopByEmailAndAuthCodeOrderByCreatedAtDesc(String email, String authCode);
    
    /**
     * 이메일로 가장 최근 인증코드 찾기
     */
    Optional<AuthCode> findTopByEmailOrderByCreatedAtDesc(String email);
    
    /**
     * 이메일의 미인증 코드 모두 삭제
     */
    @Modifying
    @Query("DELETE FROM AuthCode a WHERE a.email = :email AND a.isVerified = false")
    void deleteUnverifiedByEmail(@Param("email") String email);
    
    /**
     * 만료된 인증번호 삭제
     */
    @Modifying
    @Query("DELETE FROM AuthCode a WHERE a.expiryTime < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);
    
    /**
     * 이메일로 인증된 코드가 있는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM AuthCode a WHERE a.email = :email AND a.isVerified = true " +
           "AND a.expiryTime > :now")
    boolean existsVerifiedByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
}