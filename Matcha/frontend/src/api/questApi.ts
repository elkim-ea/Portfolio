// src/api/questApi.ts
// 설명: 퀘스트 관련 API 모듈 (오늘의 퀘스트, 주간/시즌 퀘스트, 인증 제출 등)

import axiosAuth from "./axiosAuth";
import type { CommonResponse } from "@/types/api";

// 퀘스트 엔티티 정의
export interface Quest {
  questId: number;
  title: string;
  description?: string;
  type: "DAILY" | "WEEKLY" | "SEASON";
  rewardScore: number;
  isActive: boolean;
  createdAt?: string;
  category: "E" | "S";
  status?: "PENDING" | "SUCCESS";
  attemptCount?: number;      // 진행 횟수
  maxAttempts?: number;       // 최대 횟수
}

// 퀘스트 인증 제출 요청 구조
export interface QuestSubmission {
  questId: number; // 퀘스트 ID
  authType: "IMAGE" | "TEXT"; // 인증 타입
  authContent: string; // 이미지 URL 또는 텍스트 내용
}

// 퀘스트 완료 후 응답 구조
export interface QuestSubmitResponse {
  message: string; // 완료 메시지
  reward: number; // 획득 점수
  newTitles: string[]; // 새로 획득한 칭호 목록
}

// 퀘스트 관련 API
export const questApi = {
  // 메인 퀘스트 조회 (홈 화면)
  getMain: async (lat: number, lon: number): Promise<CommonResponse<any>> => {
    const response = await axiosAuth.get("/quest/main", {
      params: { lat, lon },
    });
    return response.data;
  },

  // 오늘의 퀘스트 조회
  getToday: async (): Promise<Quest[]> => {
    const response = await axiosAuth.get("/quest/today");
    return response.data?.data || []; // 안전하게 처리
  },
  // 주간 퀘스트 조회
  getWeekly: async (): Promise<Quest[]> => {
    const response = await axiosAuth.get("/quest/weekly");
    return response.data?.data || [];
  },
  // 시즌 퀘스트 조회
  getSeason: async (): Promise<Quest[]> => {
    const response = await axiosAuth.get("/quest/season");
    return response.data?.data || [];
  },

  // 내 퀘스트 현황 조회
  getMyQuests: async (): Promise<CommonResponse<Quest[]>> => {
    const response = await axiosAuth.get("/quest/mypage");
    return response.data;
  },

  // 퀘스트 인증 제출 (이미지/텍스트)
  submitQuest: async (
    questId: number,
    data: QuestSubmission
  ): Promise<CommonResponse<QuestSubmitResponse>> => {
    const response = await axiosAuth.post(`/quest/${questId}/submit`, data);
    return response.data;
  },

  // 현재 날씨 조회 (위경도 기반)
  getWeather: async (
    lat: number,
    lon: number
  ): Promise<CommonResponse<any>> => {
    const response = await axiosAuth.get("/weather/current", {
      params: { lat, lon },
    });
    return response.data;
  },
};

export default questApi;
