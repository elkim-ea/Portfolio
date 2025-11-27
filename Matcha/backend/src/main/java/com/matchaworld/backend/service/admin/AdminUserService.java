package com.matchaworld.backend.service.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matchaworld.backend.domain.User;
import com.matchaworld.backend.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    /**
     * 사용자 목록 조회 (검색, 필터링, 페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String keyword, String role, Pageable pageable) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 키워드 검색 (이메일 또는 닉네임)
            if (keyword != null && !keyword.isEmpty()) {
                Predicate emailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")), 
                    "%" + keyword.toLowerCase() + "%"
                );
                Predicate nicknamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("nickname")), 
                    "%" + keyword.toLowerCase() + "%"
                );
                predicates.add(criteriaBuilder.or(emailPredicate, nicknamePredicate));
            }

            // 권한 필터
            if (role != null && !role.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("role"), User.Role.valueOf(role)));
            }


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable);
    }

    /**
     * 사용자 ID로 조회
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + id));
    }

    /**
     * 사용자 정보 수정
     */
    @Transactional
    public User updateUser(Long id, User updateData) {
        User user = getUserById(id);

        // 수정 가능한 필드만 업데이트
        if (updateData.getNickname() != null) {
            user.setNickname(updateData.getNickname());
        }
        if (updateData.getCharacter() != null) {
            user.setCharacter(updateData.getCharacter());
        }
        if (updateData.getEsgScore() != null) {
            user.setEsgScore(updateData.getEsgScore());
        }
        if (updateData.getEScore() != null) {
            user.setEScore(updateData.getEScore());
        }
        if (updateData.getSScore() != null) {
            user.setSScore(updateData.getSScore());
        }

        return userRepository.save(user);
    }

    /**
     * 사용자 삭제
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        
        // 관리자는 삭제할 수 없도록 체크
        if (user.getRole() == User.Role.ADMIN) {
            throw new IllegalArgumentException("관리자 계정은 삭제할 수 없습니다.");
        }
        
        userRepository.delete(user);
        log.info("사용자 삭제됨: {}", user.getEmail());
    }

    /**
     * 사용자 권한 변경
     */
    @Transactional
    public User changeUserRole(Long id, User.Role newRole) {
        User user = getUserById(id);
        user.setRole(newRole);
        return userRepository.save(user);
    }

    /**
     * 전체 사용자 수 조회
     */
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepository.count();
    }

    /**
     * 권한별 사용자 수 조회
     */
    @Transactional(readOnly = true)
    public long getUserCountByRole(User.Role role) {
        return userRepository.countByRole(role);
    }
}