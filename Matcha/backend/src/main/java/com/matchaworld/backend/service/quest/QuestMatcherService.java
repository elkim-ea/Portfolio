package com.matchaworld.backend.service.quest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matchaworld.backend.domain.Certification;
import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.domain.UserQuest;
import com.matchaworld.backend.dto.response.ai.AiResult;
import com.matchaworld.backend.dto.response.quest.QuestResponse;
import com.matchaworld.backend.dto.response.quest.QuestSubmitResponse;
import com.matchaworld.backend.repository.CertificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ✅ QuestMatcherService (지능형 + 매칭 점수 로깅 버전)
 * - AI 분석 결과 기반으로 자동 퀘스트 완료를 처리
 * - 모든 퀘스트의 매칭 점수를 로그로 출력하여 디버깅 가능
 * - 가장 높은 점수의 퀘스트 1개만 자동완료 (점수 2.0 이상일 때)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestMatcherService {

    private final QuestService questService;
    private final CertificationRepository certificationRepository;

    @Transactional
    public void matchAndComplete(Long userId, AiResult result) {

        synchronized (userId.toString().intern()) {
            List<String> keywords = Optional.ofNullable(result.getKeywords()).orElse(List.of());
            if (keywords.isEmpty()) {
                log.info("⚠️ [자동완료 스킵] 키워드 없음 → {}", result);
                return;
            }

            List<QuestResponse> userQuests = questService.getUserQuests(userId);
            if (userQuests.isEmpty()) {
                log.info("⚠️ [자동완료 스킵] 진행 중 퀘스트 없음");
                return;
            }

            // ✅ 각 퀘스트별 매칭 점수 계산
            Map<QuestResponse, Double> scored = new HashMap<>();
            for (QuestResponse quest : userQuests) {
                double score = calculateMatchScore(quest, result);
                scored.put(quest, score);
                log.info("📊 [매칭 점수 계산] quest='{}', 점수={}, keywords={}", 
                        quest.getTitle(), String.format("%.2f", score), result.getKeywords());
            }

            // ✅ 최고 점수 퀘스트 선택 (최소 2점 이상일 때만 자동완료)
            QuestResponse bestMatch = scored.entrySet().stream()
                    .filter(e -> e.getValue() >= 2.0)
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (bestMatch == null) {
                log.info("🤔 [자동완료 없음] 어떤 퀘스트도 충분히 일치하지 않음 (최고 점수={})",
                        String.format("%.2f", scored.values().stream().max(Double::compareTo).orElse(0.0)));
                return;
            }

            // ✅ 중복 완료 방지
            if (!UserQuest.Status.PENDING.equals(bestMatch.getStatus())) {
                log.info("⚠️ [자동완료 스킵] 이미 완료된 퀘스트 → {}", bestMatch.getTitle());
                return;
            }

            // ✅ 자동완료 실행
            try {
                double finalScore = scored.get(bestMatch);
                log.info("🎯 [자동완료 매칭 성공] '{}' → questId={}, userId={}, 매칭점수={}",
                        bestMatch.getTitle(), bestMatch.getQuestId(), userId, String.format("%.2f", finalScore));

                QuestSubmitResponse response = questService.submitQuest(bestMatch.getQuestId(), userId);
                log.info("🏆 [퀘스트 완료 성공] userId={}, questId={}, message={}, +{}점",
                        userId, bestMatch.getQuestId(), response.getMessage(), response.getReward());

                // 인증 로그 저장
                String safeAuthContent = Optional.ofNullable(result.getRawText())
                        .filter(s -> !s.isBlank())
                        .orElse("(내용 없음)");

                Certification cert = Certification.builder()
                        .userQuest(UserQuest.builder().id(bestMatch.getUserQuestId()).build())
                        .user(User.builder().id(userId).build())
                        .authType(Certification.AuthType.TEXT)
                        .authContent(safeAuthContent)
                        .validationStatus(Certification.ValidationStatus.SUCCESS)
                        .validatedAt(LocalDateTime.now())
                        .modelType(Certification.ModelType.OPENAPI)
                        .confidenceScore(result.getConfidence())
                        .build();

                certificationRepository.save(cert);
                log.info("🪪 [인증 로그 저장 완료] questId={}, userId={}, content={}",
                        bestMatch.getQuestId(), userId, safeAuthContent);

            } catch (Exception e) {
                log.error("⚠️ [퀘스트 자동완료 실패] questId={}, userId={}, error={}",
                        bestMatch.getQuestId(), userId, e.getMessage());
            }
        }
    }

    /**
     * 🧠 퀘스트-기록 매칭 점수 계산식 (디버그 로그 포함)
     */
    private double calculateMatchScore(QuestResponse quest, AiResult result) {
        List<String> keywords = Optional.ofNullable(result.getKeywords()).orElse(List.of());
        String title = normalize(quest.getTitle());
        String desc = normalize(Optional.ofNullable(quest.getDescription()).orElse(""));
        String aiCategory = Optional.ofNullable(result.getCategory()).orElse("").toLowerCase();

        double score = 0.0;

        // 1️⃣ 키워드 일치 점수 (title 2점, desc 1점)
        for (String kw : keywords) {
            String keyword = normalize(kw);
            if (title.contains(keyword)) {
                score += 2.0;
                log.debug("🔹 title 매칭: '{}' ⟶ '{}'", quest.getTitle(), keyword);
            } else if (desc.contains(keyword)) {
                score += 1.0;
                log.debug("🔸 desc 매칭: '{}' ⟶ '{}'", quest.getTitle(), keyword);
            }
        }

        // 2️⃣ 카테고리 일치 보너스 (예: AI category=E 이고 desc에 '환경' 포함 등)
        if (!aiCategory.isBlank() && desc.contains(aiCategory)) {
            score += 0.5;
            log.debug("💚 category 매칭: '{}' 카테고리 '{}' 포함", quest.getTitle(), aiCategory);
        }

        // 3️⃣ 제목-키워드 부분 일치율 보정
        long partial = keywords.stream().filter(k -> title.contains(normalize(k))).count();
        score += 0.3 * partial;

        return score;
    }

    private String normalize(String text) {
        return text.replaceAll("\\s+", "").trim().toLowerCase();
    }
}
