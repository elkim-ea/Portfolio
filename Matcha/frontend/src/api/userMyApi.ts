// src/api/userApi.ts
import { apiClient } from './authApi';

// ===========================
// 타입 정의
// ===========================

export interface UserQuestInfo {
  userQuestId: number;
  questId: number;
  questTitle: string;
  questDescription: string;
  rewardScore: number;
  status: 'PENDING' | 'SUCCESS' | 'FAILED';
  attemptCount: number;
  maxAttempts: number;
  progressPercentage: number;
  startedAt: string | null;
  completedAt: string | null;
}

export interface UserProfile {
  userId: number;
  email: string;
  nickname: string;
  character: string;
  esgScore: number;
  eScore: number;
  sScore: number;
  role: string;
  mainTitleId: number | null;
  mainTitleName: string | null;
  titles: TitleInfo[];
  userQuests: UserQuestInfo[];
  questProgress: QuestProgress;
}

export interface TitleInfo {
  titleId: number;
  name: string;
  description: string;
  earned: boolean;
  earnedAt: string | null;
}

export interface QuestProgress {
  totalQuests: number;
  completedQuests: number;
  progressPercentage: number;
}

export interface ProfileUpdateRequest {
  currentPassword: string;
  newNickname: string;
}

export interface PasswordUpdateRequest {
  currentPassword: string;
  newPassword: string;
}

interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
}

// ===========================
// API 함수들
// ===========================

export const userMyApi = {
  /**
   * 프로필 조회
   */
  getProfile: async (): Promise<UserProfile> => {
    try {
      const response = await apiClient.get<ApiResponse<UserProfile>>('/my/profile');
      
      if (!response.data.success) {
        throw new Error(response.data.message || '프로필 조회에 실패했습니다.');
      }
      
      return response.data.data!;
    } catch (error: any) {
      console.error('프로필 조회 실패:', error);
      throw new Error(error.message || '프로필 조회에 실패했습니다.');
    }
  },

  /**
   * 닉네임 변경
   */
  updateNickname: async (currentPassword: string, newNickname: string): Promise<void> => {
    try {
      const response = await apiClient.put<ApiResponse>('/my/profile/nickname', {
        currentPassword,
        newNickname,
      });
      
      if (!response.data.success) {
        throw new Error(response.data.message || '닉네임 변경에 실패했습니다.');
      }
    } catch (error: any) {
      console.error('닉네임 변경 실패:', error);
      throw new Error(error.message || '닉네임 변경에 실패했습니다.');
    }
  },

  /**
   * 비밀번호 변경
   */
  updatePassword: async (currentPassword: string, newPassword: string): Promise<void> => {
    try {
      const response = await apiClient.put<ApiResponse>('/my/profile/password', {
        currentPassword,
        newPassword,
      });
      
      if (!response.data.success) {
        throw new Error(response.data.message || '비밀번호 변경에 실패했습니다.');
      }
    } catch (error: any) {
      console.error('비밀번호 변경 실패:', error);
      throw new Error(error.message || '비밀번호 변경에 실패했습니다.');
    }
  },

  /**
   * 회원 탈퇴
   */
  deleteAccount: async (currentPassword: string): Promise<void> => {
    try {
      const response = await apiClient.delete<ApiResponse>('/my/profile', {
        data: { currentPassword },
      });
      
      if (!response.data.success) {
        throw new Error(response.data.message || '회원 탈퇴에 실패했습니다.');
      }
    } catch (error: any) {
      console.error('회원 탈퇴 실패:', error);
      throw new Error(error.message || '회원 탈퇴에 실패했습니다.');
    }
  },

  /**
   * 대표 칭호 설정
   */
  setMainTitle: async (titleId: number | null): Promise<void> => {
    try {
      const url = titleId !== null 
        ? `/my/profile/title/${titleId}` 
        : `/my/profile/title/null`;
      
      const response = await apiClient.put<ApiResponse>(url);
      
      if (!response.data.success) {
        throw new Error(response.data.message || '대표 칭호 설정에 실패했습니다.');
      }
    } catch (error: any) {
      console.error('대표 칭호 설정 실패:', error);
      throw new Error(error.message || '대표 칭호 설정에 실패했습니다.');
    }
  },
};

export default userMyApi;