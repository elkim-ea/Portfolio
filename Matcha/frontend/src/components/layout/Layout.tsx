import { Outlet } from "react-router-dom";
import { useState } from "react";
import Sidebar from "../sidebar/Sidebar";
import Header from "../header/Header";
import Footer from "../footer/Footer";

const Layout = () => {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const toggleSidebar = () => setIsSidebarOpen(!isSidebarOpen);

  return (
    // ✅ 스크롤 유지하려면 h-screen 대신 min-h-screen + relative
    <div className="relative flex flex-col min-h-screen w-screen bg-[#FDFCF9] text-[#2E7D32]">
      {/* 상단 고정 헤더 */}
      <Header onToggleSidebar={toggleSidebar} />

      {/* 본문 */}
      <div className="flex flex-1 mt-[60px] transition-all duration-300">
        <Sidebar isOpen={isSidebarOpen} onClose={() => setIsSidebarOpen(false)} />
        <main
          className={`flex-1 overflow-y-auto transition-all duration-300 ${
            isSidebarOpen ? "ml-[220px]" : "ml-0"
          }`} // ✅ Footer 영역 확보 (스크롤 가능)
        >
          <Outlet />
        </main>
      </div>

      {/* ✅ 화면 하단 고정 푸터 */}
      <Footer />
    </div>
  );
};

export default Layout;