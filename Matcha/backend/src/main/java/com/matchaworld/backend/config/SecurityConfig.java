package com.matchaworld.backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.matchaworld.backend.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] WHITE_LIST_URLS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",

            "/auth/**",
            "/api/auth/**",

            "/api/weather/**",
            "/api/ai/**",
            // "/api/record/**",

            "/uploads/**",
            "/css/**",
            "/js/**",
            "/images/**"
    };


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // CSRF 사용 안 함 (JWT 기반)
            .csrf(csrf -> csrf.disable())

            // ⭐ CORS 활성화 (아래 Bean 사용)
            .cors(Customizer.withDefaults())

            // 세션 미사용
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // URL 권한 설정
            .authorizeHttpRequests(req -> req
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(WHITE_LIST_URLS).permitAll()
                    .requestMatchers("/uploads/**", "/css/**", "/js/**", "/images/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/record/**").hasAnyRole("USER", "ADMIN")
                    .anyRequest().authenticated()
            )

            // JWT 필터
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // ⭐ 인증 정보 포함 허용
        config.setAllowCredentials(true);

        // ⭐ Spring Security 6 필수 - allowedOriginPatterns 대체
        config.setAllowedOrigins(List.of(
                // ===== AWS 운영 =====
                "https://matchaworld.shop",
                "https://www.matchaworld.shop"

                // // ===== NCP 프론트 (운영/테스트용) =====
                // "http://*.kr.lb.naverncp.com",
                // "https://*.kr.lb.naverncp.com",

                // // ===== 로컬 개발 =====
                // "http://localhost:5173",
                // "http://127.0.0.1:5173",
                // "http://localhost:3000",
                // "http://127.0.0.1:3000"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        log.info("✅ CORS configured (origin patterns): {}", config.getAllowedOriginPatterns());
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
