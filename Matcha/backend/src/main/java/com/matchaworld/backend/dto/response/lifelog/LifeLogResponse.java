package com.matchaworld.backend.dto.response.lifelog;

import com.matchaworld.backend.domain.LifeLog;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LifeLogResponse {

    private Long logId;
    private String content;
    @Builder.Default
    private LifeLog.Category category = LifeLog.Category.E;
    private LocalDateTime loggedAt;
    private BigDecimal esgScoreEffect;

    public static LifeLogResponse fromEntity(LifeLog log) {
        return LifeLogResponse.builder()
                .logId(log.getId())
                .content(log.getContent())
                .category(log.getCategory())
                .loggedAt(log.getLoggedAt())
                .esgScoreEffect(log.getEsgScoreEffect())
                .build();
    }
}