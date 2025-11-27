// src/pages/Quest/QuestPage.tsx
import React, { useEffect, useState, useCallback } from "react";
import { AiOutlineUpload } from "react-icons/ai";
import { questApi } from "@/api/questApi";
import type { Quest } from "@/api/questApi";
import FileUploadModal from "./FileUploadModal";
import TitleModal from "@/pages/Title/TitleModal";

interface WeatherInfo {
  temperature: number;
  humidity: number;
  pm10?: number;
  weatherDescription?: string;
}

const QuestPage: React.FC = () => {
  const [todayQuests, setTodayQuests] = useState<Quest[]>([]);
  const [weeklyQuests, setWeeklyQuests] = useState<Quest[]>([]);
  const [seasonQuests, setSeasonQuests] = useState<Quest[]>([]);
  const [weather, setWeather] = useState<WeatherInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [showTitleModal, setShowTitleModal] = useState(false);
  const [newTitles, setNewTitles] = useState<string[]>([]);

  // ✅ fetchAllData 함수로 데이터 로딩 로직을 분리
  const fetchAllData = useCallback(async () => {
    try {
      setLoading(true);
      navigator.geolocation.getCurrentPosition(
        async (pos) => {
          const lat = pos.coords.latitude;
          const lon = pos.coords.longitude;

          const airRes = await fetch(
            `https://air-quality-api.open-meteo.com/v1/air-quality?latitude=${lat}&longitude=${lon}&hourly=pm10`
          );
          const airData = await airRes.json();
          const pm10 = airData.hourly?.pm10?.[0] ?? 0;

          const weatherRes = await questApi.getWeather(lat, lon);
          if (weatherRes.success && weatherRes.data) {
            setWeather({ ...weatherRes.data, pm10 });
          }

          const [today, weekly, season] = await Promise.all([
            questApi.getToday(),
            questApi.getWeekly(),
            questApi.getSeason(),
          ]);

          setTodayQuests(today || []);
          setWeeklyQuests(weekly || []);
          setSeasonQuests(season || []);

          setLoading(false);
        },
        (err) => {
          console.error("위치 권한 오류:", err);
          setError("위치 정보를 가져올 수 없습니다.");
          setLoading(false);
        }
      );
    } catch (err) {
      console.error("데이터 로딩 실패:", err);
      setError("데이터를 불러오는 중 오류가 발생했습니다.");
      setLoading(false);
    }
  }, []);

  // ✅ 페이지 로드시 한 번 실행
  useEffect(() => {
    fetchAllData();
  }, [fetchAllData]);

  if (loading) return <div className="p-8 text-center">로딩 중...</div>;
  if (error) return <div className="p-8 text-center text-red-600">{error}</div>;

  return (
    <div className="min-h-screen w-full bg-[#E8F5E9]">
      <div className="max-w-5xl mx-auto py-10 space-y-8">
        {/* 상단 아이콘 */}
        <div className="flex justify-end items-center gap-8 mb-8">
          <div
            className="flex flex-col items-center cursor-pointer text-gray-600 hover:text-main-green transition-colors"
            onClick={() => setIsModalOpen(true)}
          >
            <AiOutlineUpload size={24} />
            <span className="text-sm">퀘스트 인증</span>
          </div>
          {/* <div
            className="flex flex-col items-center cursor-pointer text-gray-600 hover:text-main-green transition-colors"
            onClick={() => setIsModalOpen(true)}
          >
            <AiOutlineUpload size={24} />
            <span className="text-sm">오픈 API</span>
          </div> */}
        </div>

        {/* 🌤 오늘의 날씨 & 퀘스트 */}
        <div className="bg-white p-6 rounded-md shadow-md border border-gray-200 hover:shadow-lg transition">
          <h3 className="text-lg font-bold text-main-green mb-9 text-center">
            🌤 오늘의 날씨 & 퀘스트
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-2 divide-y md:divide-y-0 md:divide-x divide-gray-100">
            {/* 왼쪽: 오늘의 날씨 */}
            <div className="flex flex-col justify-center items-center text-center px-4 py-4 md:py-0">
              {weather ? (
                <div className="text-gray-700 leading-relaxed">
                  <p>
                    현재 온도:{" "}
                    <span className="font-semibold">
                      {weather.temperature}°C
                    </span>
                  </p>
                  <p>
                    습도:{" "}
                    <span className="font-semibold">{weather.humidity}%</span>
                  </p>
                  <p>
                    미세먼지:{" "}
                    <span className="font-semibold">{weather.pm10} μg/m³</span>
                  </p>
                </div>
              ) : (
                <p className="text-gray-500">날씨 정보를 불러오는 중...</p>
              )}
            </div>

            {/* 오른쪽: 오늘의 퀘스트 */}
            <div className="flex flex-col justify-center items-center text-center px-4 py-4 md:py-0">
              {todayQuests.length > 0 ? (
                <>
                  <p
                    className={`font-semibold text-lg mb-2 ${
                      todayQuests[0].status === "SUCCESS"
                        ? "line-through text-gray-400"
                        : todayQuests[0].category === "E"
                        ? "text-teal-600"
                        : "text-rose-500"
                    }`}
                  >
                    {todayQuests[0].title}
                  </p>
                  <p
                    className={`leading-relaxed ${
                      todayQuests[0].status === "SUCCESS"
                        ? "line-through text-gray-400"
                        : "text-gray-600"
                    }`}
                  >
                    {todayQuests[0].description}
                  </p>
                </>
              ) : (
                <p className="text-gray-500">오늘의 퀘스트가 없습니다.</p>
              )}
            </div>
          </div>
        </div>

        {/* 📆 주간 & 🌱 시즌 퀘스트 */}
        <div className="relative grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* 📆 주간 퀘스트 */}
          <div className="bg-white p-6 rounded-md shadow-md border border-gray-200">
            <h3 className="text-lg font-bold text-center text-main-green mb-4">
              📆 주간 퀘스트
            </h3>
            <ul className="list-none space-y-2">
              {weeklyQuests.length > 0 ? (
                weeklyQuests.map((q) => (
                  <li
                    key={q.questId}
                    className="flex items-center justify-between gap-2"
                  >
                    {/* 왼쪽: 퀘스트 이름 */}
                    <div className="flex items-center gap-2">
                      <span
                        className={`inline-block w-2 h-2 rounded-full ${
                          q.category === "E" ? "bg-teal-400" : "bg-rose-400"
                        }`}
                      ></span>
                      <span
                        className={`${
                          q.status === "SUCCESS"
                            ? "line-through text-gray-400"
                            : "text-gray-800"
                        }`}
                      >
                        {q.title}
                      </span>
                    </div>

                    {/* 오른쪽: 진행 횟수 */}
                    <span
                      className={`text-sm ${
                        q.status === "SUCCESS"
                          ? "text-gray-400"
                          : "text-gray-500"
                      }`}
                    >
                      ({q.attemptCount ?? 0}/{q.maxAttempts ?? 10})
                    </span>
                  </li>
                ))
              ) : (
                <p className="text-gray-500 text-center">
                  주간 퀘스트가 없습니다.
                </p>
              )}
            </ul>
          </div>

          {/* 🌱 시즌 퀘스트 */}
          <div className="bg-white p-6 rounded-md shadow-md border border-gray-200 relative">
            <h3 className="text-lg font-bold text-center text-main-green mb-4">
              🌱 시즌 퀘스트
            </h3>
            <ul className="list-none space-y-2">
              {seasonQuests.length > 0 ? (
                seasonQuests.map((q) => (
                  <li
                    key={q.questId}
                    className="flex items-center justify-between gap-2"
                  >
                    {/* 왼쪽: 퀘스트 이름 */}
                    <div className="flex items-center gap-2">
                      <span
                        className={`inline-block w-2 h-2 rounded-full ${
                          q.category === "E" ? "bg-teal-400" : "bg-rose-400"
                        }`}
                      ></span>
                      <span
                        className={`${
                          q.status === "SUCCESS"
                            ? "line-through text-gray-400"
                            : "text-gray-800"
                        }`}
                      >
                        {q.title}
                      </span>
                    </div>

                    {/* 오른쪽: 진행 횟수 */}
                    <span
                      className={`text-sm ${
                        q.status === "SUCCESS"
                          ? "text-gray-400"
                          : "text-gray-500"
                      }`}
                    >
                      ({q.attemptCount ?? 0}/{q.maxAttempts ?? 10})
                    </span>
                  </li>
                ))
              ) : (
                <p className="text-gray-500 text-center">
                  시즌 퀘스트가 없습니다.
                </p>
              )}
            </ul>

            {/* 범례 */}
            <div className="absolute -bottom-6 right-3 flex items-center gap-4 text-sm text-gray-600">
              <div className="flex items-center gap-1">
                <span className="inline-block w-2 h-2 rounded-full bg-teal-400"></span>
                <span>E (환경)</span>
              </div>
              <div className="flex items-center gap-1">
                <span className="inline-block w-2 h-2 rounded-full bg-rose-400"></span>
                <span>S (사회)</span>
              </div>
            </div>
          </div>

          {/* ✅ 모달 닫히면 데이터 새로고침 */}
          {isModalOpen && todayQuests.length > 0 && (
            <FileUploadModal
              questId={todayQuests[0].questId} // 수정 이유 : FileUploadModal에 questId 전달 누락으로 undefined 발생 → 오늘의 퀘스트 ID를 props로 전달
              onClose={() => {
                setIsModalOpen(false);
                fetchAllData();
              }}
              onTitleEarned={(titles) => {
                // ✅ FileUploadModal 닫힌 후 실행됨
                setNewTitles(titles);
                setShowTitleModal(true);
              }}
            />
          )}

          {showTitleModal && (
            <TitleModal
              titles={newTitles}
              onClose={() => setShowTitleModal(false)}
            />
          )}
        </div>
      </div>
    </div>
  );
};

export default QuestPage;
