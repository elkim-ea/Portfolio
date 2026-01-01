import axios from "axios";

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  // baseURL: import.meta.env.VITE_API_BASE_URL || "/api", //gcp
  withCredentials: true,
});

// src/types/api.ts
// 설명: 백엔드 공통 응답 포맷에 맞춰 타입을 정의한다.
//       success/message/data/meta 구조를 가정한다. 실제 필드는 백엔드와 맞춰 업데이트한다.

// 공통 응답 메타 정보
export interface Meta {
  page?: number;       // 페이지 번호(선택)
  size?: number;       // 페이지 크기(선택)
  total?: number;      // 전체 개수(선택)
  [key: string]: any;  // 그 외 메타 정보 허용
}

// 공통 응답 래퍼
export interface CommonResponse<T> {
  success: boolean;    // 요청 성공 여부
  message?: string;    // 서버 메시지(선택)
  data?: T;            // 실제 데이터
  meta?: Meta;         // 부가 정보(선택)
}

export default apiClient;