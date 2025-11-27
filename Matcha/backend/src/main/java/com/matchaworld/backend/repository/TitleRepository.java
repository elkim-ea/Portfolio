package com.matchaworld.backend.repository;

import com.matchaworld.backend.domain.Title;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleRepository extends JpaRepository<Title, Long>, JpaSpecificationExecutor<Title> {
    
    // 칭호명 중복 체크
    boolean existsByName(String name);
}