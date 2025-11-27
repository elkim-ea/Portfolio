package com.matchaworld.backend.controller.ai;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
// 수정 이유 : 퀘스트 완료 로직에 필요한 ResponseEntity 및 QuestService 사용을 위해 import 추가
import org.springframework.http.ResponseEntity;
import com.matchaworld.backend.service.quest.QuestService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.matchaworld.backend.service.ai.AIService;
import lombok.extern.slf4j.Slf4j;
// import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ai")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class AIController {

    private final AIService aiService;
    // 수정 이유 : AI 분석 후 퀘스트 완료 처리를 위해 QuestService 주입
    private final QuestService questService;

    // 기존 생성자
    // public AIController(AIService aiService) {
    //     this.aiService = aiService;
    // }
    // 수정 이유 : QuestService 추가 주입을 위해 생성자 수정
    @Autowired
    public AIController(AIService aiService, QuestService questService) {
        this.aiService = aiService;
        this.questService = questService;
    }

    // ✅ 이미지 분석 요청 (JSON 단일 응답)
    // @PostMapping(
    //     value = "/image-analysis",
    //     consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
    //     produces = MediaType.APPLICATION_JSON_VALUE
    // )
    // public Mono<String> imageAnalysis(
    //         @RequestParam("question") String question,
    //         @RequestParam(value = "attach", required = false) MultipartFile attach) throws IOException {
    //
    //     log.info("🧠 [AI 분석 요청] question={}, file={}", question,
    //             (attach != null ? attach.getOriginalFilename() : "none"));
    //
    //     if (attach == null || !attach.getContentType().contains("image/")) {
    //         return Mono.just("이미지 파일을 업로드해주세요.");
    //     }
    //
    //     // ✅ 실제 분석 요청
    //     return aiService.imageAnalysis(question, attach.getContentType(), attach.getBytes());
    // }
    // 수정 이유 : 기존 Mono 기반 비동기 응답을 ResponseEntity로 변경하여 동기식 퀘스트 완료 로직과 연결

    @PostMapping(
        value = "/image-analysis",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> imageAnalysis(
            @RequestParam("question") String question,
            @RequestParam("userId") Long userId,
            @RequestParam("questId") Long questId,
            @RequestParam(value = "attach", required = false) MultipartFile attach
    ) throws IOException {

        log.info("🧠 [AI 분석 요청] question={}, file={}", question,
                (attach != null ? attach.getOriginalFilename() : "none"));

        if (attach == null || !attach.getContentType().contains("image/")) {
            return ResponseEntity.badRequest().body("이미지 파일을 업로드해주세요.");
        }

        // 수정 이유 : AIService 내부 비동기 처리 후 동기식 결과 반환을 위해 imageAnalysisBlocking 사용
        String result = aiService.imageAnalysisBlocking(
            question,
            attach.getContentType(),
            attach.getBytes()
        );

        log.info("📄 AI 분석 결과: {}", result);

        // 수정 이유 : AI 분석 결과가 성공일 때 퀘스트 완료 처리 로직 추가
        if (result.toLowerCase().contains("성공") || result.toLowerCase().contains("ok")) {
            questService.submitQuest(questId, userId);
            log.info("🏁 퀘스트 완료 처리됨 → questId={}, userId={}", questId, userId);
        }

        return ResponseEntity.ok(result);
    }
}
