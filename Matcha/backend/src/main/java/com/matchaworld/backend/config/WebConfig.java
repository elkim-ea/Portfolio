package com.matchaworld.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     // SecurityConfig의 CORS 설정만으로는 OPTIONS 응답 헤더가 누락되어 403 발생
    //     registry.addMapping("/**")
    //             .allowedOrigins(
    //                     "http://34.64.88.163",      // 🔥 GKE Frontend LB Origin 추가
    //                     "http://localhost:5173",
    //                     "http://127.0.0.1:5173",
    //                     "http://localhost:3000",
    //                     "http://127.0.0.1:3000"
    //             )
    //             .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
    //             .allowedHeaders("Authorization", "Content-Type")
    //             .exposedHeaders("Authorization", "Content-Type")
    //             .allowCredentials(true)
    //             .maxAge(3600);
    // }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ 실행 환경에 따라 동적 경로 결정
        String uploadPath;

        if (isDockerEnvironment()) {
            uploadPath = "file:/app/uploads/"; // 컨테이너 내부
        } else {
            // 로컬 개발 환경 기준 경로
            uploadPath = "file:" + Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath() + "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }

    // ✅ 단순한 Docker 환경 감지 (필요 시 개선 가능)
    private boolean isDockerEnvironment() {
        return System.getenv("DOCKER_ENV") != null
                || System.getenv("container") != null
                || (System.getenv("HOSTNAME") != null && System.getenv("HOSTNAME").contains("matcha"));
    }
}
