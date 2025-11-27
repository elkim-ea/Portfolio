// src/pages/Home/components/QuestSection.tsx
import React, { useEffect, useState } from "react";
import axiosAuth from "@/api/axiosAuth";
import type { CommonResponse, Quest } from "@/types/api";
import { FiRefreshCw } from "react-icons/fi";

type WeatherInfo = {
  temperature: number;
  humidity: number;
  pm10: number;
  pm25: number;
  uv: number;
};

export default function QuestSection() {
  const [today, setToday] = useState<Quest[]>([]);
  const [progress, setProgress] = useState<Quest[]>([]);
  const [weather, setWeather] = useState<WeatherInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const MAIN_PATH = "/quest/main";
  const WEATHER_PATH = "/weather/current";

  const fetchData = async (lat?: number, lon?: number) => {
    setLoading(true);
    setError(null);

    try {
      const [questRes, weatherRes] = await Promise.all([
        // axiosAuth 사용 → Authorization 헤더 자동 첨부됨
        axiosAuth.get<CommonResponse<any>>(MAIN_PATH, {
          params: lat && lon ? { lat, lon } : undefined,
        }),
        axiosAuth.get<CommonResponse<WeatherInfo>>(WEATHER_PATH, {
          params: lat && lon ? { lat, lon } : undefined,
        }),
      ]);

      const questData = questRes.data.data;
      setToday(questData.today || []);
      setProgress(questData.progress || []);
      setWeather(weatherRes.data.data || null);
    } catch (e: any) {
      console.error("❌ QuestSection fetchData error:", e);
      setError(e?.response?.data?.message || "데이터 불러오기 실패");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    navigator.geolocation.getCurrentPosition(
      (pos) => fetchData(pos.coords.latitude, pos.coords.longitude),
      () => fetchData()
    );
  }, []);

  return (
    <section className="bg-[#FDFCF9] rounded-lg shadow-lg p-6 text-center">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-bold text-gray-900">모아보기</h3>

        {/* 🔄 리프레시 버튼 */}
        <button
          onClick={() =>
            navigator.geolocation.getCurrentPosition(
              (pos) => fetchData(pos.coords.latitude, pos.coords.longitude),
              () => fetchData()
            )
          }
          className="p-2 rounded-full hover:bg-white-green transition duration-300"
          title="새로고침"
        >
          <FiRefreshCw className="w-5 h-5 text-main-green hover:rotate-180 transition-transform duration-300" />
        </button>
      </div>

      {loading && <p className="text-sm text-gray-600">불러오는 중...</p>}
      {!loading && error && <p className="text-sm text-red-600">{error}</p>}

      {!loading && !error && (
        <>
          {weather && (
            <div className="mb-4 rounded-md border border-main-green bg-[#E8F5E9] shadow-lg py-3">
              <h4 className="text-base font-semibold text-main-green mb-1">
                오늘의 날씨
              </h4>
              <p className="text-sm text-gray-700">
                기온 {weather.temperature.toFixed(1)}°C · 습도{" "}
                {Math.round(weather.humidity)}%
              </p>
              <p className="text-sm text-gray-700 mt-1">
                미세먼지 {Math.round(weather.pm10)} · PM2.5{" "}
                {Math.round(weather.pm25)} µg/m³
              </p>
              <p className="text-xs text-main-green mt-1">
                UV {weather.uv.toFixed(1)}
              </p>
            </div>
          )}

          {/* 오늘의 퀘스트 */}
          <div className="mb-5">
            <h4 className="text-main-green text-lg font-semibold mb-2">
              오늘의 퀘스트
            </h4>
            {today.length > 0 ? (
              <>
                <p
                  className={`text-base font-medium ${
                    today[0].status === "SUCCESS"
                      ? "line-through text-gray-400"
                      : "text-gray-900"
                  }`}
                >
                  {today[0].title}
                </p>

                {today[0].description && (
                  <p
                    className={`text-sm mt-1 ${
                      today[0].status === "SUCCESS"
                        ? "line-through text-gray-400"
                        : "text-gray-500"
                    }`}
                  >
                    {today[0].description}
                  </p>
                )}
              </>
            ) : (
              <p className="text-sm text-gray-600">오늘의 퀘스트가 없습니다.</p>
            )}
          </div>

          <hr className="my-3 border-main-green/40" />

          {/* 진행 중 퀘스트 */}
          <div>
            <h4 className="text-main-green text-lg font-semibold mb-2">
              진행 중 퀘스트
            </h4>
            {progress.length > 0 ? (
              <ul className="flex flex-col items-center space-y-1">
                {progress.map((q) => (
                  <li
                    key={q.questId}
                    className={`text-base flex items-center space-x-1 ${
                      q.status === "SUCCESS"
                        ? "line-through text-gray-400"
                        : "text-gray-900"
                    }`}
                  >
                    <span>{q.title}</span>
                    {/* 진행 횟수 표시 (작은 글씨, 한 칸 띄워서) */}
                    {q.attemptCount !== undefined &&
                      q.maxAttempts !== undefined && (
                        <span
                          className={`text-xs ml-1 ${
                            q.status === "SUCCESS"
                              ? "text-gray-400"
                              : "text-gray-500"
                          }`}
                        >
                          ({q.attemptCount}/{q.maxAttempts})
                        </span>
                      )}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-sm text-gray-600">
                진행 중인 퀘스트가 없습니다.
              </p>
            )}
          </div>
        </>
      )}
    </section>
  );
}
