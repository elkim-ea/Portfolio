import React from "react";
import ProfileSection from "./components/ProfileSection";
import CalendarSection from "./components/CalendarSection";
import QuestSection from "./components/QuestSection";

export default function HomePage() {
  return (
    // ✅ 전체 배경색 적용 + flex 중앙정렬 (absolute 제거)
    <div className="flex justify-center items-center min-h-[calc(100vh-60px)] bg-[#E8F5E9]">
      <div className="grid grid-cols-[2fr_1fr] gap-8 max-w-6xl w-full px-6">
        {/* 왼쪽 */}
        <div className="flex flex-col gap-6">
          <ProfileSection />
          <CalendarSection />
        </div>

        {/* 오른쪽 */}
        <div className="flex flex-col gap-6">
          <QuestSection />
        </div>
      </div>
    </div>
  );
}
