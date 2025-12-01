import React, { useEffect, useRef, useState } from "react";
import html2canvas from "html2canvas";
import { getMyActivity, getActivitySummary, ActivityResponse } from "@/api/activityApi";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import { Line } from "react-chartjs-2";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

declare global {
  interface Window {
    html2canvas: any;
  }
}

const ASSET_BASE = import.meta.env.VITE_API_BASE_URL;

const ActivityPage: React.FC = () => {
  const [data, setData] = useState<ActivityResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const scoreCardRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const fetchAll = async () => {
      setLoading(true);
      setErr(null);
      try {
        const [userRes, summaryRes] = await Promise.all([
          getMyActivity(),
          getActivitySummary(7, 30),
        ]);

        setData({
          totalScore: userRes.totalScore,
          esgScore: userRes.esgScore,
          eScore: userRes.eScore,
          sScore: userRes.sScore,
          characterUrl: userRes.characterUrl,
          eWeeklyData: summaryRes.eWeeklyData,
          sMonthlyData: summaryRes.sMonthlyData,
          eRecentLogs: summaryRes.eRecentLogs,
          sRecentLogs: summaryRes.sRecentLogs,
        });

      } catch (e) {
        setErr("⚠️ 활동 데이터를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, []);

  const chartOptions = (title: string): any => ({
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      title: { display: true, text: title },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          stepSize: 1, // ✅ 1 단위 눈금만 표시
          callback: (value: number) => (Number.isInteger(value) ? value : null),
        },
      },
    },
  });

  const eChart = {
    labels: data?.eWeeklyData?.labels ?? [],
    datasets: [
      {
        label: "E 활동 점수 변화",
        data: data?.eWeeklyData?.scores ?? [],
        borderColor: "#66BB6A",
        backgroundColor: "#2E7D32",
        tension: 0.4,
      },
    ],
  };

  const sChart = {
    labels: data?.sMonthlyData?.labels ?? [],
    datasets: [
      {
        label: "S 활동 점수 변화",
        data: data?.sMonthlyData?.scores ?? [],
        borderColor: "#42A5F5",
        backgroundColor: "#1565C0",
        tension: 0.4,
      },
    ],
  };

  const handleDownloadImage = async () => {
    const el = scoreCardRef.current;
    if (!el) return;
    try {
      await new Promise((resolve) => setTimeout(resolve, 300));
      const canvas = await html2canvas(el, { useCORS: true, background: "#fff" });
      const dataUrl = canvas.toDataURL("image/png");
      const a = document.createElement("a");
      a.href = dataUrl;
      a.download = "my-esg-card.png";
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    } catch (e) {
      console.error("이미지 다운로드 실패:", e);
    }
  };

  return (
    <div className="bg-[#E8F5E9] min-h-screen p-4 sm:p-6 lg:p-8">
      <div className="max-w-3xl mx-auto bg-white rounded-lg shadow-md p-5 sm:p-6 lg:p-7">
        <header className="mb-4">
          <h1 className="text-4xl font-bold text-black mb-2">나의 활동</h1>
          <p className="text-[#1565C0]">
            일상 속 ESG 활동을 기록하고 포인트를 쌓아보세요.
          </p>
        </header>

        {/* ✅ ESG 점수 카드 */}
        <div
          ref={scoreCardRef}
          className="flex items-center justify-between border border-[#e9ecef] rounded-xl p-5 mb-4"
        >
          <div>
            <h2 className="text-xl font-bold mb-2">나의 ESG 종합 점수</h2>
            <p className="text-3xl sm:text-4xl font-extrabold text-[#2E7D32] mb-2">
              {data?.esgScore ?? 0}점
            </p>
            <p className="mb-0.5">
              E 활동 점수:{" "}
              <span className="font-bold" style={{ color: "#1565C0" }}>
                {data?.eScore ?? 0}
              </span>
            </p>
            <p>
              S 활동 점수:{" "}
              <span className="font-bold" style={{ color: "#2E7D32" }}>
                {data?.sScore ?? 0}
              </span>
            </p>
          </div>
          <div className="text-center">
            <img
              src={data?.characterUrl || `${ASSET_BASE}/uploads/character/flower.png`}
              alt="ESG 캐릭터"
              className="w-[120px] h-[120px] object-contain"
              crossOrigin=""
              onError={(e) => {
                (e.currentTarget as HTMLImageElement).src = `${ASSET_BASE}/uploads/character/flower.png`;
              }}
            />
          </div>
        </div>

        {/* ✅ 이미지 다운로드 버튼 */}
        <button
          onClick={handleDownloadImage}
          className="block w-full max-w-[300px] mx-auto mb-7 py-3 px-4 rounded-lg font-bold text-white bg-[#2E7D32] hover:bg-[#1b5e20] transition-colors"
        >
          카드 이미지 다운로드
        </button>

        {/* ✅ E/S 활동 차트 */}
        <div className="grid grid-cols-1 gap-5 mb-5 lg:grid-cols-2">
          <ChartCard
            title="E 활동 변화 차트 (최근 1주일)"
            chartData={eChart}
            loading={loading}
            options={chartOptions("E 활동 주간 변화")}
          />
          <ChartCard
            title="S 활동 변화 차트 (최근 1개월)"
            chartData={sChart}
            loading={loading}
            options={chartOptions("S 활동 월간 변화")}
          />
        </div>

        {/* ✅ 활동 로그 리스트 */}
        <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
          <LogList
            title="E 카테고리 (최근 7일)"
            logs={data?.eRecentLogs || []}
            loading={loading}
          />
          <LogList
            title="S 카테고리 (최근 30일)"
            logs={data?.sRecentLogs || []}
            loading={loading}
          />
        </div>

         {/* --- 에러 메시지 --- */}
        {!loading && err && (
          <div className="text-center text-[#d32f2f] font-bold mt-4">{err}</div>
        )}
      </div>
    </div>
  );
};

const ChartCard = ({ title, chartData, options, loading }: any) => (
  <div className="bg-white rounded-xl shadow p-4">
    <h3 className="font-semibold mb-3">{title}</h3>
    <div className="h-[300px]">
      {loading ? (
        <div className="text-center text-[#1565C0] py-4">불러오는 중...</div>
      ) : (
        <Line data={chartData} options={options} />
      )}
    </div>
  </div>
);

const LogList = ({ title, logs, loading }: any) => (
  <div className="bg-white rounded-xl shadow p-4">
    <h4 className="font-semibold mb-3">{title}</h4>
    <div className="h-[300px] overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-[#bdbdbd] scrollbar-track-[#f5f5f5]">
      {loading ? (
        <div className="text-center text-[#1565C0] py-4">불러오는 중...</div>
      ) : logs.length > 0 ? (
        logs.map((l: any) => (
          <div
            key={l.logId}
            className="flex items-center justify-between py-2 border-b last:border-0 border-[#e9ecef]"
          >
            <span className="text-[#222]">
              {l.content} <b>+{l.esgScoreEffect}</b>
            </span>
            <span className="text-[#666] text-sm">{l.loggedAt}</span>
          </div>
        ))
      ) : (
        <p className="text-[#1565C0]">기록이 없습니다.</p>
      )}
    </div>
  </div>
);

export default ActivityPage;