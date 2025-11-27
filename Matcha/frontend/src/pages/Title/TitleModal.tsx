// src/pages/Title/TitleModal.tsx
import React from "react";
import { motion, AnimatePresence } from "framer-motion"; // 부드러운 등장 애니메이션

interface TitleModalProps {
  titles: string[];
  onClose: () => void;
}

export default function TitleModal({ titles, onClose }: TitleModalProps) {
  return (
    <AnimatePresence>
      <motion.div
        className="fixed inset-0 flex items-center justify-center bg-black/40 backdrop-blur-sm z-50"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
      >
        <motion.div
          className="bg-white rounded-2xl shadow-xl border border-gray-100 w-96 p-8 text-center"
          initial={{ scale: 0.9, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.9, opacity: 0 }}
          transition={{ duration: 0.25 }}
        >
          {/* 제목 */}
          <h2 className="text-2xl font-bold text-main-green mb-4">
            칭호를 획득 하였습니다!
          </h2>

          {/* 칭호 목록 */}
          <div className="space-y-2 mb-6">
            {titles.map((t) => (
              <p
                key={t}
                className="text-gray-800 font-semibold py-2"
              >
                🐸 {t} 🐸
              </p>
            ))}
          </div>

          {/* 닫기 버튼 */}
          <button
            onClick={onClose}
            className="bg-main-green hover:bg-green-700 text-white font-semibold px-6 py-2 rounded-md shadow-sm transition-colors duration-200"
          >
            닫기
          </button>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}
