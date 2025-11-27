// // src/api/axiosAuth.ts
// // 설명: 로그인된 사용자 전용 axios 인스턴스 (모든 인증 필요 API 요청에 사용)

// import axios from "axios";
// import { getAccessToken, clearAccessToken } from "./authApi";

// // 기본 axios 인스턴스 설정
// const axiosAuth = axios.create({
//   baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
//   withCredentials: true, // CORS 쿠키 허용
// });

// // 요청 인터셉터: JWT 토큰 자동 첨부
// axiosAuth.interceptors.request.use(
//   (config) => {
//     const token = localStorage.getItem("token");

//     // headers 객체 병합 (axios 버전별 덮어쓰기 방지)
//     config.headers = {
//       ...config.headers,
//       "Content-Type": "application/json",
//     };

//     console.log("🔍 axiosAuth 요청 URL →", config.url);
//     console.log("📦 저장된 토큰:", token);

//     if (token) {
//       config.headers.Authorization = `Bearer ${token}`;
//       console.log("✅ Authorization 헤더 추가됨:", config.headers.Authorization);
//     } else {
//       console.warn("⚠️ 토큰 없음 — Authorization 헤더 추가 안 됨");
//     }

//     return config;
//   },
//   (error) => Promise.reject(error)
// );

// // 응답 인터셉터: 인증 만료 시 자동 로그아웃 처리
// axiosAuth.interceptors.response.use(
//   (response) => response,
//   (error) => {
//     const { response } = error;
//     if (response?.status === 401) {
//       console.warn("🚫 인증 만료 — 로그인 페이지로 이동");
//       clearAccessToken();
//       window.location.href = "/login";
//     } else if (response?.status === 403) {
//       console.error("⛔ 403 Forbidden — 권한 부족 또는 토큰 인식 실패");
//     }
//     return Promise.reject(error);
//   }
// );

// export default axiosAuth;




// src/api/axiosAuth.ts
// 설명: 로그인된 사용자 전용 axios 인스턴스 (모든 인증 필요 API 요청에 사용)

import axios from "axios";
import { getAccessToken, clearAccessToken } from "./authApi";

// 기본 axios 인스턴스 설정
const axiosAuth = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  withCredentials: true, // CORS 쿠키 허용
});

// 요청 인터셉터: JWT 토큰 자동 첨부
axiosAuth.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");

    // headers 객체 병합 (axios 버전별 덮어쓰기 방지)
    config.headers = {
      ...config.headers,
      "Content-Type": "application/json",
    };

    // 토큰이 존재할 경우 Authorization 헤더 추가
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터: 인증 만료 시 자동 로그아웃 처리
axiosAuth.interceptors.response.use(
  (response) => response,
  (error) => {
    const { response } = error;

    // 401 → 인증 만료 시 토큰 삭제 후 로그인 페이지로 이동
    if (response?.status === 401) {
      clearAccessToken();
      window.location.href = "/login";
    }

    // 403 → 접근 권한 없음
    if (response?.status === 403) {
      // 권한 부족 시 추가 처리 로직을 여기에 작성 가능 (예: 알림창 표시 등)
    }

    return Promise.reject(error);
  }
);

export default axiosAuth;
