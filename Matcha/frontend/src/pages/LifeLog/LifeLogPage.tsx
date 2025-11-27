// src/pages/Record/LifeLogPage.tsx
import React, { useState, useEffect, useCallback } from "react";
import { useParams } from "react-router-dom";
import {
  getMyLogs,
  addLog as addLifeLog,
  updateLog as updateLifeLog,
  deleteLog as deleteLifeLog,
} from "@/api/lifelogApi";

// --- Icon Components ---
const FiTrash2 = ({ size = 18, ...props }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24"
    fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...props}>
    <polyline points="3 6 5 6 21 6" />
    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
    <line x1="10" y1="11" x2="10" y2="17" />
    <line x1="14" y1="11" x2="14" y2="17" />
  </svg>
);

const FiClock = ({ size = 32, ...props }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24"
    fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...props}>
    <circle cx="12" cy="12" r="10" />
    <polyline points="12 6 12 12 16 14" />
  </svg>
);

const FiPlusCircle = (props: any) => (
  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
    viewBox="0 0 24 24" fill="none" stroke="currentColor"
    strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...props}>
    <circle cx="12" cy="12" r="10" />
    <line x1="12" y1="8" x2="12" y2="16" />
    <line x1="8" y1="12" x2="16" y2="12" />
  </svg>
);

const FiEdit2 = ({ size = 18, ...props }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size}
    viewBox="0 0 24 24" fill="none" stroke="currentColor"
    strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...props}>
    <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z" />
  </svg>
);

const FiCheckSquare = ({ size = 18, ...props }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size}
    viewBox="0 0 24 24" fill="none" stroke="currentColor"
    strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...props}>
    <polyline points="9 11 12 14 22 4" />
    <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
  </svg>
);

// --- 타입 ---
type EsgCategory = "E" | "S" | "G";
interface LifeLog {
  logId: number;
  content: string;
  loggedAt: string;
  category: EsgCategory;
  esgScoreEffect: number;
}

// --- 커스텀 훅 ---
const useLifeLogs = (date: string) => {
  const [logs, setLogs] = useState<LifeLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [currentPage, setCurrentPage] = useState(1);
  const logsPerPage = 5;

  const fetchLogs = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await getMyLogs(date);
      setLogs(
        data.sort((a, b) =>
          new Date(b.loggedAt).getTime() - new Date(a.loggedAt).getTime()
        )
      );
    } catch {
      setError("기록을 불러오는 중 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  }, [date]);

  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  const totalPages = Math.ceil(logs.length / logsPerPage);
  const startIdx = (currentPage - 1) * logsPerPage;
  const endIdx = startIdx + logsPerPage;
  const currentLogs = logs.slice(startIdx, endIdx);

  const handlePageChange = (page: number) => {
    if (page >= 1 && page <= totalPages) setCurrentPage(page);
  };

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  }, [currentPage]);

  const addLogFn = async (content: string) => {
    await addLifeLog(content, "E");
    await fetchLogs();
  };

  const deleteLogFn = async (logId: number) => {
    await deleteLifeLog(logId);
    await fetchLogs();
  };

  const updateLogFn = async (logId: number, content: string) => {
    await updateLifeLog(logId, content, "E");
    await fetchLogs();
  };

  return { logs, isLoading, error, addLogFn, deleteLogFn, updateLogFn, currentLogs, totalPages, currentPage, handlePageChange };
};

