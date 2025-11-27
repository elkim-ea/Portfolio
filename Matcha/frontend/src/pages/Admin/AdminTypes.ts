// src/pages/Admin/AdminTypes.ts

export interface User {
  userId: number;
  email: string;
  nickname: string;
  role: 'USER' | 'ADMIN';
  esgScore: number;
  eScore: number;
  sScore: number;
  character: string;
  createdAt: string;
  updatedAt: string;
}

export interface Quest {
  questId: number;
  adminId: number;
  title: string;
  description: string;
  rewardScore: number;
  category: 'E' | 'S';
  type: 'DAILY' | 'WEEKLY' | 'SEASON';
  authType: 'IMAGE' | 'TEXT';
  isActive: boolean;
  maxAttempts: number;
  conditionJson: string;
  createdAt: string;
}

export interface QuestCreateRequest {
  title: string;
  description: string;
  category: 'E' | 'S';
  type: 'DAILY' | 'WEEKLY' | 'SEASON';
  authType: 'IMAGE' | 'TEXT';
  rewardScore: number;
  maxAttempts: number;
  conditionJson: string;
  isActive: boolean;
  adminId: number;
}

export interface QuestUpdateRequest {
  title: string;
  description: string;
  category: 'E' | 'S';
  type: 'DAILY' | 'WEEKLY' | 'SEASON';
  authType: 'IMAGE' | 'TEXT';
  rewardScore: number;
  maxAttempts: number;
  conditionJson: string;
  isActive: boolean;
  adminId: number;
}

export interface Title {
  titleId: number;
  name: string;
  description: string;
  conditionJson: string;
}

export interface TitleCreateRequest {
  name: string;
  description: string;
  conditionJson: string;
}

export interface TitleUpdateRequest {
  name: string;
  description: string;
  conditionJson: string;
}

export interface Company {
  id: number;
  companyName: string;
  categories: string[];
  companyLogo?: string;
  companyWebsiteUrl?: string;
  createdAt: string;
  updatedAt?: string;
}

// 기업 생성 요청
export interface CompanyCreateRequest {
  companyName: string;
  categories: string[];  // 'LEADER' | 'SPONSOR'
  companyLogo?: string; // URL
  logoFile?: File;    // MultipartFile
  companyWebsiteUrl?: string;
}

// 기업 수정 요청
export interface CompanyUpdateRequest {
  companyName: string;
  categories: string[];  // 'LEADER' | 'SPONSOR'
  companyLogo?: string;  // URL
  logoFile?: File;      // MultipartFile
  companyWebsiteUrl?: string;
}

export interface PaginationParams {
  page: number;
  size: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
}

export interface UserSearchFilterParams {
  keyword?: string;
  role?: string;
  startDate?: string;
  endDate?: string;
}

export interface CompanySearchFilterParams {
  keyword?: string;
  category?: string;
  startDate?: string;
  endDate?: string;
}

export interface TitleSearchFilterParams {
  keyword?: string;
  startDate?: string;
  endDate?: string;
}

export interface QuestSearchFilterParams {
  keyword?: string;
  type?: string;
  authType?: string;
  // status?: boolean;
  startDate?: string;
  endDate?: string;
}