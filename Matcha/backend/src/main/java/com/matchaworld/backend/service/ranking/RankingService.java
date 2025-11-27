package com.matchaworld.backend.service.ranking;

import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.dto.response.ranking.RankingResponse;
import com.matchaworld.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final UserRepository userRepository;

    /** ✅ 기존 글로벌 랭킹 */
    public List<RankingResponse> getGlobalRanking(int limit) {
        List<User> top = userRepository.findAllByOrderByEsgScoreDesc();
        return IntStream.range(0, Math.min(top.size(), limit))
                .mapToObj(i -> new RankingResponse(
                        i + 1,
                        top.get(i).getNickname(),
                        top.get(i).getEsgScore()
                ))
                .collect(Collectors.toList());
    }

    /** ✅ 나의 랭킹 계산 */
    public RankingResponse getMyRanking(Long userId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        long higher = userRepository.countByEsgScoreGreaterThan(me.getEsgScore());
        return new RankingResponse(
                (int) higher + 1,
                me.getNickname(),
                me.getEsgScore()
        );
    }

    /** ✅ 로그인한 사용자를 최상단에 표시하는 글로벌 랭킹 */
    public List<RankingResponse> getGlobalRankingWithUserOnTop(Long userId, int limit) {
        // 전체 랭킹 가져오기
        List<User> topUsers = userRepository.findAllByOrderByEsgScoreDesc();

        // 로그인한 사용자 정보
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 내 순위 계산
        long higher = userRepository.countByEsgScoreGreaterThan(me.getEsgScore());
        RankingResponse myRanking = new RankingResponse(
                (int) higher + 1,
                me.getNickname(),
                me.getEsgScore()
        );

        // 내 아이디와 일치하는 항목 제외
        List<RankingResponse> globalList = IntStream.range(0, Math.min(topUsers.size(), limit))
                .mapToObj(i -> new RankingResponse(
                        i + 1,
                        topUsers.get(i).getNickname(),
                        topUsers.get(i).getEsgScore()
                ))
                .filter(r -> !r.getNickname().equals(me.getNickname())) // ✅ 중복 방지
                .collect(Collectors.toList());

        // 최상단에 '나' 추가
        List<RankingResponse> result = new ArrayList<>();
        result.add(myRanking);
        result.addAll(globalList);

        return result;
    }
}