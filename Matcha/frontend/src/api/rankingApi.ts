import axiosAuth from "./axiosAuth";

export interface RankingItem {
  rank: number;
  nickname: string;
  score: number;
}

/** ✅ 전체 랭킹 조회 */
export const getGlobalRanking = async (limit = 100): Promise<RankingItem[]> => {
  const res = await axiosAuth.get("/ranking/global", { params: { limit } });
  return res.data;
};

/** ✅ 내 랭킹 조회 */
export const getMyRanking = async (): Promise<RankingItem> => {
  const res = await axiosAuth.get("/ranking/me");
  return res.data;
};
