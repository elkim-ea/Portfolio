package com.matchaworld.backend.service.auth;

import java.util.Optional;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TermsService termsService;
    
    /**
     * 회원가입 (약관 동의 포함)
     */
    @Transactional
    public User signup(String nickname, String email, String password, List<Long> agreedTermsIds) {
        // 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 필수 약관 동의 확인
        if (!termsService.hasAgreedToRequiredTerms(agreedTermsIds)) {
            throw new IllegalArgumentException("필수 약관에 모두 동의해야 합니다.");
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);
        
        // 사용자 생성
        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .character("/uploads/character/default.png")
                .esgScore(0)
                .eScore(0)
                .sScore(0)
                .role(User.Role.USER)
                .build();
        
        User savedUser = (User)userRepository.save(user);
        
        log.info("회원가입 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        // 약관 동의 내역 저장
        termsService.saveUserTermsAgreement(savedUser, agreedTermsIds);
        
        return savedUser;
    }

    /**
     * 회원가입 (기존 메서드 - 하위 호환성 유지)
     */
    @Transactional
    public User signup(String nickname, String email, String password) {
        // 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);
        
        // 사용자 생성
        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .character("/uploads/character/default.png")
                .esgScore(0)
                .eScore(0)
                .sScore(0)
                .role(User.Role.USER)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmail());
        
        return savedUser;
    }
    
    /**
     * 로그인 (이메일과 비밀번호 확인)
     */
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        
        log.info("로그인 성공: userId={}, email={}, role={}", user.getId(), user.getEmail(), user.getRole());
        return user;
    }
    
    /**
     * 비밀번호 재설정
     */
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        
        userRepository.save(user);
        log.info("비밀번호 재설정 완료: userId={}, email={}", user.getId(), user.getEmail());
    }
    
    /**
     * 이메일로 사용자 찾기
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * 이메일 중복 체크
     */
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * 닉네임 중복 체크
     */
    public boolean isNicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}