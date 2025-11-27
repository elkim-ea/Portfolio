// src/pages/Company/CompanyPage.tsx
import React, { useEffect, useState } from "react";
import { companyApi } from "@/api/companyApi";
import type { Company } from "@/api/companyApi";
import.meta.env.VITE_API_BASE_URL;

const CompanyPage: React.FC = () => {
  const [selectedTier, setSelectedTier] = useState<"LEADER" | "SPONSOR">(
    "LEADER"
  );
  const [companies, setCompanies] = useState<Company[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 기업 데이터 요청 함수
  const fetchCompanies = async () => {
    setLoading(true);
    setError(null);

    try {
      const res =
        selectedTier === "LEADER"
          ? await companyApi.getLeading()
          : await companyApi.getSponsor();

      if (res.success) {
        setCompanies(res.data || []);
      } else {
        setError(res.message || "기업 정보를 불러오지 못했습니다.");
      }
    } catch (err) {
      console.error(err);
      setError("기업 정보를 불러오는 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  // 탭 변경 시 데이터 새로 요청
  useEffect(() => {
    fetchCompanies();
  }, [selectedTier]);

  return (
    <div className="min-h-[calc(100vh-60px)] bg-[#E8F5E9] flex flex-col items-center py-12 px-6">
      {/* 상단 제목 + 탭 */}
      <section className="bg-white/80 backdrop-blur-md shadow-md rounded-2xl px-10 py-8 w-full max-w-5xl text-center border border-[#E0E0E0]/50">
        <h2 className="text-3xl font-extrabold text-[#2E7D32] mb-6 tracking-tight">
          참여 기업 목록
        </h2>

        <div className="flex justify-center gap-4">
          {[
            { id: "LEADER", label: "🌱 ESG 선도 기업" },
            { id: "SPONSOR", label: "🤝 후원 기업" },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setSelectedTier(tab.id as "LEADER" | "SPONSOR")}
              className={`px-6 py-2.5 rounded-full font-semibold text-sm shadow-sm transition-all duration-200 ${
                selectedTier === tab.id
                  ? "bg-[#66BB6A] text-white scale-[1.03]"
                  : "bg-[#FDFCF9] text-[#2E7D32] border border-[#A5D6A7] hover:bg-[#E8F5E9]"
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </section>

      {/* 기업 리스트 */}
      <section className="mt-10 bg-white/80 backdrop-blur-md rounded-2xl shadow-md p-10 w-full max-w-6xl border border-[#E0E0E0]/50">
        {loading ? (
          <p className="text-gray-500 text-center">불러오는 중...</p>
        ) : error ? (
          <p className="text-red-500 text-center">{error}</p>
        ) : (
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8 place-items-center">
            {companies.map((company) => (
              <div
                key={company.companyId}
                onClick={() => window.open(company.companyWebsiteUrl, "_blank")}
                className="group relative w-full max-w-[220px] h-[180px] rounded-2xl bg-[#E8F5E9] border border-[#E0E0E0]/60 flex flex-col items-center justify-center shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300 cursor-pointer"
              >
                {company.companyLogo ? (
                  <img
                    src={company.companyLogo}
                    alt={`${company.companyName} 로고`}
                    className="w-16 h-16 object-contain mb-3 opacity-90 group-hover:opacity-100 transition"
                  />
                ) : (
                  <div className="w-16 h-16 bg-white border border-[#A5D6A7] rounded-full flex items-center justify-center mb-2">
                    <span className="text-sm text-black/70">No Logo</span>
                  </div>
                )}
                <span className="text-base font-semibold text-[#2E7D32]">
                  {company.companyName}
                </span>
                <div className="absolute inset-0 rounded-2xl bg-[#66BB6A]/5 opacity-0 group-hover:opacity-100 transition-all duration-300"></div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
};

export default CompanyPage;
