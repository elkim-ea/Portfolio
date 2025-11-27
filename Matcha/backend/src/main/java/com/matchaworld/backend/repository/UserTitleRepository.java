package com.matchaworld.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.matchaworld.backend.domain.Title;
import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.domain.UserTitle;

@Repository
public interface UserTitleRepository extends JpaRepository<UserTitle, Long> {
    
    // User 엔티티의 id로 조회
    @Query("SELECT ut FROM UserTitle ut WHERE ut.user.id = :userId")
    List<UserTitle> findByUserId(@Param("userId") Long userId);

    // 사용자가 특정 칭호를 보유하고 있는지 확인
    @Query("SELECT CASE WHEN COUNT(ut) > 0 THEN true ELSE false END FROM UserTitle ut WHERE ut.user.id = :userId AND ut.title.id = :titleId")
    boolean existsByUserIdAndTitleId(@Param("userId") Long userId, @Param("titleId") Long titleId);

    // 대표 칭호 조회
    @Query("SELECT ut FROM UserTitle ut WHERE ut.user.id = :userId AND ut.isMain = true")
    Optional<UserTitle> findByUserIdAndIsMainTrue(@Param("userId") Long userId);

    // 사용자의 모든 대표 칭호 해제
    @Modifying
    @Query("UPDATE UserTitle ut SET ut.isMain = false WHERE ut.user.id = :userId")
    void clearMainTitle(@Param("userId") Long userId);

    // 사용자가 이미 특정 칭호를 보유하고 있는지 여부를 확인
    boolean existsByUserAndTitle(User user, Title title);
}