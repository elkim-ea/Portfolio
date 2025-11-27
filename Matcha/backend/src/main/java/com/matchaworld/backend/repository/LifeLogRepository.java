package com.matchaworld.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.matchaworld.backend.domain.LifeLog;
import com.matchaworld.backend.domain.User;

public interface LifeLogRepository extends JpaRepository<LifeLog, Long> {


    List<LifeLog> findByUserOrderByLoggedAtDesc(User user);

    List<LifeLog> findByUserId(Long userId);

    List<LifeLog> findByUserIdAndLoggedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    // ✅ EnumType.STRING 매핑에 맞게 category를 Enum으로 지정
    @Query("SELECT l FROM LifeLog l " +
           "WHERE l.user.id = :userId " +
           "AND l.category = :category " +
           "AND l.loggedAt BETWEEN :start AND :end " +
           "ORDER BY l.loggedAt DESC")
    List<LifeLog> findLogsInPeriod(
            @Param("userId") Long userId,
            @Param("category") LifeLog.Category category,  // ← 변경됨
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT SUM(l.esgScoreEffect) FROM LifeLog l WHERE l.user.id = :userId AND l.category = :category")
        BigDecimal sumScoreByCategory(@Param("userId") Long userId, @Param("category") LifeLog.Category category);

}
