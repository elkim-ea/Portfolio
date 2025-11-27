package com.matchaworld.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// 스웨거 : http://localhost:8080/swagger-ui/index.html
@SpringBootApplication
@EnableScheduling // 스케줄러 활성화 (인증번호 정리용)
@EnableJpaAuditing
@EnableAsync // ✅ 비동기 기능 활성화
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
