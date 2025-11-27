import axiosAuth from "./axiosAuth";

export interface ChartData {
  labels: string[];
  scores: number[];
}

export interface ActivityResponse {
  totalScore: number;   // ✅ 추가
  esgScore: number;
  eScore: number;
  sScore: number;
  characterUrl: string;
  eWeeklyData?: ChartData;
  sMonthlyData?: ChartData;
  eRecentLogs?: any[];
  sRecentLogs?: any[];
}

/** ✅ 내 ESG 요약 정보 */
export const getMyActivity = async (): Promise<ActivityResponse> => {
  const res = await axiosAuth.get("/activity/me");
  return res.data;
};

/** ✅ ESG 활동 통계 + 로그 (E=7일, S=30일) */
export const getActivitySummary = async (
  eDays = 7,
  sDays = 30
): Promise<ActivityResponse> => {
  const res = await axiosAuth.get("/activity", { params: { eDays, sDays } });
  return res.data;
};
