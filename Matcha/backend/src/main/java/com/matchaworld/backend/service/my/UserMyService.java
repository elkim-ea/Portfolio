package com.matchaworld.backend.service.my;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matchaworld.backend.domain.Title;
import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.domain.UserTitle;
import com.matchaworld.backend.dto.response.my.UserProfileResponse;
import com.matchaworld.backend.repository.TitleRepository;
import com.matchaworld.backend.repository.UserRepository;
import com.matchaworld.backend.repository.UserTitleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMyService {

    private final UserRepository userRepository;
    private final UserTitleRepository userTitleRepository;
    private final TitleRepository titleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 프로필 조회
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 모든 칭호 조회
        List<Title> allTitles = titleRepository.findAll();
        List<UserTitle> userTitles = userTitleRepository.findByUserId(userId);

        // 대표 칭호 조회
        Optional<UserTitle> mainTitle = userTitleRepository.findByUserIdAndIsMainTrue(userId);

        // 칭호 DTO 생성
        List<UserProfileResponse.TitleDTO> titleDTOList = allTitles.stream()
                .map(title -> {
                    Optional<UserTitle> userTitle = userTitles.stream()
                            .filter(ut -> ut.getTitle().getId().equals(title.getId()))
                            .findFirst();

                    return UserProfileResponse.TitleDTO.builder()
                            .titleId(title.getId())
                            .name(title.getName())
                            .description(title.getDescription())
                            .earned(userTitle.isPresent())
                            .earnedAt(userTitle.map(ut
                                    -> ut.getEarnedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            ).orElse(null))
                            .build();
                })
                .collect(Collectors.toList());

        return UserProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .characterImageUrl(user.getCharacter()) // 기존 코드: DB 저장된 경로를 그대로 사용
                // .characterImageUrl(UserProfileResponse.getCharacterImageByScore(user.getEsgScore())) // 수정: ESG 점수 기반으로 캐릭터 이미지 자동 매핑
                .esgScore(user.getEsgScore())
                .eScore(user.getEScore())
                .sScore(user.getSScore())
                .role(user.getRole().name())
                .mainTitleId(mainTitle.map(ut -> ut.getTitle().getId()).orElse(null))
                .mainTitleName(mainTitle.map(ut -> ut.getTitle().getName()).orElse(null))
                .titles(titleDTOList)
                .build();
    }

    /**
     * 닉네임 변경
     */
    @Transactional
    public void updateNickname(Long userId, String currentPassword, String newNickname) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 닉네임 중복 체크
        if (!user.getNickname().equals(newNickname)
                && userRepository.existsByNickname(newNickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        user.setNickname(newNickname);
        userRepository.save(user);
        log.info("닉네임 변경 완료: userId={}, newNickname={}", userId, newNickname);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void updatePassword(Long userId, String currentPassword, String newPassword) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호가 현재 비밀번호와 같은지 확인
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("비밀번호 변경 완료: userId={}", userId);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteAccount(Long userId, String currentPassword) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
        log.info("회원 탈퇴 완료: userId={}, email={}", userId, user.getEmail());
    }

    /**
     * 대표 칭호 설정
     */
    @Transactional
    public void setMainTitle(Long userId, Long titleId) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 기존 대표 칭호 해제
        userTitleRepository.clearMainTitle(userId);

        // 새 대표 칭호 설정 (null이 아닌 경우)
        if (titleId != null) {
            // 칭호를 보유하고 있는지 확인하고 가져오기
            List<UserTitle> userTitles = userTitleRepository.findByUserId(userId);
            UserTitle userTitle = userTitles.stream()
                    .filter(ut -> ut.getTitle().getId().equals(titleId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("보유하지 않은 칭호입니다."));

            userTitle.setIsMain(true);
            userTitleRepository.save(userTitle);

            log.info("대표 칭호 설정: userId={}, titleId={}, titleName={}",
                    userId, titleId, userTitle.getTitle().getName());
        } else {
            log.info("대표 칭호 해제: userId={}", userId);
        }
    }

    // 캐릭터 이미지 업데이트
    @Transactional
    public String updateUserCharacter(Long userId, int score) {
    
        String characterImgUrl = "/uploads/character/default.png";
        if (score < 50) characterImgUrl = "/uploads/character/default.png";
        else if (score < 150) characterImgUrl ="/uploads/character/seed.png";
        else if (score < 300) characterImgUrl ="/uploads/character/flower.png";
        else if (score < 500) characterImgUrl ="/uploads/character/tree.png";
        else if (score < 800) characterImgUrl ="/uploads/character/earth.png";
        else if (score < 1100) characterImgUrl ="/uploads/character/moon.png";
        else if (score < 1500) characterImgUrl ="/uploads/character/star.png";
        else if (score < 2000) characterImgUrl ="/uploads/character/sun.png";
        else if (score < 2500) characterImgUrl ="/uploads/character/wind.png";
        else characterImgUrl = "/uploads/character/cloud.png";

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.setCharacter(characterImgUrl);

        userRepository.save(user);
        log.info("캐릭터 이미지 업데이트 완료: Character={}", user.getCharacter());

        return characterImgUrl;
    }


    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
