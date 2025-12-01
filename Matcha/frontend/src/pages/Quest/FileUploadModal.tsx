// src/pages/Quest/FileUploadModal.tsx
import React, { useState, useRef, DragEvent, ChangeEvent } from "react";
import uploadApi from "@/api/uploadApi";

interface FileUploadModalProps {
  questId: number; // 수정 이유 : QuestPage에서 동적으로 questId를 전달받기 위해 추가
  onClose: () => void;
  onTitleEarned?: (titles: string[]) => void; // 부모에게 칭호 전달 콜백
}

const FileUploadModal: React.FC<FileUploadModalProps> = ({
  questId, // 수정 이유 : props로 전달된 questId 사용
  onClose,
  onTitleEarned,
}) => {
  const [isDragOver, setIsDragOver] = useState(false);
  const [uploadedFile, setUploadedFile] = useState<File | null>(null);
  const [result, setResult] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragOver(true);
  };

  const handleDragLeave = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragOver(false);
  };

  const handleDrop = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragOver(false);
    const files = Array.from(e.dataTransfer.files);
    if (files.length > 0) setUploadedFile(files[0]);
  };

  const handleFileSelect = (e: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (files.length > 0) setUploadedFile(files[0]);
  };

  const removeFile = () => {
    setUploadedFile(null);
    setResult("");
  };

  /** ✅ AI 분석 */
  const handleAnalyze = async () => {
    if (!uploadedFile) {
      alert("파일을 먼저 업로드해주세요!");
      return;
    }

    setLoading(true);
    setResult("");

    try {
      const formData = new FormData();
      const userId = localStorage.getItem("userId") || "1";
      formData.append("question", "이 사진이 텀블러, 머그컵, 일회용컵, 모두 아님 중 무엇인가요?");
      formData.append("attach", uploadedFile);
      formData.append("questId", String(questId)); // 수정 이유 : localStorage의 todayQuestId 하드코딩을 제거하고 props로 받은 questId 사용
      formData.append("userId", String(Number(userId)));;

      const response = await uploadApi.post("/ai/image-analysis", formData);

      const textResult =
        typeof response.data === "string" ? response.data : response.data?.result;
      const cleanText = textResult?.replace(/(^\"|\"$)/g, "").trim() ?? "";

      setResult(cleanText || "결과를 해석할 수 없습니다.");
    } catch (err) {
      console.error("AI 분석 오류:", err);
      setResult("AI 분석 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  /** ✅ 퀘스트 완료 후 부모에게 알림 */
  const handleQuestComplete = async () => {
    try {
      const response = await uploadApi.post(`/quest/${questId}/submit`); // 수정 이유 : 하드코딩된 questId(1) 제거, props로 전달받은 questId 사용
      const data = response.data;

      if (data.success) {
        alert("🎉 퀘스트가 완료되었습니다!");

        const titles = data.data?.newTitles || [];
        if (titles.length > 0 && onTitleEarned) {
          onTitleEarned(titles); // 부모에게 새 칭호 전달
        }

        onClose(); // 파일 업로드 모달 닫기
      } else {
        alert("퀘스트 완료에 실패했습니다.");
      }
    } catch (error) {
      console.error("퀘스트 완료 오류:", error);
      alert("퀘스트 완료 중 문제가 발생했습니다.");
    }
  };

  return (
    <div className="fixed inset-0 bg-[#E8F5E9]/80 flex items-center justify-center z-50">
      <div className="bg-white border border-gray-200 rounded-md shadow-md w-full max-w-2xl mx-4 p-8 relative max-h-[90vh] overflow-y-auto">
        {/* 헤더 */}
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-main-green flex-1 text-center">
            🌿 AI 이미지 분석
          </h2>
          <button
            onClick={onClose}
            className="absolute right-4 top-4 text-gray-500 hover:text-main-green text-2xl font-bold"
          >
            ×
          </button>
        </div>

        {/* 드래그 앤 드롭 */}
        <div
          className={`border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors duration-200 ${
            isDragOver
              ? "border-main-green bg-green-50"
              : "border-gray-300 hover:border-main-green"
          }`}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
        >
          <div className="text-gray-500">
            <svg
              className="mx-auto h-12 w-12 mb-4 text-main-green"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
              />
            </svg>
            <p className="text-lg mb-2 font-medium text-gray-700">
              파일을 드래그하거나 클릭하여 선택하세요
            </p>
            <p className="text-sm text-gray-400">
              텀블러 / 머그컵 / 일회용컵 이미지를 업로드해주세요
            </p>
          </div>
        </div>

        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          onChange={handleFileSelect}
          className="hidden"
        />

        {/* 업로드 미리보기 */}
        {uploadedFile && (
          <div className="mt-6">
            <h3 className="text-lg font-semibold mb-3 text-main-green text-center">
              업로드된 이미지
            </h3>
            <div className="bg-gray-50 border border-gray-200 p-4 rounded-lg">
              <div className="flex items-start justify-between mb-3">
                <div>
                  <p className="text-sm font-medium text-gray-700">{uploadedFile.name}</p>
                  <p className="text-xs text-gray-500">
                    ({(uploadedFile.size / 1024 / 1024).toFixed(2)} MB)
                  </p>
                </div>
                <button
                  onClick={removeFile}
                  className="text-red-500 hover:text-red-700 text-sm font-medium"
                >
                  삭제
                </button>
              </div>
              <div className="flex justify-center">
                <img
                  src={URL.createObjectURL(uploadedFile)}
                  alt="업로드된 이미지"
                  className="max-w-full max-h-64 rounded-lg shadow-sm object-contain"
                />
              </div>
            </div>
          </div>
        )}

        {/* 버튼 */}
        <div className="flex gap-3 mt-8">
          <button
            onClick={handleAnalyze}
            disabled={loading}
            className={`flex-1 font-semibold py-3 px-6 rounded-md transition-colors duration-200 ${
              loading
                ? "bg-gray-400 cursor-not-allowed text-white"
                : "bg-main-green hover:bg-green-700 text-white"
            }`}
          >
            {loading ? "분석 중..." : "AI 분석 시작"}
          </button>
          <button
            onClick={onClose}
            className="px-6 py-3 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors duration-200"
          >
            닫기
          </button>
        </div>

        {/* 결과 */}
        {result && (
          <div className="mt-8 text-center">
            <h3 className="text-lg font-semibold text-gray-800 mb-2">분석 결과</h3>
            <p className="text-main-green text-xl font-bold">{result}</p>

            {!(result.includes("모두아님")||result.includes("모두 아님")||result.includes("일회용컵")) && (
              <button
                onClick={handleQuestComplete}
                className="mt-5 bg-green-600 hover:bg-green-700 text-white py-2 px-6 rounded-md"
              >
                🎯 퀘스트 완료하기
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default FileUploadModal;
