import React, { useEffect, useState } from "react";
import axiosAuth from "@/api/axiosAuth";
import defaultChar from "../../../assets/character/default.png";

export default function ProfileSection() {
  const [user, setUser] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await axiosAuth.get("/my/profile");
        setUser(res.data.data);
      } catch (err) {
        console.error("프로필 불러오기 실패:", err);
        setError("프로필을 불러올 수 없습니다.");
      }
    };
    fetchProfile();
  }, []);

  if (error)
    return <div className="text-center text-gray-500 mt-8">{error}</div>;
  if (!user)
    return <div className="text-center text-gray-500 mt-8">로딩 중...</div>;

  // ✅ 하드코딩 등급 구간 (백엔드와 동일해야 함)
  const levels = [
    { max: 50, img: "/uploads/character/default.png" },
    { max: 150, img: "/uploads/character/seed.png" },
    { max: 300, img: "/uploads/character/flower.png" },
    { max: 500, img: "/uploads/character/tree.png" },
    { max: 800, img: "/uploads/character/earth.png" },
    { max: 1100, img: "/uploads/character/moon.png" },
    { max: 1500, img: "/uploads/character/star.png" },
    { max: 2000, img: "/uploads/character/sun.png" },
    { max: 2500, img: "/uploads/character/wind.png" },
    { max: Infinity, img: "/uploads/character/cloud.png" },
  ];

  // ✅ 현재 점수 및 등급 계산
  const score = user.esgScore || 0;
  const currentLevel = levels.find((level) => score < level.max) || levels[0];
  const prevLevelMax =
    levels[levels.indexOf(currentLevel) - 1]?.max || 0;
  const nextLevelMax = currentLevel.max;

  // ✅ 경험치바 비율 계산
  const expRate = Math.min(
    ((score - prevLevelMax) / (nextLevelMax - prevLevelMax)) * 100,
    100
  );
  // ✅ 다음 등급까지 남은 점수
  const expRemaining = Math.max(nextLevelMax - score, 0);

  return (
    <section className="bg-[#FDFCF9] rounded-lg shadow-lg p-6 text-center">
      <div className="mb-3">
        <p className="text-sm text-gray-700 font-medium">
          {user.mainTitleName || "칭호 없음"}
        </p>
        <h3 className="text-2xl font-bold text-gray-900">{user.nickname}</h3>
      </div>

      {/* ✅ 캐릭터 이미지 표시 (하드코딩 경로 사용) */}
      <div className="w-28 h-28 mx-auto my-4">
        <img
          src={
            currentLevel.img
              ? `http://localhost:8080${currentLevel.img}`
              : defaultChar
          }
          alt="캐릭터"
          className="w-full h-full object-contain"
        />
      </div>

      {/* ✅ 경험치바 + 남은 점수 표시 */}
      <div className="mt-4">
        <div className="w-full bg-[#E8F5E9] rounded-full h-3">
          <div
            className="bg-main-green h-3 rounded-full transition-all duration-300"
            style={{ width: `${expRate}%` }}
          ></div>
        </div>
        <p className="text-sm text-gray-600 mt-1">
          다음 등급까지 {expRemaining}p
        </p>
      </div>
    </section>
  );
}
