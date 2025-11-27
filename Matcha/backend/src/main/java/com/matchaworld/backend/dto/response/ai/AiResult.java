package com.matchaworld.backend.dto.response.ai;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiResult {
    private String category;   // "E" or "S"
    private List<String> keywords;
    private double confidence;
    private String rawText;
}
