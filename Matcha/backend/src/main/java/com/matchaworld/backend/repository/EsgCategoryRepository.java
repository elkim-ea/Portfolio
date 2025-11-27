package com.matchaworld.backend.repository;

import com.matchaworld.backend.domain.EsgCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EsgCategoryRepository extends JpaRepository<EsgCategory, Long> {
    Optional<EsgCategory> findByCategoryName(String categoryName);
    boolean existsByCategoryName(String categoryName);
}