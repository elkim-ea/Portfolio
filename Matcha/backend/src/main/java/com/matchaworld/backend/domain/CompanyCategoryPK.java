package com.matchaworld.backend.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCategoryPK implements Serializable {
    private Long company; // Entity 필드 이름과 일치
    private Long category; // Entity 필드 이름과 일치
}