import React, { useEffect, useState } from "react";
import { getGlobalRanking, getMyRanking, RankingItem } from "@/api/rankingApi";

const RankingPage: React.FC = () => {
  const [topRankings, setTopRankings] = useState<RankingItem[]>([]);
  const [myRanking, setMyRanking] = useState<RankingItem | null>(null);
  const [showAll, setShowAll] = useState(false);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  /** ✅ 데이터 불러오기 */
  useEffect(() => {
    const fetchRankingData = async () => {
      setLoading(true);
      setErr(null);
      try {
        const [globalRes, myRes] = await Promise.all([
          getGlobalRanking(100),
          getMyRanking(),
        ]);
        setTopRankings(globalRes);
        setMyRanking(myRes);
      } catch {
        setErr("⚠️ 랭킹 데이터를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };
    fetchRankingData();
  }, []);

  /** ✅ 리스트 처리 (본인 제외 후 20개 or 전체) */
  const filteredList = topRankings.filter(
    (item) => item.nickname !== myRanking?.nickname
  );
  const visibleList = showAll ? filteredList : filteredList.slice(0, 20);

  /** ✅ 전체 보기 / 접기 핸들러 */
  const handleShowAll = () => setShowAll(true);
  const handleCollapse = () => setShowAll(false);

  return (
    <div className="bg-[#E8F5E9] min-h-screen p-4 sm:p-6 lg:p-8">
      <div className="max-w-3xl mx-auto bg-white rounded-lg shadow-md p-5 sm:p-6 lg:p-7">
        {/* ✅ 헤더 */}
        <header className="mb-4">
          <h1 className="text-4xl font-bold text-black mb-2">랭킹보기</h1>
          <p className="text-right text-sm text-[#666]">
            (E, S 종합점수 기반 랭킹)
          </p>
        </header>

        {/* ✅ 로그인 유저 정보 */}
        {!loading && myRanking && (
          <div className="mb-6 border-2 border-[#1565C0] bg-[#E3F2FD] rounded-md px-4 py-4 flex items-center justify-between font-bold text-[#0D47A1]">
            <span className="w-20">{myRanking.rank}.</span>
            <span className="flex-1 truncate px-2">{myRanking.nickname} (나)</span>
            <span className="w-24 text-right">{myRanking.score}점</span>
          </div>
        )}

        {/* ✅ 상태 메시지 */}
        {loading && (
          <div className="text-center p-8 text-[#1565C0]">불러오는 중...</div>
        )}
        {!loading && err && (
          <div className="text-center p-6 font-bold text-[#d32f2f]">{err}</div>
        )}

        {/* ✅ 전체 랭킹 리스트 (본인 제외) */}
        {!loading && !err && (
          <div id="ranking-list" className="divide-y divide-[#2E7D32]/40">
            {visibleList.length > 0 ? (
              visibleList.map((item) => (
                <div
                  key={`${item.rank}-${item.nickname}`}
                  className="flex items-center justify-between py-2.5"
                >
                  <span className="w-16 font-bold">{item.rank}.</span>
                  <span className="flex-1 truncate px-2">{item.nickname}</span>
                  <span className="w-24 text-right font-semibold">
                    {item.score}점
                  </span>
                </div>
              ))
            ) : (
              <div className="py-10 text-center text-[#1565C0]">
                랭킹 데이터가 없습니다.
              </div>
            )}
          </div>
        )}

        {/* ✅ 전체 보기 / 접기 버튼 */}
        {!loading && !err && filteredList.length > 20 && (
          <>
            {!showAll ? (
              <button
                className="block mx-auto mt-6 mb-4 text-[#2E7D32] underline hover:no-underline"
                onClick={handleShowAll}
              >
                전체 보기
              </button>
            ) : (
              <button
                className="block mx-auto mt-6 mb-4 text-[#2E7D32] underline hover:no-underline"
                onClick={handleCollapse}
              >
                접기
              </button>
            )}
          </>
        )}

        <p className="text-center text-sm text-[#888] mt-5">
          {showAll ? "(전체 랭킹 + 나의 정보)" : "(상위 20등까지만 + 나의 정보)"}
        </p>
      </div>
    </div>
  );
};

export default RankingPage;