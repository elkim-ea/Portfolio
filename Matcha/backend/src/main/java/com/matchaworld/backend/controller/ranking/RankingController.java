package com.matchaworld.backend.controller.ranking;

import com.matchaworld.backend.dto.response.ranking.RankingResponse;
import com.matchaworld.backend.service.JwtService;
import com.matchaworld.backend.service.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;
    private final JwtService jwtService;

    /** ✅ 로그인한 유저를 최상단에 포함한 글로벌 랭킹 */
    @GetMapping("/global")
    public List<RankingResponse> getGlobalWithUserOnTop(
            HttpServletRequest request,
            @RequestParam(defaultValue = "100") int limit
    ) {
        Long userId = extractUserIdFromRequest(request);
        return rankingService.getGlobalRankingWithUserOnTop(userId, limit);
    }

    /** ✅ 내 랭킹 단독 조회 */
    @GetMapping("/me")
    public RankingResponse getMyRanking(HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        return rankingService.getMyRanking(userId);
    }

    /** ✅ 토큰에서 userId 추출 */
    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization 헤더가 없습니다.");
        }
        String token = header.substring(7);
        return jwtService.getUserIdFromToken(token);
    }
}