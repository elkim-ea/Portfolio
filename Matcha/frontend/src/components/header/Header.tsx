import React, { useState, useEffect } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { FiMenu } from "react-icons/fi";

interface HeaderProps {
  onToggleSidebar: () => void;
}

const Header: React.FC<HeaderProps> = ({ onToggleSidebar }) => {
  const [isAdmin, setIsAdmin] = useState<boolean>(false);
  const navigate = useNavigate();

  useEffect(() => {
    const role = localStorage.getItem('userRole');
    setIsAdmin(role === 'ADMIN');
  }, []);

  return (
    <header
      className="fixed top-0 left-0 right-0 h-[60px]
                 bg-gradient-to-r from-[#66BB6A] to-[#43A047]
                 flex items-center justify-between px-6
                 text-black shadow-md z-[1000]"
    >
      {/* 햄버거 버튼 */}
      <button
        onClick={onToggleSidebar}
        className="hover:opacity-80 transition-opacity duration-200"
      >
        <FiMenu size={26} />
      </button>

      {isAdmin &&
        <button
          onClick={() => navigate('/admin')}
          className="ml-auto px-4 py-2 text-sm font-medium text-white bg-[#2E7D32] rounded-md hover:bg-[#1B5E20] transition-colors"
        >
          관리자 페이지로
        </button>
      }
      {/* 로고 */}
      <h1 className="ml-3 text-[20px] font-bold tracking-wide cursor-pointer select-none">
        <NavLink
          to="/"
          className="no-underline text-black hover:opacity-80 transition-opacity duration-200"
        >
          Matcha World
        </NavLink>
      </h1>
    </header>
  );
};

export default Header;
