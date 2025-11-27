// package com.matchaworld.backend.controller.activity;

// import java.time.LocalDateTime;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;

// import com.matchaworld.backend.domain.LifeLog;
// import com.matchaworld.backend.domain.User;
// import com.matchaworld.backend.dto.response.activity.ActivityResponse;
// import com.matchaworld.backend.dto.response.dailyscore.DailyScoreResponse;
// import com.matchaworld.backend.repository.UserRepository;
// import com.matchaworld.backend.service.activity.ActivityService;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// @RestController
// @RequestMapping("/api/activity")
// @RequiredArgsConstructor
// @Slf4j
// public class ActivityController {

//     private final ActivityService activityService;
//     private final UserRepository userRepository;

//     /**
//      * ✅ 내 ESG 종합 점수 + 캐릭터 정보 조회 (JWT 기반)
//      * - 마이페이지 점수 로직과 동일하게 UserRepository에서 최신 점수를 가져옴
//      * - ActivityService.updateUserScores(userId)로 E/S/ESG 동기화
//      */
//     @GetMapping("/me")
//     public ActivityResponse getMyInfo(@AuthenticationPrincipal Long userId) {
//         activityService.updateUserScores(userId);  // ← 추가

//         // 🧩 최신 사용자 정보 조회
//         User user = userRepository.findById(userId)
//                 .map(u -> {
//                         userRepository.flush(); // 영속성 컨텍스트 flush
//                         return userRepository.getReferenceById(userId);
//                 })
//                 .orElseThrow(() -> new RuntimeException("User not found"));

//         log.info("🌱 [활동 점수 조회] userId={}, ESG={}, E={}, S={}",
//                 userId, user.getEsgScore(), user.getEScore(), user.getSScore());

//         // ✅ ActivityResponse 빌더로 단순 변환
//         String characterPath = user.getCharacter();
//         String characterUrl = (characterPath != null && characterPath.startsWith("/uploads/"))
//                 ? characterPath
//                 : "/uploads/character/" + (characterPath != null ? characterPath : "flower.png");

//         return ActivityResponse.builder()
//                 .totalScore(user.getEsgScore())  // ✅ ESG_SCORE = 총점
//                 .eScore(user.getEScore())
//                 .sScore(user.getSScore())
//                 .characterUrl(characterUrl)
//                 .build();
//     }

//     /**
//      * ✅ ESG 활동 통계 + 로그 (E=7일, S=30일)
//      */
//     @GetMapping
//     public Map<String, Object> getActivitySummary(
//             @AuthenticationPrincipal Long userId,
//             @RequestParam(defaultValue = "7") int eDays,
//             @RequestParam(defaultValue = "30") int sDays
//     ) {
//         User user = userRepository.findById(userId)
//                 .orElseThrow(() -> new RuntimeException("User not found"));

//         Map<String, Object> result = new HashMap<>();
        
//         // ✅ 점수 및 캐릭터 URL
//         result.put("totalScore", user.getEsgScore()); // ✅ 총점 (ESG_SCORE)
//         result.put("eScore", user.getEScore());
//         result.put("sScore", user.getSScore());
//         result.put("characterUrl", user.getCharacter());

//         LocalDateTime now = LocalDateTime.now();

//         // ✅ E 활동 데이터 (최근 eDays일)
//         LocalDateTime eStart = now.minusDays(eDays);
//         List<DailyScoreResponse> eScores = activityService.getScoresByPeriod(
//                 userId, LifeLog.Category.E, eStart, now);
//         result.put("eWeeklyData", Map.of(
//                 "labels", eScores.stream().map(DailyScoreResponse::getDate).collect(Collectors.toList()),
//                 "scores", eScores.stream().map(DailyScoreResponse::getScore).collect(Collectors.toList())
//         ));

//         // ✅ S 활동 데이터 (최근 sDays일)
//         LocalDateTime sStart = now.minusDays(sDays);
//         List<DailyScoreResponse> sScores = activityService.getScoresByPeriod(
//                 userId, LifeLog.Category.S, sStart, now);
//         result.put("sMonthlyData", Map.of(
//                 "labels", sScores.stream().map(DailyScoreResponse::getDate).collect(Collectors.toList()),
//                 "scores", sScores.stream().map(DailyScoreResponse::getScore).collect(Collectors.toList())
//         ));

