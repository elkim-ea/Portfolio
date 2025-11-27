import React from "react";
import { useNavigate } from "react-router-dom";

export default function CalendarSection() {
  const navigate = useNavigate();

  const date = new Date();
  const year = date.getFullYear();
  const month = date.getMonth();
  const today = date.getDate();

  const lastDay = new Date(year, month + 1, 0).getDate();
  const days = Array.from({ length: lastDay }, (_, i) => i + 1);

  const handleClick = (day: number) => {
    // ✅ toISOString() 대신 로컬 기준으로 직접 포맷
    const selectedDate = `${year}-${String(month + 1).padStart(
      2,
      "0"
    )}-${String(day).padStart(2, "0")}`;
    navigate(`/record/${selectedDate}`);
  };

  return (
    <section className="bg-[#FDFCF9] rounded-lg shadow-lg p-6 text-center">
      <h3 className="text-xl font-bold text-black-900 mb-6">
        {year}년 {month + 1}월 캘린더
      </h3>

      <div className="grid grid-cols-7 gap-x-6 gap-y-5 justify-items-center px-4">
        {days.map((day) => (
          <div
            key={day}
            onClick={() => handleClick(day)}
            className={`flex items-center justify-center w-12 h-12 rounded-md border text-sm font-medium shadow-sm cursor-pointer transition
              ${
                day === today
                  ? "bg-[#66BB6A] text-[#FDFCF9] border-[#E8F5E9] shadow-md"
                  : "bg-white text-gray-700 border-gray-300 hover:bg-[#E8F5E9] hover:text-[#2E7D32] hover:shadow"
              }`}
          >
            {day}
          </div>
        ))}
      </div>
    </section>
  );
}
