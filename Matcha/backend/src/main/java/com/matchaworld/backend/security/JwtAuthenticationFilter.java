package com.matchaworld.backend.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.matchaworld.backend.service.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 수정 이유: CORS preflight 요청(OPTIONS)은 인증 필터를 거치면 403 발생하므로 우회 + CORS 헤더 직접 추가
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 인증 필요 없는 경로는 필터 건너뛰기
        if (requestURI.startsWith("/api/auth")
                || requestURI.startsWith("/api/weather")
                || requestURI.startsWith("/uploads")
                || requestURI.startsWith("/v3/api-docs")
                || requestURI.startsWith("/swagger-ui")) {

            log.info("🟢 JWT Filter Skip for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        if (requestURI.equals("/api/health")) {
            response.getWriter().write("ok");
            return;
        }

        String method = request.getMethod();

        log.info("========================================");
        log.info("🌐 JWT Filter Start - {} {}", method, requestURI);
        log.info("========================================");

        try {
            String token = extractTokenFromRequest(request);

            if (token == null) {
                log.warn("⚠️ No token found in Authorization header");
            } else {
                log.info("✅ Token found: {}...", token.substring(0, Math.min(20, token.length())));

                // JWT 유효성 검증
                if (jwtService.validateToken(token)) {
                    log.info("✅ Token is valid");

                    String tokenType = jwtService.getTokenType(token);
                    log.info("📝 Token type: {}", tokenType);

                    if ("access".equals(tokenType)) {
                        Long userId = jwtService.getUserIdFromToken(token);
                        String email = jwtService.getEmailFromToken(token);
                        String role = jwtService.getRoleFromToken(token);

                        log.info("🔐 JWT Data - userId: {}, email: {}, role: {}", userId, email, role);

                        if (role != null && !role.isEmpty()) {
                            String authority = "ROLE_" + role;
                            SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority);

                            UsernamePasswordAuthenticationToken authentication
                                    = new UsernamePasswordAuthenticationToken(
                                            userId, null, Collections.singletonList(grantedAuthority)
                                    );

                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            log.info("✅ SecurityContext set successfully");
                        } else {
                            throw new JwtException("권한 정보가 없습니다."); // 수정 이유: null role 예외 처리 통일
                        }
                    } else {
                        throw new JwtException("유효하지 않은 토큰 타입입니다."); // 수정 이유: invalid type 예외 처리 통일
                    }
                } else {
                    throw new JwtException("유효하지 않은 토큰입니다."); // 수정 이유: token invalid 예외 처리 통일
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            setErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
        }

        log.info("🔚 JWT Filter End - Proceeding to next filter");
        log.info("========================================");
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void setErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
    }
}
