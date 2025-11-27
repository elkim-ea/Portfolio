package com.matchaworld.backend.service.title;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchaworld.backend.domain.Quest;
import com.matchaworld.backend.domain.Title;
import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.domain.UserQuest;
import com.matchaworld.backend.domain.UserTitle;
import com.matchaworld.backend.repository.TitleRepository;
import com.matchaworld.backend.repository.UserQuestRepository;
import com.matchaworld.backend.repository.UserTitleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TitleServiceImpl implements TitleService {

    // Title, UserTitle, UserQuest 저장소 의존성 주입
    private final TitleRepository titleRepository;
    private final UserTitleRepository userTitleRepository;
    private final UserQuestRepository userQuestRepository;

    // JSON 파싱용 ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 사용자의 퀘스트 완료 내역을 검사하여 조건을 충족한 경우 칭호를 지급하고 획득한 칭호 목록 반환
    @Override
    @Transactional
    public List<String> checkAndGrantTitle(User user, Quest quest) {
        List<String> newTitles = new ArrayList<>();
        try {
            List<Title> titles = titleRepository.findAll();

            for (Title title : titles) {
                Map<String, Object> condition = objectMapper.readValue(title.getConditionJson(), Map.class);

                // 어떤 칭호와 어떤 퀘스트를 비교 중인지 표시
                log.info("칭호 검사 시작 → titleName={}, questTitle={}, conditionQuestTitle={}",
                        title.getName(), quest.getTitle(), condition.get("questTitle"));

                // questTitle 불일치 시 이유 출력
                if (condition.containsKey("questTitle")
                        && !condition.get("questTitle").equals(quest.getTitle())) {
                    log.info("❌ 불일치: [{}] 조건='{}' / 실제='{}'",
                            title.getName(), condition.get("questTitle"), quest.getTitle());
                    continue;
                }

                if (condition.containsKey("count")) {
                    int targetCount = (int) condition.get("count");
                    long completedCount = userQuestRepository
                            .countByUserAndQuest_TitleAndStatus(user, quest.getTitle(), UserQuest.Status.SUCCESS);

                    // 횟수 비교 결과 출력
                    log.info("칭호 조건 검사 → [{}] 목표={}회 / 현재={}회",
                            title.getName(), targetCount, completedCount);

                    if (completedCount >= targetCount && grantTitle(user, title)) {
                        newTitles.add(title.getName());
                        log.info("✅ 칭호 지급 성공 → userId={}, title={}", user.getId(), title.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[ERROR] 칭호 지급 중 오류 발생: {}", e.getMessage());
        }
        return newTitles;
    }

    // 사용자가 아직 보유하지 않은 칭호를 지급하고 UserTitle에 저장
    // true - 새로 지급됨, false - 이미 보유 중
    private boolean grantTitle(User user, Title title) {
        // 이미 보유 중인지 검사
        boolean alreadyOwned = userTitleRepository.existsByUserAndTitle(user, title);
        if (!alreadyOwned) {
            // 신규 칭호 등록
            UserTitle ut = UserTitle.builder()
                    .user(user)
                    .title(title)
                    .earnedAt(LocalDateTime.now())
                    .isMain(false)
                    .build();

            userTitleRepository.save(ut);
            log.info("[INFO] 칭호 지급 완료 → userId={}, title={}", user.getId(), title.getName());
            return true;
        }
        return false;
    }
}
