package com.matchaworld.backend.service.title;

import java.util.List;

import com.matchaworld.backend.domain.Quest;
import com.matchaworld.backend.domain.User;

public interface TitleService {
    
    // 퀘스트 완료 후 새로 획득한 칭호 이름 목록을 반환
    List<String> checkAndGrantTitle(User user, Quest quest);
}
