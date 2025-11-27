package com.matchaworld.backend.controller.quest;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.matchaworld.backend.dto.response.quest.QuestResponse;
import com.matchaworld.backend.dto.response.quest.QuestSubmitResponse;
import com.matchaworld.backend.service.JwtService;
import com.matchaworld.backend.service.quest.QuestServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@Slf4j
@RestController
@RequestMapping("/api/quest")
@RequiredArgsConstructor
public class QuestController {

    private final QuestServiceImpl questService;
    private final JwtService jwtService;

    // JWT → userId 추출
    private Long extractUserIdFromToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header 누락");
        }
        return jwtService.extractUserId(header.substring(7));
    }

    // 오늘의 퀘스트
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayQuest(@RequestHeader("Authorization") String auth) {
        Long userId = extractUserIdFromToken(auth);
        QuestResponse quest = questService.getTodayQuest(userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "오늘의 퀘스트 지급 완료", "data", List.of(quest)));
    }

    // 주간 퀘스트
    @GetMapping("/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyQuests(@RequestHeader("Authorization") String auth) {
        Long userId = extractUserIdFromToken(auth);
        List<QuestResponse> quests = questService.getWeeklyQuests(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", quests));
    }

    // 시즌 퀘스트
    @GetMapping("/season")
    public ResponseEntity<Map<String, Object>> getSeasonQuests(@RequestHeader("Authorization") String auth) {
        Long userId = extractUserIdFromToken(auth);
        List<QuestResponse> quests = questService.getSeasonQuests(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", quests));
    }

    // 메인 통합 조회
    @GetMapping("/main")
    public ResponseEntity<Map<String, Object>> getMainQuests(
            @RequestHeader("Authorization") String auth,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {
        Long userId = extractUserIdFromToken(auth);
        Map<String, Object> data = questService.getMainQuests(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    // 퀘스트 완료 처리 (칭호 지급 포함)
    @PostMapping("/{questId}/submit")
    public ResponseEntity<Map<String, Object>> submitQuest(
            @PathVariable Long questId,
            @RequestHeader("Authorization") String auth) {

        Long userId = extractUserIdFromToken(auth);
        QuestSubmitResponse result = questService.submitQuest(questId, userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result.getMessage(),
                "data", result
        ));
    }
}
