// src/pages/MyPage.tsx
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  userMyApi,
  UserProfile,
  TitleInfo,
  UserQuestInfo,
} from "../../api/userMyApi";
import EditProfileModal from "./EditProfileModal";
import DeleteAccountModal from "./DeleteAccountModal";

const MyPage: React.FC = () => {
  const navigate = useNavigate();
  const [profile, setProfile] = useState<UserProfile | null>();
  const [loading, setLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const data = await userMyApi.getProfile();
      let profileData = data;
      // if(!data.esgScore) profileData.esgScore = 0;
      // if(!data.eScore) profileData.eScore = 0;
      // if(!data.sScore) profileData.sScore = 0;

      setProfile(profileData);
    } catch (error: any) {
      console.error("프로필 조회 실패:", error);
      alert(error.message || "프로필을 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleSetMainTitle = async (titleId: number | null) => {
    try {
      await userMyApi.setMainTitle(titleId);
      alert("대표 칭호가 설정되었습니다.");
      fetchProfile();
    } catch (error: any) {
      alert(error.message || "대표 칭호 설정에 실패했습니다.");
    }
  };

  const handleDeleteAccount = () => {
    // 로그아웃 처리
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    localStorage.removeItem("userRole");
    navigate("/login");
    window.location.href = "/login";
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-gray-600">프로필을 불러올 수 없습니다.</p>
      </div>
    );
  }

  const earnedTitles = profile.titles.filter((t) => t.earned);
  const unearnedTitles = profile.titles.filter((t) => !t.earned);

  const pendingQuests =
    profile.userQuests?.filter((q) => q.status === "PENDING") || [];
  const completedQuests =
    profile.userQuests?.filter((q) => q.status === "SUCCESS") || [];
  const failedQuests =
    profile.userQuests?.filter((q) => q.status === "FAILED") || [];

  return (
    <div className="min-h-screen bg-white-green py-8">
      <div className="max-w-6xl mx-auto px-4">
        {/* 헤더 */}
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">마이페이지</h1>
          <button
            onClick={() => setShowEditModal(true)}
            className="px-4 py-2 bg-sub-green text-white rounded-lg hover:bg-main-green transition-colors"
          >
            회원정보 수정
          </button>
        </div>

        {/* 메인 콘텐츠 */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* 좌측: 프로필 요약 */}
          <div className="bg-white rounded-lg shadow p-6 h-full">
            <div className="text-center">
              <div className="w-24 h-24 mx-auto mb-4 bg-gray-200 rounded-full flex items-center justify-center">
                {/* <span className="text-4xl">👤</span> */}
                {/* 기존 코드: character 기준으로 분기 */}
                {/* {profile.character ? (
                  // <img src={profile.character} alt="캐릭터" className="max-h-full object-contain"/>
                  <img src={profile.characterImageUrl} alt="캐릭터" className="max-h-full object-contain"/>
                ) : (
                  <div className="text-center text-gray-400">
                    <span className="text-6xl">🌱</span>
                    <p className="mt-2">아직 캐릭터가 없어요! 열심히 포인트를 모아볼까요?</p>
                  </div>
                )} */}

                {/* 수정 이유: 기본 캐릭터(default.png)는 항상 표시하되 안내 문구는 숨기기 위해 조건 분리 */}
                {profile.characterImageUrl ? (
                  <img
                    src={profile.characterImageUrl}
                    alt="캐릭터"
                    className="max-h-full object-contain"
                  />
                ) : (
                  <div className="text-center text-gray-400">
                    <span className="text-6xl">🌱</span>
                    <p className="mt-2">
                      아직 캐릭터가 없어요! 열심히 포인트를 모아볼까요?
                    </p>
                  </div>
                )}
              </div>


              <h2 className="text-xl font-bold text-gray-900">
                {profile.nickname}
              </h2>
              <p className="text-sm text-gray-500">{profile.email}</p>
              {profile.mainTitleName && (
                <div className="mt-2">
                  <span className="inline-block px-3 py-1 bg-yellow-100 text-yellow-800 text-sm font-semibold rounded-full">
                    {profile.mainTitleName}
                  </span>
                </div>
              )}
            </div>

            <div className="mt-6 space-y-3">
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-600">ESG 점수</span>
                <span className="text-lg font-bold text-blue-600">
                  {profile.esgScore}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-600">E 점수</span>
                <span className="text-lg font-bold text-green-600">
                  {profile.eScore}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-600">S 점수</span>
                <span className="text-lg font-bold text-purple-600">
                  {profile.sScore}
                </span>
              </div>
            </div>
          </div>

          {/* 중앙: 칭호 */}
          <div className="lg:col-span-2 space-y-6">
            {/* 칭호 목록 */}
            <div className="bg-white rounded-lg shadow p-6 h-full">
              <h3 className="text-lg font-bold text-gray-900 mb-4">칭호</h3>

              {/* 획득한 칭호 */}
              <div className="mb-6">
                <h4 className="text-sm font-semibold text-gray-700 mb-3">
                  획득한 칭호 ({earnedTitles.length})
                </h4>
                {earnedTitles.length > 0 ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {earnedTitles.map((title) => (
                      <TitleCard
                        key={title.titleId}
                        title={title}
                        isActive={profile.mainTitleId === title.titleId}
                        onSelect={() => handleSetMainTitle(title.titleId)}
                      />
                    ))}
                  </div>
                ) : (
                  <p className="text-gray-500 text-sm">
                    아직 획득한 칭호가 없습니다.
                  </p>
                )}
              </div>

              {/* 미획득 칭호 */}
              <div>
                <h4 className="text-sm font-semibold text-gray-700 mb-3">
                  미획득 칭호 ({unearnedTitles.length})
                </h4>
                {unearnedTitles.length > 0 ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {unearnedTitles.map((title) => (
                      <TitleCard
                        key={title.titleId}
                        title={title}
                        isActive={false}
                        onSelect={() => {}}
                        disabled
                      />
                    ))}
                  </div>
                ) : (
                  <p className="text-gray-500 text-sm">
                    모든 칭호를 획득하셨습니다!
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* 회원 탈퇴 버튼 */}
        <div className="mt-6 flex justify-end">
          <button
            onClick={() => setShowDeleteModal(true)}
            className="px-4 py-2 text-sm text-red-600 hover:text-red-800 hover:underline"
          >
            회원 탈퇴
          </button>
        </div>
      </div>

      {/* 회원정보 수정 모달 */}
      {showEditModal && (
        <EditProfileModal
          profile={profile}
          onClose={() => setShowEditModal(false)}
          onSuccess={() => {
            setShowEditModal(false);
            fetchProfile();
          }}
        />
      )}

      {/* 회원 탈퇴 모달 */}
      {showDeleteModal && (
        <DeleteAccountModal
          onClose={() => setShowDeleteModal(false)}
          onSuccess={handleDeleteAccount}
        />
      )}
    </div>
  );
};

