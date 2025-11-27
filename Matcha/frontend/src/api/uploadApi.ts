// // src/api/uploadApi.ts
// // 설명: 파일 업로드 전용 axios 인스턴스 (JWT 포함, multipart/form-data 자동 처리)

// import axios from "axios";
// import { getAccessToken } from "./authApi";

// const uploadApi = axios.create({
//   baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
//   withCredentials: true,
// });

// uploadApi.interceptors.request.use((config) => {
//   const token = getAccessToken();

//   // JWT 자동 첨부
//   if (token) {
//     config.headers = {
//       ...config.headers,
//       Authorization: `Bearer ${token}`,
//     };
//   }

//   return config;
// });

// export default uploadApi;

// src/api/uploadApi.ts
import axios from "axios";

const uploadApi = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  withCredentials: true,
});

// ✅ JWT 자동 첨부
uploadApi.interceptors.request.use((config) => {
  const token = localStorage.getItem("token"); // 로그인 시 저장한 access token
  if (token) {
    config.headers = {
      ...config.headers,
      Authorization: `Bearer ${token}`,
    };
  }
  return config;
});

export default uploadApi;