// --- 입력 폼 ---
const LogInputForm: React.FC<{
  onSave: (content: string) => Promise<void>;
  selectedDate: string;
}> = ({ onSave, selectedDate }) => {
  const [content, setContent] = useState("");
  const [isSaving, setIsSaving] = useState(false);

  const handleSave = async () => {
    // 🚫 공백, 줄바꿈, 탭만 있는 경우 저장 방지
    if (!content.replace(/\s/g, "").length) {
      alert("⚠️ 기록할 내용을 입력해주세요.");
      return;
    }

    const todayStr = new Date().toISOString().split("T")[0];
    if (selectedDate < todayStr) {
      alert("⚠️ 과거 날짜에는 새로운 기록을 추가할 수 없습니다.");
      return;
    }
    if (selectedDate > todayStr) {
      alert("⚠️ 미래 날짜에는 기록을 남길 수 없습니다.");
      return;
    }

    setIsSaving(true);
    try {
      await onSave(content.trim());
      setContent("");
    } catch (e) {
      alert((e as Error).message);
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-md mb-8">
      <textarea
        className="w-full p-3 border border-[#1565C0] rounded-md text-black placeholder-[#2E7D32] focus:text-black focus:ring-2 focus:ring-black focus:border-black"
        placeholder="오늘의 활동을 기록해보세요..."
        value={content}
        onChange={(e) => setContent(e.target.value)}
        maxLength={255}
        rows={4}
        disabled={isSaving}
      />
      <button
        onClick={handleSave}
        disabled={isSaving}
        className="w-full mt-4 bg-[#66BB6A] hover:bg-[#2E7D32] text-white font-bold py-3 px-4 rounded-lg flex items-center justify-center"
      >
        <FiPlusCircle className="mr-2" />
        {isSaving ? "저장 중..." : "기록 저장하기"}
      </button>
    </div>
  );
};

// --- 기록 항목 ---
const LogItem: React.FC<{
  log: LifeLog;
  onUpdate: (id: number, newContent: string) => Promise<void>;
  onDelete: (id: number) => Promise<void>;
}> = ({ log, onUpdate, onDelete }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [edited, setEdited] = useState(log.content);

  // ✅ 오늘 날짜 문자열
  const todayStr = new Date().toISOString().split("T")[0];
  // ✅ 로그 날짜가 오늘보다 이전인지 판별
  const isPast = log.loggedAt.split("T")[0] < todayStr;

  const handleSave = async () => {
    if (!edited.replace(/\s/g, "").length) {
      alert("⚠️ 기록할 내용을 입력해주세요.");
      return;
    }
    await onUpdate(log.logId, edited);
    setIsEditing(false);
  };

  return (
    <div className="bg-white p-4 rounded-lg shadow-sm hover:shadow-md transition-shadow mb-4">
      <div className="flex justify-between items-center">
        {isEditing ? (
          <textarea
            value={edited}
            onChange={(e) => setEdited(e.target.value)}
            className="border border-black text-black rounded-md flex-grow focus:ring-2 focus:ring-black focus:border-black"
          />
        ) : (
          <p className="text-black flex-grow whitespace-pre-wrap">
            {log.content}
          </p>
        )}

        {/* ✅ 오늘 날짜일 때만 수정/삭제 버튼 표시 */}
        {!isPast && (
          <div className="ml-4 flex gap-2">
            {isEditing ? (
              <>
                <button onClick={handleSave}><FiCheckSquare /></button>
                <button onClick={() => setIsEditing(false)}><FiTrash2 /></button>
              </>
            ) : (
              <>
                <button onClick={() => setIsEditing(true)}><FiEdit2 /></button>
                <button onClick={() => onDelete(log.logId)}><FiTrash2 /></button>
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
};


// --- 메인 페이지 ---
const LifeLogPage: React.FC = () => {
  const { date: paramDate } = useParams<{ date?: string }>();
  const today = new Date();
  const getLocalDate = () => {
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, "0");
    const day = String(today.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };

  const [date, setDate] = useState(paramDate || getLocalDate());
  const { logs, isLoading, error, addLogFn, deleteLogFn, updateLogFn, currentLogs, totalPages, currentPage, handlePageChange } =
    useLifeLogs(date);

  useEffect(() => {
    if (paramDate) setDate(paramDate);
  }, [paramDate]);

  return (
    <div className="bg-[#E8F5E9] min-h-screen p-4 sm:p-6 lg:p-8">
      <div className="max-w-3xl mx-auto">
        <header className="mb-8">
          <h1 className="text-4xl font-bold text-black mb-2">활동 기록</h1>
          <p className="text-[#1565C0]">ESG 활동을 기록하고 포인트를 쌓아보세요.</p>
        </header>

        <div className="mb-6">
          <label htmlFor="date" className="block text-sm font-medium text-[#2E7D32] mb-1">
            날짜 선택
          </label>
          <input
            id="date"
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="p-2 border border-[#1565C0] rounded-md shadow-sm"
          />
        </div>

        <LogInputForm onSave={addLogFn} selectedDate={date} />

        <main>
          {isLoading ? (
            <p className="text-[#1565C0] text-center py-6">로딩 중...</p>
          ) : error ? (
            <p className="text-red-500 text-center py-6">{error}</p>
          ) : logs.length === 0 ? (
            <div className="text-center p-10 bg-[#E3F2FD] rounded-lg">
              <FiClock size={32} className="mx-auto text-[#1565C0] mb-2" />
              <p>해당 날짜에 기록이 없습니다.</p>
            </div>
          ) : (
            currentLogs.map((log) => (
              <LogItem
                key={log.logId}
                log={log}
                onUpdate={updateLogFn}
                onDelete={deleteLogFn}
              />
            ))
          )}

          {/* ✅ 페이지네이션 */}
          {totalPages > 1 && (
            <div className="flex justify-center items-center mt-8 space-x-2">
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 1}
                className={`px-3 py-2 rounded-md border ${
                  currentPage === 1
                    ? "bg-gray-100 text-gray-400 cursor-not-allowed"
                    : "hover:bg-gray-200"
                }`}
              >
                이전
              </button>

              {[...Array(totalPages)].map((_, i) => (
                <button
                  key={i}
                  onClick={() => handlePageChange(i + 1)}
                  className={`px-3 py-2 rounded-md border ${
                    currentPage === i + 1
                      ? "bg-blue-600 text-white"
                      : "hover:bg-gray-100"
                  }`}
                >
                  {i + 1}
                </button>
              ))}

              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
                className={`px-3 py-2 rounded-md border ${
                  currentPage === totalPages
                    ? "bg-gray-100 text-gray-400 cursor-not-allowed"
                    : "hover:bg-gray-200"
                }`}
              >
                다음
              </button>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

export default LifeLogPage;