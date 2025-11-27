package com.matchaworld.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.matchaworld.backend.domain.Company;

public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {

    // 카테고리명(LEADER/SPONSOR)으로 기업 조회
    @Query("""
        SELECT DISTINCT c
        FROM Company c
        JOIN c.categories ec
        WHERE ec.categoryName = :categoryName
    """)
    List<Company> findByCategoryName(@Param("categoryName") String categoryName);

    boolean existsByCompanyName(String companyName);
}
