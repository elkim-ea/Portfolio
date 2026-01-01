// src/api/adminApi.ts
import { apiClient } from './authApi';
import {
  User,
  Quest,
  Title,
  Company,
  QuestCreateRequest,
  QuestUpdateRequest,
  TitleCreateRequest,
  TitleUpdateRequest,
  CompanyCreateRequest,
  CompanyUpdateRequest,
  PaginationParams,
  PaginatedResponse,
  UserSearchFilterParams,
  CompanySearchFilterParams,
  TitleSearchFilterParams,
  QuestSearchFilterParams
} from '../pages/Admin/AdminTypes'

const buildQueryString = (params: any): string => {
  const queryParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      queryParams.append(key, String(value));
    }
  });
  return queryParams.toString();
};

// apiClient import
const adminApiClient = apiClient;

export const adminApi = {
  // 사용자 관리
  users: {
    getAll: async (
      pagination: PaginationParams,
      filters?: UserSearchFilterParams
    ): Promise<PaginatedResponse<User>> => {
      const query = buildQueryString({ ...pagination, ...filters });
      const response = await adminApiClient.get(`/api/admin/users?${query}`);
      return response.data.data;
    },

    getById: async (id: number): Promise<User> => {
      const response = await adminApiClient.get(`/api/admin/users/${id}`);
      return response.data.data;
    },

    update: async (id: number, data: Partial<User>): Promise<User> => {
      const response = await adminApiClient.put(`/api/admin/users/${id}`, data);
      return response.data.data;
    },

    delete: async (id: number): Promise<void> => {
      await adminApiClient.delete(`/api/admin/users/${id}`);
    },

    changeRole: async (id: number, role: string): Promise<User> => {
      const response = await adminApiClient.patch(
        `/api/admin/users/${id}/role?role=${role}`
      );

      //  자신의 권한을 변경한 경우
      if (response.data.data?.requiresRelogin) {
        alert('권한이 변경되었습니다. 다시 로그인해주세요.');
        
        // 로그아웃 처리
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('userRole');
        
        // 로그인 페이지로 이동
        window.location.href = '/login';
      } else{
        alert('권한이 변경되었습니다.');
      }
      
      return response.data.data.user || response.data.data;
    },
  },

  // 퀘스트 관리
  quests: {
    getAll: async (
      pagination: PaginationParams,
      filters?: QuestSearchFilterParams
    ): Promise<PaginatedResponse<Quest>> => {
      const query = buildQueryString({ ...pagination, ...filters });
      const response = await adminApiClient.get(`/api/admin/quests?${query}`);
      return response.data.data;
    },

    getById: async (id: number): Promise<Quest> => {
      const response = await adminApiClient.get(`/api/admin/quests/${id}`);
      return response.data.data;
    },

    create: async (data: QuestCreateRequest): Promise<Quest> => {
      const response = await adminApiClient.post('/api/admin/quests', data);
      return response.data.data;
    },

    update: async (id: number, data: QuestUpdateRequest): Promise<Quest> => {
      const response = await adminApiClient.put(`/api/admin/quests/${id}`, data);
      return response.data.data;
    },

    delete: async (id: number): Promise<void> => {
      await adminApiClient.delete(`/api/admin/quests/${id}`);
    },

    toggleActive: async (id: number): Promise<Quest> => {
      const response = await adminApiClient.patch(`/api/admin/quests/${id}/toggle-active`);
      return response.data.data;
    },
  },

  // 칭호 관리
  titles: {
    getAll: async (
      pagination: PaginationParams,
      filters?: TitleSearchFilterParams
    ): Promise<PaginatedResponse<Title>> => {
      const query = buildQueryString({ ...pagination, ...filters });
      const response = await adminApiClient.get(`/api/admin/titles?${query}`);
      return response.data.data;
    },

    getById: async (id: number): Promise<Title> => {
      const response = await adminApiClient.get(`/api/admin/titles/${id}`);
      return response.data.data;
    },

    create: async (data: TitleCreateRequest): Promise<Title> => {
      const response = await adminApiClient.post('/api/admin/titles', data);
      return response.data.data;
    },

    update: async (id: number, data: TitleUpdateRequest): Promise<Title> => {
      const response = await adminApiClient.put(`/api/admin/titles/${id}`, data);
      return response.data.data;
    },

    delete: async (id: number): Promise<void> => {
      await adminApiClient.delete(`/api/admin/titles/${id}`);
    },

    toggleActive: async (id: number): Promise<Title> => {
      const response = await adminApiClient.patch(`/api/admin/titles/${id}/toggle-active`);
      return response.data.data;
    },
  },

  // 기업 관리
  companies: {
    getAll: async (
      pagination: PaginationParams,
      filters?: CompanySearchFilterParams
    ): Promise<PaginatedResponse<Company>> => {
      const query = buildQueryString({ ...pagination, ...filters });
      const response = await adminApiClient.get(`/api/admin/companies?${query}`);
      return response.data.data;
    },

    getById: async (id: number): Promise<Company> => {
      const response = await adminApiClient.get(`/api/admin/companies/${id}`);
      return response.data.data;
    },

    create: async (data: CompanyCreateRequest, logoFile?: File): Promise<Company> => {
      const formData = new FormData();
      formData.append('companyName', data.companyName);
      
      // 카테고리 배열
      data.categories.forEach(category => {
        formData.append('categories', category);
      });
      // formData.append('categories', data.categories);
      
      if (data.companyWebsiteUrl) {
        formData.append('companyWebsiteUrl', data.companyWebsiteUrl);
      }
      
      if (logoFile) {
        formData.append('logoFile', logoFile);
      }
      if (data.companyLogo) {
        formData.append('companyLogo', data.companyLogo);
      }else{
        formData.append('companyLogo', "https://example.com/logo.png");
      }

      const response = await adminApiClient.post('/api/admin/companies', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      if (!response.data.success) {
        throw new Error(response.data.message || '기업 생성에 실패했습니다.');
      }

      return response.data.data;
    },

    update: async (id: number, data: CompanyUpdateRequest, logoFile?: File): Promise<Company> => {
      const formData = new FormData();
      formData.append('companyName', data.companyName);
      
      // 카테고리 배열
      data.categories.forEach(category => {
        formData.append('categories', category);
      });
      
      if (data.companyWebsiteUrl) {
        formData.append('companyWebsiteUrl', data.companyWebsiteUrl);
      }
      
      if (logoFile && logoFile.size > 0) {
        formData.append('logoFile', logoFile);
      }
      if (data.companyLogo) {
        formData.append('companyLogo', data.companyLogo);
      } else {
        formData.append('companyLogo', "");
      }
      const response = await adminApiClient.put(`/api/admin/companies/${id}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      if (!response.data.success) {
        throw new Error(response.data.message || '기업 수정에 실패했습니다.');
      }

      return response.data.data;
    },

    delete: async (id: number): Promise<void> => {
      await adminApiClient.delete(`/api/admin/companies/${id}`);
    },
  },

  // 파일 업로드
  // upload: {
  //   image: async (
  //     file: File, 
  //     type: 'logo' | 'profile' | 'quest' | 'title' | 'general' = 'general'
  //   ): Promise<{ url: string; filename: string; type: string }> => {
  //     const formData = new FormData();
  //     formData.append('file', file);
  //     formData.append('type', type);

  //     const response = await adminApiClient.post('/upload/image', formData, {
  //       headers: {
  //         'Content-Type': 'multipart/form-data',
  //       },
  //     });

  //     if (!response.data.success) {
  //       throw new Error(response.data.message || '파일 업로드에 실패했습니다.');
  //     }

  //     return {
  //       url: response.data.url,
  //       filename: response.data.filename,
  //       type: response.data.type,
  //     };
  //   },

  //   deleteImage: async (url: string): Promise<void> => {
  //     const response = await adminApiClient.delete('/upload/image', {
  //       params: { url },
  //     });

  //     if (!response.data.success) {
  //       throw new Error(response.data.message || '파일 삭제에 실패했습니다.');
  //     }
  //   },
  // },
};