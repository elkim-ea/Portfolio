package com.matchaworld.backend.dto.response.quest;

import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestSubmitResponse {
    private String message;  // 퀘스트 완료 메시지
    private int reward;      // 획득한 보상 점수
    private List<String> newTitles; // 새로 얻은 칭호 목록
}