//         // ✅ 최근 로그 (limit 제거)
//         result.put("eRecentLogs", activityService.getRecentLogs(userId, LifeLog.Category.E, Integer.MAX_VALUE));
//         result.put("sRecentLogs", activityService.getRecentLogs(userId, LifeLog.Category.S, Integer.MAX_VALUE));

//         return result;
//     }
// }

package com.matchaworld.backend.controller.activity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.matchaworld.backend.domain.LifeLog;
import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.dto.response.activity.ActivityResponse;
import com.matchaworld.backend.dto.response.dailyscore.DailyScoreResponse;
import com.matchaworld.backend.repository.UserRepository;
import com.matchaworld.backend.service.activity.ActivityService;
import com.matchaworld.backend.service.my.UserMyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
@Slf4j
public class ActivityController {

    private final ActivityService activityService;
    private final UserRepository userRepository;
    private final UserMyService userMyService;

    /**
     * ✅ 내 ESG 종합 점수 + 캐릭터 정보 조회 (E/S + 퀘스트 합산)
     */
    @GetMapping("/me")
    public ActivityResponse getMyInfo(@AuthenticationPrincipal Long userId) {

        // 🆕 수정: 점수 동기화 복원
        activityService.updateUserScores(userId);

        User user = userRepository.findById(userId)
                .map(u -> {
                    userRepository.flush(); // 🆕 flush 유지
                    return userRepository.getReferenceById(userId);
                })
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Integer> scores = activityService.calculateUserScores(userId);
        int e = scores.get("eScore");
        int s = scores.get("sScore");
        int questScore = user.getEsgScore() != null ? user.getEsgScore() : 0;
        int total = questScore + e + s;

        String characterUrl = userMyService.updateUserCharacter(userId, scores.get("esgScore"));

        // String characterPath = user.getCharacter();
        // String characterUrl = (characterPath != null && characterPath.startsWith("/uploads/"))
        //         ? characterPath
        //         : "/uploads/character/" + (characterPath != null ? characterPath : "flower.png");

        log.info("🌱 [활동 점수 조회] userId={}, total={}, quest={}, E={}, S={}",
                userId, total, questScore, e, s);

        return ActivityResponse.builder()
                .totalScore(total)
                .esgScore(user.getEsgScore()) // 🆕 프론트 대응 필드 추가
                .eScore(e)
                .sScore(s)
                .characterUrl(characterUrl)
                .build();
    }

    /**
     * ✅ ESG 활동 통계 + 로그 (E=7일, S=30일)
     */
    @GetMapping
    public Map<String, Object> getActivitySummary(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "7") int eDays,
            @RequestParam(defaultValue = "30") int sDays
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Integer> scores = activityService.calculateUserScores(userId);
        int e = scores.get("eScore");
        int s = scores.get("sScore");
        int questScore = user.getEsgScore() != null ? user.getEsgScore() : 0;
        int total = questScore + e + s;

        Map<String, Object> result = new HashMap<>();
        result.put("totalScore", total);
        result.put("esgScore", user.getEsgScore()); // 🆕 프론트 대응
        result.put("eScore", e);
        result.put("sScore", s);
        result.put("characterUrl", user.getCharacter());

        LocalDateTime now = LocalDateTime.now();

        // ✅ E 활동 데이터 (최근 eDays일)
        LocalDateTime eStart = now.minusDays(eDays);
        List<DailyScoreResponse> eScores = activityService.getScoresByPeriod(
                userId, LifeLog.Category.E, eStart, now);
        result.put("eWeeklyData", Map.of(
                "labels", eScores.stream().map(DailyScoreResponse::getDate).collect(Collectors.toList()),
                "scores", eScores.stream().map(DailyScoreResponse::getScore).collect(Collectors.toList())
        ));

        // ✅ S 활동 데이터 (최근 sDays일)
        LocalDateTime sStart = now.minusDays(sDays);
        List<DailyScoreResponse> sScores = activityService.getScoresByPeriod(
                userId, LifeLog.Category.S, sStart, now);
        result.put("sMonthlyData", Map.of(
                "labels", sScores.stream().map(DailyScoreResponse::getDate).collect(Collectors.toList()),
                "scores", sScores.stream().map(DailyScoreResponse::getScore).collect(Collectors.toList())
        ));

        // ✅ 최근 로그
        result.put("eRecentLogs", activityService.getRecentLogs(userId, LifeLog.Category.E, Integer.MAX_VALUE));
        result.put("sRecentLogs", activityService.getRecentLogs(userId, LifeLog.Category.S, Integer.MAX_VALUE));

        return result;
    }
}

