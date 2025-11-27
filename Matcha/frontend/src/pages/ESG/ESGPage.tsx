import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const ESGPage = () => {
  const navigate = useNavigate();
  const [isPlaying, setIsPlaying] = useState<boolean>(false);

  // 페이지 진입 시 ESGPage를 이미 본 사용자는 로그인 페이지로 리다이렉트
  useEffect(() => {
    const hasSeen = localStorage.getItem("hasSeenESG");
    if (hasSeen === "true") {
      navigate("/login");
    }
  }, [navigate]);

  return (
    <div className="min-h-screen bg-[#E8F5E9] flex flex-col items-center gap-6 p-8">
      {/* ESG 설명 섹션 */}
      <section className="bg-white rounded-xl shadow p-8 mt-10 text-center w-full max-w-[1100px]">
        <h2 className="text-2xl font-bold text-[#66BB6A] mb-4">ESG란?</h2>
        <p className="text-[#000000] leading-relaxed text-base max-w-[900px] mx-auto">
          ESG는 기업이 단순히 이윤만 추구하는 것이 아니라,
          <br />
          <strong className="text-[#2E7D32]">환경(Environment)</strong> /
          <strong className="text-[#2E7D32]"> 사회(Social)</strong> /
          <strong className="text-[#2E7D32]"> 지배구조(Governance)</strong> 세
          가지 비재무적 요소를 함께 고려하여
          <br />
          지속가능한 성장을 추구하는 경영 원칙을 의미합니다.
        </p>
      </section>

      {/* ESG 세부 요소 카드 */}
      <section className="w-full max-w-[1100px]">
        <div className="grid grid-cols-3 gap-6">
          <div className="bg-[#66BB6A] text-white p-6 rounded-lg leading-relaxed shadow hover:shadow-lg transition">
            <h3 className="text-lg font-semibold">환경(Environment)</h3>
            <p className="mt-2 text-sm">
              탄소배출 감축, 자원 절약, 재생에너지 사용 확대 등으로 지구 환경 보호에 기여하는 책임을 뜻합니다.
            </p>
          </div>

          <div className="bg-[#66BB6A] text-white p-6 rounded-lg leading-relaxed shadow hover:shadow-lg transition">
            <h3 className="text-lg font-semibold">사회(Social)</h3>
            <p className="mt-2 text-sm">
              근로자 권리 보장, 다양성과 포용성 실현, 공정한 노동환경 조성 등 사회적 책임을 의미합니다.
            </p>
          </div>

          <div className="bg-[#66BB6A] text-white p-6 rounded-lg leading-relaxed shadow hover:shadow-lg transition">
            <h3 className="text-lg font-semibold">지배구조(Governance)</h3>
            <p className="mt-2 text-sm">
              투명한 의사결정, 윤리적 경영, 주주의 권익 보호, 부패 방지 및 공정한 기업 운영을 포함합니다.
            </p>
          </div>
        </div>
      </section>

      {/* ESG 소개 영상 */}
      <section className="bg-white rounded-xl shadow p-8 text-center w-full max-w-[1100px] flex flex-col items-center justify-center">
        <h3 className="text-xl font-semibold text-[#66BB6A] mb-5">ESG 소개 영상</h3>

        {isPlaying ? (
          <iframe
            src="https://www.youtube.com/embed/Wv2nuSeOVGo?autoplay=1"
            title="ESG 설명 영상"
            className="w-full max-w-[800px] h-[450px] rounded-lg border border-[#A5D6A7]"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
          ></iframe>
        ) : (
          <button
            className="cursor-pointer hover:opacity-80 transition"
            onClick={() => setIsPlaying(true)}
          >
            <svg
              width="120"
              height="120"
              viewBox="0 0 24 24"
              fill="#2E7D32"
              xmlns="http://www.w3.org/2000/svg"
            >
              <polygon points="5,3 19,12 5,21" />
            </svg>
          </button>
        )}
      </section>

      {/* 하단 로그인 버튼 */}
      <div className="w-full max-w-[1100px] flex justify-end">
        <button
          className="bg-white border border-[#66BB6A] text-[#2E7D32] px-6 py-3 rounded-lg font-medium transition hover:bg-[#2E7D32] hover:text-white hover:border-[#2E7D32]"
          onClick={() => {
            localStorage.setItem("hasSeenESG", "true"); // 사용자가 ESGPage를 본 기록 저장
            navigate("/login"); // 로그인 페이지로 이동
          }}
        >
          Login
        </button>
      </div>
    </div>
  );
};

export default ESGPage;
