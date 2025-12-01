import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

// 개발 서버용 백엔드 주소 (Vite proxy는 ONLY 개발 모드에서만 사용됨)
const backendTarget =
  process.env.NODE_ENV === "production"
    ? "" // 배포모드에서는 proxy가 아예 필요없음
    : "http://localhost:8080";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { "@": path.resolve(__dirname, "src") },
  },

  server: {
    port: 5173,

    // 🔥 proxy는 개발환경에서만 동작
    proxy:
      backendTarget !== ""
        ? {
            "/api": {
              target: backendTarget,
              changeOrigin: true,
              secure: false,
            },
            "/uploads": {
              target: backendTarget,
              changeOrigin: true,
              secure: false,
            },
          }
        : undefined,
  },
});
