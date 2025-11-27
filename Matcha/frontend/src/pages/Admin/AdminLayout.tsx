// src/pages/Admin/AdminLayout.tsx
import React, { useState } from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { FiMenu } from 'react-icons/fi';

const AdminLayout: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  const menuItems = [
    { path: '/admin/users', label: '사용자 관리', icon: '👥' },
    { path: '/admin/quests', label: '퀘스트 관리', icon: '🎯' },
    { path: '/admin/titles', label: '칭호 관리', icon: '🏆' },
    { path: '/admin/companies', label: '기업 관리', icon: '🏢' },
  ];

  const isActive = (path: string) => location.pathname === path;

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem("user");
    localStorage.removeItem('userRole');
    navigate('/login');
    alert(`로그아웃 되었습니다.`);
    window.location.href = '/login';
  };

  return (
    <div className="min-h-screen bg-[#FDFCF9]">
      {/* 헤더 - 기존 프로젝트 스타일 적용 */}
      <header className="fixed top-0 left-0 right-0 h-[60px] bg-[#66BB6A] flex items-center px-5 z-[1000]">
        <button
          onClick={() => setIsSidebarOpen(!isSidebarOpen)}
          className="bg-transparent border-none outline-none cursor-pointer mr-4 flex items-center justify-center text-black focus:outline-none active:outline-none"
        >
          <FiMenu size={30} />
        </button>
        <h1 className="text-[20px] font-bold text-black">
          관리자 페이지
        </h1>
        <button
          onClick={() => navigate('/home')}
          className="ml-auto px-4 py-2 text-sm font-medium text-white bg-[#2E7D32] rounded-md hover:bg-[#1B5E20] transition-colors"
        >
          사용자 페이지로
        </button>
        <button
          onClick={handleLogout}
          className="ml-3 px-4 py-2 text-sm font-medium text-white bg-red-500 rounded-md hover:bg-red-600 transition-colors"
        >
          로그아웃
        </button>
      </header>

      <div className="pt-[60px] flex">
        {/* 사이드바 */}
        <aside
          className={`fixed left-0 top-[60px] bottom-0 bg-white shadow-lg transition-all duration-300 ${
            isSidebarOpen ? 'w-64' : 'w-0'
          } overflow-hidden`}
        >
          <nav className="p-4">
            <ul className="space-y-2">
              {menuItems.map((item) => (
                <li key={item.path}>
                  <Link
                    to={item.path}
                    className={`flex items-center px-4 py-3 rounded-lg transition-colors ${
                      isActive(item.path)
                        ? 'bg-[#E8F5E9] text-[#2E7D32] font-bold'
                        : 'text-gray-700 hover:bg-[#F1F8E9] hover:text-[#66BB6A]'
                    }`}
                  >
                    <span className="text-2xl mr-3">{item.icon}</span>
                    <span>{item.label}</span>
                  </Link>
                </li>
              ))}
            </ul>
          </nav>
        </aside>

        {/* 메인 콘텐츠 */}
        <main
          className={`flex-1 transition-all duration-300 ${
            isSidebarOpen ? 'ml-64' : 'ml-0'
          }`}
        >
          <div className="p-6">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;