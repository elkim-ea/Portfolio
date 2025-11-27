// src/api/companyApi.ts
// 설명: ESG 기업 관련 API 모듈 (선도 기업 / 후원 기업 / 상세 조회)

import axiosAuth from "./axiosAuth";
import type { CommonResponse } from "@/types/api";

export interface Company {
  companyId: number;          // 기업 ID
  companyName: string;        // 기업명
  companyLogo: string;        // 로고 이미지 URL
  companyWebsiteUrl: string;  // 기업 공식 홈페이지 URL
  createdAt?: string;         // 등록 일시 (선택)
}

// ESG 기업 관련 API
export const companyApi = {
  // ESG 선도 기업 리스트 조회
  getLeading: async (): Promise<CommonResponse<Company[]>> => {
    const response = await axiosAuth.get("/company/leading");
    return response.data;
  },

  // ESG 후원 기업 리스트 조회
  getSponsor: async (): Promise<CommonResponse<Company[]>> => {
    const response = await axiosAuth.get("/company/sponsor");
    return response.data;
  },

  // 기업 상세 ESG 활동 조회
  getDetail: async (
    companyId: number
  ): Promise<CommonResponse<Company>> => {
    const response = await axiosAuth.get(`/company/${companyId}`);
    return response.data;
  },
};

export default companyApi;
