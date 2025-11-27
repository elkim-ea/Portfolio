package com.matchaworld.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "COMPANY")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMPANY_ID")
    private Long id;

    @Column(name = "COMPANY_NAME", nullable = false, unique = true, length = 100)
    private String companyName;

    @Column(name = "COMPANY_LOGO", columnDefinition = "TEXT")
    private String companyLogo;

    @Column(name = "COMPANY_WEBSITE_URL", columnDefinition = "TEXT")
    private String companyWebsiteUrl;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    // 카테고리 관계 매핑
    @ManyToMany(fetch = FetchType.LAZY) 
    @JoinTable(
        name = "COMPANY_CATEGORY",
        joinColumns = @JoinColumn(name = "COMPANY_ID"),
        inverseJoinColumns = @JoinColumn(name = "CATEGORY_ID")
    )
    @Builder.Default
    private Set<EsgCategory> categories = new HashSet<>();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 첫 번째 카테고리 이름 반환
    @Transient
    public String getPrimaryCategory() {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        return categories.iterator().next().getCategoryName();
    }

    // 모든 카테고리 이름 반환
    @Transient
    public Set<String> getCategoryNames() {
        if (categories == null) {
            return new HashSet<>();
        }
        return categories.stream()
                .map(EsgCategory::getCategoryName)
                .collect(Collectors.toSet());
    }

    // 카테고리 설정
    public void setCategory(EsgCategory category) {
        if (this.categories == null) {
            this.categories = new HashSet<>();
        }
        this.categories.clear();
        this.categories.add(category);
    }

    // 카테고리 추가
    public void addCategory(EsgCategory category) {
        if (this.categories == null) {
            this.categories = new HashSet<>();
        }
        this.categories.add(category);
    }
}