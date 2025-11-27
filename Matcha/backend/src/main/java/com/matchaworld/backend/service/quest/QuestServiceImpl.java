package com.matchaworld.backend.service.quest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchaworld.backend.domain.Quest;
import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.domain.UserQuest;
import com.matchaworld.backend.dto.response.quest.QuestResponse;
import com.matchaworld.backend.dto.response.quest.QuestSubmitResponse;
import com.matchaworld.backend.mapper.QuestMapper;
import com.matchaworld.backend.repository.QuestRepository;
import com.matchaworld.backend.repository.UserQuestRepository;
import com.matchaworld.backend.service.title.TitleService;
import com.matchaworld.backend.weather.WeatherClient;
import com.matchaworld.backend.weather.WeatherInfo;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestServiceImpl implements QuestService {

    private final QuestRepository questRepository;
    private final UserQuestRepository userQuestRepository;
    private final EntityManager em;
    private final WeatherClient weatherClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TitleService titleService;

    // 오늘의 퀘스트 조회
    @Transactional
    public QuestResponse getTodayQuest(Long userId) {
        List<UserQuest> today = userQuestRepository.findByUserIdAndQuestType(userId, Quest.Type.DAILY);
        if (!today.isEmpty()) {
            log.debug("[DEBUG] 이미 DAILY 퀘스트 존재함 → {}", today.get(0).getQuest().getTitle());
            return QuestMapper.toResponse(today.get(0));
        }
        return assignWeatherDailyQuest(userId, 37.5665, 126.9780);
    }

    // 주간 퀘스트 조회
    @Transactional
    public List<QuestResponse> getWeeklyQuests(Long userId) {
        List<UserQuest> weekly = userQuestRepository.findByUserIdAndQuestType(userId, Quest.Type.WEEKLY);
        if (weekly.isEmpty()) {
            log.debug("[DEBUG] 주간 퀘스트 없음 → 랜덤 지급 실행");
            assignRandomQuests(userId, Quest.Type.WEEKLY);
            weekly = userQuestRepository.findByUserIdAndQuestType(userId, Quest.Type.WEEKLY);
        }
        return weekly.stream().map(QuestMapper::toResponse).toList();
    }

    // 시즌 퀘스트 조회
    @Transactional
    public List<QuestResponse> getSeasonQuests(Long userId) {
        List<UserQuest> season = userQuestRepository.findByUserIdAndQuestType(userId, Quest.Type.SEASON);
        if (season.isEmpty()) {
            log.debug("[DEBUG] 시즌 퀘스트 없음 → 랜덤 지급 실행");
            assignRandomQuests(userId, Quest.Type.SEASON);
            season = userQuestRepository.findByUserIdAndQuestType(userId, Quest.Type.SEASON);
        }
        return season.stream().map(QuestMapper::toResponse).toList();
    }

    // 메인 퀘스트 통합 조회
    @Transactional(readOnly = true)
    public Map<String, Object> getMainQuests(Long userId) {
        Map<String, Object> data = new HashMap<>();
        QuestResponse today = getTodayQuest(userId);
        List<QuestResponse> weekly = getWeeklyQuests(userId);
        List<QuestResponse> season = getSeasonQuests(userId);
        List<QuestResponse> progress = new ArrayList<>();
        progress.addAll(weekly);
        progress.addAll(season);
        data.put("today", List.of(today));
        data.put("weekly", weekly);
        data.put("season", season);
        data.put("progress", progress);
        return data;
    }

    // 랜덤 퀘스트 지급
    @Transactional
    public void assignRandomQuests(Long userId, Quest.Type type) {
        List<Quest> availableQuests = questRepository.findByTypeAndIsActive(type, true);
        if (availableQuests.isEmpty()) {
            log.warn("[WARN] 활성화된 {} 퀘스트 없음", type);
            return;
        }
        List<Long> ownedQuestIds = userQuestRepository.findByUserIdAndQuestType(userId, type)
                .stream().map(uq -> uq.getQuest().getId()).toList();

        List<Quest> notOwned = new ArrayList<>(availableQuests.stream()
                .filter(q -> !ownedQuestIds.contains(q.getId()))
                .toList());
        if (notOwned.isEmpty()) {
            notOwned.addAll(availableQuests);
        }

        Collections.shuffle(notOwned);
        notOwned.stream().limit(5).forEach(q -> {
            UserQuest uq = new UserQuest();
            uq.setUser(em.getReference(User.class, userId));
            uq.setQuest(q);
            uq.setStatus(UserQuest.Status.PENDING);
            uq.setStartedAt(LocalDateTime.now());
            userQuestRepository.save(uq);
        });
        log.debug("[DEBUG] {} 퀘스트 랜덤 지급 완료", type);
    }

    // 날씨 기반 일일 퀘스트 지급
    @Transactional
    public QuestResponse assignWeatherDailyQuest(Long userId, Double lat, Double lon) {
        WeatherInfo weather = weatherClient.getCurrentWeather(lat, lon);
        if (weather == null) {
            return assignRandomDailyFallback(userId);
        }
        double temp = weather.getTemperature();
        double humidity = weather.getHumidity();
        double pm10 = weather.getPm10();
        List<Quest> dailyList = questRepository.findByTypeAndIsActive(Quest.Type.DAILY, true);
        if (dailyList.isEmpty()) {
            return assignRandomDailyFallback(userId);
        }

        Map<Quest, Integer> scored = new HashMap<>();
        for (Quest quest : dailyList) {
            try {
                if (quest.getConditionJson() == null || quest.getConditionJson().isBlank()) {
                    scored.put(quest, 0);
                    continue;
                }
                Map<String, Object> cond = objectMapper.readValue(quest.getConditionJson(), Map.class);
                int score = 0;
                if (cond.containsKey("temp_min") && temp >= ((Number) cond.get("temp_min")).doubleValue()) {
                    score++;
                }
                if (cond.containsKey("temp_max") && temp <= ((Number) cond.get("temp_max")).doubleValue()) {
                    score++;
                }
                if (cond.containsKey("humidity_min") && humidity >= ((Number) cond.get("humidity_min")).doubleValue()) {
                    score++;
                }
                if (cond.containsKey("humidity_max") && humidity <= ((Number) cond.get("humidity_max")).doubleValue()) {
                    score++;
                }
                if (cond.containsKey("pm10_max") && pm10 <= ((Number) cond.get("pm10_max")).doubleValue()) {
                    score++;
                }
                scored.put(quest, score);
            } catch (Exception e) {
                log.warn("[WARN] CONDITION_JSON 파싱 실패: {}", quest.getTitle());
            }
        }

        Quest selected = scored.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseGet(() -> dailyList.get(new Random().nextInt(dailyList.size())));

        UserQuest uq = new UserQuest();
        uq.setUser(em.getReference(User.class, userId));
        uq.setQuest(selected);
        uq.setStatus(UserQuest.Status.PENDING);
        uq.setStartedAt(LocalDateTime.now());
        userQuestRepository.save(uq);

        return QuestMapper.toResponse(selected);
    }

    // 랜덤 대체 일일 퀘스트 지급
    @Transactional
    public QuestResponse assignRandomDailyFallback(Long userId) {
        List<Quest> list = questRepository.findByTypeAndIsActive(Quest.Type.DAILY, true);
        Quest random = list.get(new Random().nextInt(list.size()));
        UserQuest uq = new UserQuest();
        uq.setUser(em.getReference(User.class, userId));
        uq.setQuest(random);
        uq.setStatus(UserQuest.Status.PENDING);
        uq.setStartedAt(LocalDateTime.now());
        userQuestRepository.save(uq);
        return QuestMapper.toResponse(random);
    }

    // 퀘스트 완료 처리 및 칭호 지급 후 결과 반환
    @Transactional
    public QuestSubmitResponse submitQuest(Long questId, Long userId) {
        UserQuest uq = userQuestRepository.findByUserIdAndQuestId(userId, questId)
                .orElseThrow(() -> new RuntimeException("해당 퀘스트를 찾을 수 없습니다."));

        if (uq.getStatus() == UserQuest.Status.SUCCESS) {
            log.info("[INFO] 이미 완료된 퀘스트입니다 → questId={}, userId={}", questId, userId);
            return new QuestSubmitResponse("이미 완료된 퀘스트입니다.", 0, List.of());
        }

        // uq.setStatus(UserQuest.Status.SUCCESS);
        // uq.setCompletedAt(LocalDateTime.now());
        // userQuestRepository.save(uq);
        // 시도 횟수 증가
        uq.setAttemptCount(uq.getAttemptCount() + 1);
        Quest quest = uq.getQuest();

        // maxAttempts 도달 시만 SUCCESS 처리
        if (uq.getAttemptCount() >= quest.getMaxAttempts()) {
            uq.setStatus(UserQuest.Status.SUCCESS);
            uq.setCompletedAt(LocalDateTime.now());
        }

        userQuestRepository.save(uq);

        Quest.Type questType = uq.getQuest().getType();
        int reward = switch (questType) {
            case DAILY ->
                10;
            case WEEKLY ->
                50;
            case SEASON ->
                200;
            default ->
                0;
        };

        // ✅ User 점수 갱신 (merge → dirty checking 자동 반영으로 변경)
        User user = em.find(User.class, userId);
        if (user != null) {
            // ✅ 퀘스트 점수 누적
            int newEsgScore = (user.getEsgScore() != null ? user.getEsgScore() : 0) + reward;
            user.setEsgScore(newEsgScore);
            log.info("🌱 [ESG 점수 갱신 완료] userId={}, +{}, 총점={}", userId, reward, newEsgScore);
        }
        // 칭호 지급
        List<String> newTitles = titleService.checkAndGrantTitle(user, uq.getQuest());
        em.flush();
        log.info("[INFO] 퀘스트 완료 → userId={}, +{}점, newTitles={}", userId, reward, newTitles);
        return new QuestSubmitResponse("퀘스트 완료 성공", reward, newTitles);
    }

    // 유저 퀘스트 조회
    @Override
    @Transactional(readOnly = true)
    public List<QuestResponse> getUserQuests(Long userId) {
        List<UserQuest> list = userQuestRepository.findByUser_Id(userId);
        return list.stream().map(QuestMapper::toResponse).toList();
    }

    // 기간 만료 퀘스트 자동 비활성화
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deactivateExpiredQuests() {
        List<Quest> active = questRepository.findByIsActive(true);
        LocalDateTime now = LocalDateTime.now();
        for (Quest quest : active) {
            boolean deactivate = switch (quest.getType()) {
                case DAILY ->
                    quest.getCreatedAt().isBefore(now.minusDays(1));
                case WEEKLY ->
                    quest.getCreatedAt().isBefore(now.minusDays(7));
                case SEASON ->
                    quest.getCreatedAt().isBefore(now.minusMonths(6));
            };
            if (deactivate) {
                quest.setIsActive(false);
                questRepository.save(quest);
                log.info("[INFO] 퀘스트 자동 비활성화 → {}", quest.getTitle());
            }
        }
    }

    // 시즌 종료 시 사용자 퀘스트 초기화
    @Transactional
    public void resetSeasonUserQuests() {
        List<UserQuest> seasonQuests = userQuestRepository.findByQuestType(Quest.Type.SEASON);
        seasonQuests.forEach(userQuestRepository::delete);
    }

    // MAX_ATTEMPTS 도달 시 자동 완료 처리
    @Override
    @Transactional
    public void checkAndCompleteQuest(Long userQuestId) {
        UserQuest uq = userQuestRepository.findById(userQuestId)
                .orElseThrow(() -> new RuntimeException("UserQuest not found"));
        Quest quest = uq.getQuest();

        uq.setAttemptCount(uq.getAttemptCount() + 1);
        if (uq.getAttemptCount() >= quest.getMaxAttempts()) {
            uq.setStatus(UserQuest.Status.SUCCESS);
            uq.setCompletedAt(LocalDateTime.now());
            log.info("[INFO] 퀘스트 자동 완료 → questId={}, userQuestId={}", quest.getId(), userQuestId);
        }
        userQuestRepository.save(uq);
    }
}
