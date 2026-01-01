// src/api/axios.ts
import axios from "axios";

// 환경 변수 기반 (로컬/도커 모두 대응)
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  //  baseURL: import.meta.env.VITE_API_BASE_URL, //gcp
  withCredentials: true, // 필요 시 CORS 세션 쿠키 유지
  headers: {
    "Content-Type": "application/json",
  },
});

export default api;
