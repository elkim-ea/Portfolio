import React from "react";
import { NavLink, useNavigate } from "react-router-dom";

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({ isOpen, onClose }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/login");
    alert(`로그아웃 되었습니다.`);
    window.location.href = '/login';
  };

  return (

    <aside
      className={`fixed top-[60px] left-0 h-[calc(100vh-60px)] bg-[#E8F5E9]
        flex flex-col justify-between overflow-hidden
        transition-transform duration-300 z-50
        ${isOpen ? "translate-x-0 w-[220px]" : "-translate-x-full w-[220px]"}`}
      onMouseLeave={onClose}
    >

      {/* 메뉴 리스트 */}
      <ul className="flex flex-col items-center list-none p-0 m-0 pt-5">
        {[
          { to: "/lifelog", label: "기록하기" },
          { to: "/activity", label: "나의 활동" },
          { to: "/quest", label: "퀘스트 목록" },
          { to: "/ranking", label: "랭킹보기" },
          { to: "/company", label: "기업 보기" },
        ].map((item) => (
          <li key={item.to} className="my-5">
            <NavLink
              to={item.to}
              className={({ isActive }) =>
                isActive
                  ? "no-underline text-[#2E7D32] text-[18px] font-bold transition-all duration-300"
                  : "no-underline text-black text-[18px] font-medium hover:text-[#66BB6A] hover:scale-105 transition-all duration-300"
              }
            >
              {item.label}
            </NavLink>
          </li>
        ))}
      </ul>

      {/* footer */}
      <div className="text-sm font-normal border-t border-none text-black flex gap-4 justify-center items-center py-4">
        <NavLink
          to="/mypage"
          className="no-underline text-inherit text-sm cursor-pointer transition-all duration-200 hover:text-[#66BB6A] hover:font-bold"
        >
          마이페이지
        </NavLink>
        <span className="text-black">|</span>
        <button
          onClick={handleLogout}
          className="bg-transparent border-none text-sm cursor-pointer transition-all duration-200 hover:text-[#66BB6A] hover:font-bold"
        >
          로그아웃
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
