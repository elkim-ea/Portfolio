package com.matchaworld.backend.controller.lifelog;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matchaworld.backend.dto.request.lifelog.LifeLogRequest;
import com.matchaworld.backend.dto.response.lifelog.LifeLogResponse;
import com.matchaworld.backend.service.JwtService;
import com.matchaworld.backend.service.lifelog.LifeLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;

@Slf4j
@RestController
@RequestMapping("/api/lifelog")
@RequiredArgsConstructor
public class LifeLogController {

    private final LifeLogService lifeLogService;
    private final JwtService jwtService;

    /** ✅ JWT 토큰에서 userId 추출 */
    private Long extractUserIdFromToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header가 누락되었습니다.");
        }
        return jwtService.extractUserId(header.substring(7));
    }

    /** ✅ 나의 기록 조회 (전체 or 특정 날짜별) */
    @GetMapping("/me")
    public List<LifeLogResponse> getMyLogs(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String date
    ) {
        return lifeLogService.getLogs(userId, date);
    }

    /** ✅ 기록 추가 (RecordController와 동일한 구조) */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addLog(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, String> body
    ) {
        try {
            Long userId = extractUserIdFromToken(auth);
            String content = body.get("content");

            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "기록 내용이 비어 있습니다."
                ));
            }

            log.info("📝 [기록 요청 수신] userId={}, content={}", userId, content);
            lifeLogService.addLifeLogWithAiAndQuest(userId,
                    new com.matchaworld.backend.dto.request.lifelog.LifeLogRequest(content, null, null));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "기록이 저장되고 AI 분석 및 퀘스트 처리가 완료되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ [기록 처리 오류]: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 내부 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    // @PostMapping
    // public LifeLogResponse addLog(
    //         @AuthenticationPrincipal Long userId,
    //         @RequestBody LifeLogRequest request
    // ) {
    //     return lifeLogService.addLifeLog(userId, request);
    // }

    /** ✅ 기록 수정 */
    @PutMapping("/{logId}")
    public LifeLogResponse updateLog(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long logId,
            @RequestBody LifeLogRequest request
    ) {
        return lifeLogService.updateLifeLog(userId, logId, request);
    }

    /** ✅ 기록 삭제 */
    @DeleteMapping("/{logId}")
    public void deleteLog(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long logId
    ) {
        lifeLogService.deleteLifeLog(userId, logId);
    }
}