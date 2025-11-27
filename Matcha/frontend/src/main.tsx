// ✅ React import보다 위에 둔다
const token = localStorage.getItem("token");
if (token) {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    const exp = payload.exp * 1000;
    if (Date.now() > exp) {
      console.warn("토큰 만료 → 자동 로그아웃 처리됨");
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      localStorage.removeItem("userRole");

      // ✅ React Router가 인식하지 못할 수 있으니 페이지 전체 새로고침
      window.location.replace("/login");
    }
  } catch (err) {
    console.error("토큰 파싱 실패:", err);
    localStorage.clear();
  }
}

import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