// 칭호 카드 컴포넌트
interface TitleCardProps {
  title: TitleInfo;
  isActive: boolean;
  onSelect: () => void;
  disabled?: boolean;
}

const TitleCard: React.FC<TitleCardProps> = ({
  title,
  isActive,
  onSelect,
  disabled = false,
}) => {
  return (
    <div
      className={`border rounded-lg p-4 transition-all ${
        disabled
          ? "bg-gray-50 border-gray-200 opacity-50"
          : isActive
          ? "bg-yellow-50 border-yellow-400 shadow-md"
          : "bg-white border-gray-300 hover:border-blue-400 hover:shadow cursor-pointer"
      }`}
      onClick={!disabled ? onSelect : undefined}
    >
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center space-x-2">
            <h5 className="font-bold text-gray-900">{title.name}</h5>
            {isActive && (
              <span className="px-2 py-0.5 bg-yellow-400 text-yellow-900 text-xs font-semibold rounded">
                대표
              </span>
            )}
            {!title.earned && <span className="text-gray-400 text-xl">🔒</span>}
          </div>
          <p className="text-sm text-gray-600 mt-1">{title.description}</p>
          {title.earnedAt && (
            <p className="text-xs text-gray-400 mt-2">
              획득일: {new Date(title.earnedAt).toLocaleDateString("ko-KR")}
            </p>
          )}
        </div>
      </div>
    </div>
  );
};

export default MyPage;
