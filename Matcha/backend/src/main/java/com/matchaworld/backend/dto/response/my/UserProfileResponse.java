package com.matchaworld.backend.dto.response.my;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.matchaworld.backend.domain.User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String characterImageUrl; // 변경: 실제 캐릭터 이미지 경로
    private Integer esgScore;

    @JsonProperty("eScore")
    private Integer eScore;

    @JsonProperty("sScore")
    private Integer sScore;

    private Integer totalScore; // ✅ 추가

    private String role;
    
    // 칭호 관련
    private Long mainTitleId;
    private String mainTitleName;
    private List<TitleDTO> titles;

    @Data
    @Builder
    public static class TitleDTO {
        private Long titleId;
        private String name;
        private String description;
        private boolean earned;
        private String earnedAt;
    }

    // ✅ 점수에 따라 캐릭터 이미지 동적 선택
    // public static String getCharacterImageByScore(int score) {
    //     if (score < 50) return "/uploads/character/default.png";
    //     else if (score < 150) return "/uploads/character/seed.png";
    //     else if (score < 300) return "/uploads/character/flower.png";
    //     else if (score < 500) return "/uploads/character/tree.png";
    //     else if (score < 800) return "/uploads/character/earth.png";
    //     else if (score < 1100) return "/uploads/character/moon.png";
    //     else if (score < 1500) return "/uploads/character/star.png";
    //     else if (score < 2000) return "/uploads/character/sun.png";
    //     else if (score < 2500) return "/uploads/character/wind.png";
    //     else return "/uploads/character/cloud.png";
    // }

    public static UserProfileResponse from(User user) {
        int total = (user.getEsgScore() != null ? user.getEsgScore() : 0)
                  + (user.getEScore() != null ? user.getEScore() : 0)
                  + (user.getSScore() != null ? user.getSScore() : 0);

        return UserProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .characterImageUrl(user.getCharacter())
                .esgScore(user.getEsgScore())
                .eScore(user.getEScore())
                .sScore(user.getSScore())
                .totalScore(total) // ✅ 합산 점수 추가
                .role(user.getRole().name())
                .build();
    }
}
