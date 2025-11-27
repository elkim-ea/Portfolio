import axiosAuth from "./axiosAuth";

export interface LifeLog {
  logId: number;
  content: string;
  category: "E" | "S" ;
  loggedAt: string;
  esgScoreEffect: number;
}

/** ✅ 내 활동 로그 조회 */
export const getMyLogs = async (date?: string): Promise<LifeLog[]> => {
  const res = await axiosAuth.get("/lifelog/me", { params: date ? { date } : {} });
  return res.data;
};

// /** ✅ 로그 추가 */
// export const addLog = async (content: string, category: "E" | "S") => {
//   const res = await axiosAuth.post("/lifelog", { content, category });
//   return res.data;
// };

/** ✅ 로그 추가 (AI 분석 포함 → RecordController 호출) */
export const addLog = async (content: string) => {
  // ⚠️ category는 RecordController에서 자동 판별하므로 제거
  const res = await axiosAuth.post("/lifelog", { content });
  return res.data;
};

/** ✅ 로그 수정 */
export const updateLog = async (
  logId: number,
  content: string,
  category: "E" | "S"
) => {
  const res = await axiosAuth.put(`/lifelog/${logId}`, { content, category, esgScoreEffect: 0,});
  return res.data;
};

/** ✅ 로그 삭제 */
export const deleteLog = async (logId: number) => {
  await axiosAuth.delete(`/lifelog/${logId}`);
};
