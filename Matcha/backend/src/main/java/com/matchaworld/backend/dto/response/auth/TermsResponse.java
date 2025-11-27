package com.matchaworld.backend.dto.response.auth;

import java.time.LocalDateTime;

import com.matchaworld.backend.domain.Terms;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TermsResponse {
    private Long id;
    private String title;
    private String version;
    private String content;
    private Boolean isRequired;
    private LocalDateTime createdAt;

    public static TermsResponse from(Terms terms) {
        return TermsResponse.builder()
                .id(terms.getId())
                .title(terms.getTitle())
                .version(terms.getVersion())
                .content(terms.getContent())
                .isRequired(terms.getIsRequired())
                .createdAt(terms.getCreatedAt())
                .build();
    }
}