// src/router/AppRouter.tsx
import React, { useState, useEffect } from "react";
import { BrowserRouter, Routes, Route, Navigate, useLocation, useNavigate } from "react-router-dom";

// 일반 사용자 페이지
import HomePage from "../pages/Home/HomePage";
import QuestPage from "../pages/Quest/QuestPage";
import CompanyPage from "../pages/Company/CompanyPage";
import Layout from "../components/layout/Layout";
import LoginForm from "../pages/Auth/LoginForm";
import SignupForm from "../pages/Auth/SignupForm";
import TermsPage from "../pages/Auth/TermsPage";
import ForgotPasswordPage from "../pages/Auth/ForgotPasswordPage";
import ESGPage from "../pages/ESG/ESGPage";
import LifeLogPage from "../pages/LifeLog/LifeLogPage";
import ActivityPage from "../pages/Activity/ActivityPage";
import RankingPage from "../pages/Ranking/RankingPage";
import MyPage from "../pages/My/MyPage";

// 관리자 페이지
import AdminLayout from "../pages/Admin/AdminLayout";
import UserManagement from "../pages/Admin/UserManagement";
import QuestManagement from "../pages/Admin/QuestManagement";
import TitleManagement from "../pages/Admin/TitleManagement";
import CompanyManagement from "../pages/Admin/CompanyManagement";

interface AuthorizationProps {
  redirectTo: string;
  children: React.ReactNode;
  requireAdmin?: boolean;
}

interface PublicOnlyProps {
  redirectTo: string;
  children: React.ReactNode;
}

export default function AppRouter(): JSX.Element {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [userRole, setUserRole] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const hasSeenESG = localStorage.getItem("hasSeenESG");

  // 토큰 유효성 검사 및 로그인 상태 감시
  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem("token");
      const role = localStorage.getItem("userRole");

      if (token) {
        try {
          const payload = JSON.parse(atob(token.split(".")[1]));
          const exp = payload.exp * 1000;

          if (Date.now() > exp) {
            console.warn("토큰 만료 → 자동 로그아웃 처리됨");
            localStorage.removeItem("token");
            localStorage.removeItem("user");
            localStorage.removeItem("userRole");
            setIsAuthenticated(false);
            setUserRole(null);
          } else {
            setIsAuthenticated(true);
            setUserRole(role);
          }
        } catch (err) {
          console.error("토큰 파싱 실패:", err);
          localStorage.clear();
          setIsAuthenticated(false);
          setUserRole(null);
        }
      } else {
        setIsAuthenticated(false);
        setUserRole(null);
      }
      setLoading(false);
    };

    checkAuth();

    const handleStorageChange = () => checkAuth();
    window.addEventListener("storage", handleStorageChange);

    const interval = setInterval(checkAuth, 5000);

    return () => {
      window.removeEventListener("storage", handleStorageChange);
      clearInterval(interval);
    };
  }, []);

  // 인증 보호용 컴포넌트
  const Authorization = ({
    redirectTo,
    children,
    requireAdmin = false,
  }: AuthorizationProps): JSX.Element => {
    const navigate = useNavigate();
    const location = useLocation();
    const [hasShownAlert, setHasShownAlert] = useState(false);

    // ✅ 권한 변경 감지
    useEffect(() => {
      if (!loading && isAuthenticated) {
        const currentRole = localStorage.getItem("userRole");
        const isAdminPage = location.pathname.startsWith("/admin");

        // 관리자 페이지인데 권한이 USER로 변경된 경우
        if (isAdminPage && currentRole !== "ADMIN" && !hasShownAlert) {
          setHasShownAlert(true);
          alert("관리자 권한이 변경되었습니다. 메인 페이지로 이동합니다.");
          navigate("/home", { replace: true });
        }
      }
    }, [userRole, isAuthenticated, loading, location.pathname, navigate, hasShownAlert]);


    if (loading) {
      return (
        <div className="flex items-center justify-center min-h-screen">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#66BB6A]" />
        </div>
      );
    }

    const token = localStorage.getItem("token");
    if (!token) {
      console.warn("인증되지 않은 사용자 → 로그인 페이지로 이동");
      localStorage.clear();
      return <Navigate to={redirectTo} replace />;
    }

    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      const exp = payload.exp * 1000;
      if (Date.now() > exp) {
        console.warn("토큰 만료 → 자동 로그아웃 처리됨");
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        localStorage.removeItem("userRole");
        return <Navigate to={redirectTo} replace />;
      }
    } catch (err) {
      console.error("토큰 파싱 실패:", err);
      localStorage.clear();
      return <Navigate to={redirectTo} replace />;
    }

    if (requireAdmin && userRole !== "ADMIN") {
      alert("관리자만 접근할 수 있습니다.");
      return <Navigate to="/home" replace />;
    }

    return <>{children}</>;
  };

  // 로그인 상태일 경우 접근 차단 (회원가입, 로그인)
  const PublicOnly = ({ redirectTo, children }: PublicOnlyProps): JSX.Element => {
    if (loading) {
      return (
        <div className="flex items-center justify-center min-h-screen">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#66BB6A]" />
        </div>
      );
    }

    if (isAuthenticated) {
      return <Navigate to={redirectTo} replace />;
    }

    return <>{children}</>;
  };

  // 로딩 중 스피너 표시
  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#66BB6A]" />
      </div>
    );
  }

  // 실제 라우팅 정의
  return (
    <BrowserRouter>
      <Routes>
        {/* 루트 접근 시 분기 */}
        <Route
          path="/"
          element={
            isAuthenticated
              ? <Navigate to="/home" replace />
              : <Navigate to={hasSeenESG ? "/login" : "/esg"} replace />
          }
        />

        {/* ESG 소개 페이지 */}
        <Route path="/esg" element={<ESGPage />} />

        {/* 로그인 및 회원가입 관련 페이지 */}
        <Route
          path="/login"
          element={
            <PublicOnly redirectTo="/home">
              <LoginForm />
            </PublicOnly>
          }
        />
        <Route
          path="/signup"
          element={
            <PublicOnly redirectTo="/home">
              <SignupForm />
            </PublicOnly>
          }
        />
        <Route path="/terms" element={<TermsPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />

        {/* 일반 사용자 전용 페이지 */}
        <Route
          element={
            <Authorization redirectTo="/login">
              <Layout />
            </Authorization>
          }
        >
          <Route path="/home" element={<HomePage />} />
          <Route path="/quest" element={<QuestPage />} />
          <Route path="/lifelog" element={<LifeLogPage />} />
          <Route path="/activity" element={<ActivityPage />} />
          <Route path="/ranking" element={<RankingPage />} />
          <Route path="/company" element={<CompanyPage />} />
          <Route path="/mypage" element={<MyPage />} />
          
          {/* 날짜별 기록 페이지 */}
          <Route path="/record/:date" element={<LifeLogPage />} />
        </Route>

        {/* 관리자 전용 페이지 */}
        <Route
          path="/admin"
          element={
            <Authorization redirectTo="/login" requireAdmin={true}>
              <AdminLayout />
            </Authorization>
          }
        >
          <Route index element={<Navigate to="/admin/users" replace />} />
          <Route path="users" element={<UserManagement />} />
          <Route path="quests" element={<QuestManagement />} />
          <Route path="titles" element={<TitleManagement />} />
          <Route path="companies" element={<CompanyManagement />} />
        </Route>

        {/* 잘못된 경로 처리 */}
        <Route
          path="*"
          element={
            isAuthenticated
              ? <Navigate to="/home" replace />
              : <Navigate to={hasSeenESG ? "/login" : "/esg"} replace />
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
