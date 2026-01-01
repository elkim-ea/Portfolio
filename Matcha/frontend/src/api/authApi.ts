// src/api/authApi.ts
import axios, { AxiosInstance, AxiosError } from 'axios';

// API 기본 설정
const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL;
// const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL || "/api"; //gcp


// ============ Token 관리 함수 (로컬스토리지 사용) ============
export const setAccessToken = (token: string) => {
    localStorage.setItem('token', token);
};

export const getAccessToken = (): string | null => {
    const token = localStorage.getItem('token');
    return token;
};

export const clearAccessToken = () => {
    localStorage.removeItem('token');
};


// Axios 인스턴스 생성
const apiClient: AxiosInstance = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

// 요청 인터셉터
apiClient.interceptors.request.use(
    (config) => {
        // 🔥 로그인 / 회원가입 / refresh 는 토큰 붙이지 않음
        if (
        config.url?.includes('/api/auth/login') ||
        config.url?.includes('/api/auth/refresh') ||
        config.url?.includes('/api/auth/signup')
        ) {
        return config;
        }

        const token = getAccessToken();
        
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 응답 인터셉터
let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

apiClient.interceptors.response.use(
    (response) => {
        if (response.config.url?.includes('/api/auth/login')) {
            return response;
    }
        if (response.data && response.data.success === false) {
            return Promise.reject(new Error(response.data.message || '요청 처리에 실패했습니다.'));
        }
        return response;
    },
    async (error: AxiosError) => {
        const originalRequest: any = error.config;

        // 403 에러 (권한 없음) 처리
        if (error.response?.status === 403) {
            const currentPath = window.location.pathname;
            
            // 관리자 페이지에서 403 에러가 발생한 경우
            if (currentPath.startsWith('/admin')) {
                alert('관리자 권한이 없습니다. 메인 페이지로 이동합니다.');
                
                // 로컬 스토리지 업데이트
                const user = localStorage.getItem('user');
                if (user) {
                    const userData = JSON.parse(user);
                    userData.role = 'USER';
                    localStorage.setItem('user', JSON.stringify(userData));
                    localStorage.setItem('userRole', 'USER');
                }
                
                window.location.href = '/home';
                return Promise.reject(error);
            }
        }

        // 401 에러 (인증 실패) 처리
        if (error.response?.status === 401 && !originalRequest._retry) {
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                })
                    .then(token => {
                        originalRequest.headers.Authorization = `Bearer ${token}`;
                        return apiClient(originalRequest);
                    })
                    .catch(err => Promise.reject(err));
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                const response = await axios.post(
                    `${API_BASE_URL}/api/auth/refresh`,
                    {},
                    { withCredentials: true }
                );

                if (response.data.success && response.data.token) {
                    const newToken = response.data.token;
                    setAccessToken(newToken);
                    
                    processQueue(null, newToken);
                    originalRequest.headers.Authorization = `Bearer ${newToken}`;
                    return apiClient(originalRequest);
                } else {
                    throw new Error('토큰 갱신 실패');
                }
            } catch (refreshError) {
                console.error('❌ Token refresh failed:', refreshError);
                processQueue(refreshError, null);
                clearAccessToken();
                window.location.href = '/login';
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);

// ==================== 타입 정의 ====================

// 약관 타입
export interface Terms {
    id: number;
    title: string;
    version: string;
    content: string;
    isRequired: boolean;
    createdAt: string;
}

interface TermsResponse {
    success: boolean;
    message: string;
    data?: Terms[];
}

interface SendEmailCodeRequest {
    email: string;
}

interface SendEmailCodeResponse {
    success: boolean;
    message: string;
}

interface VerifyEmailCodeRequest {
    email: string;
    code: string;
}

interface VerifyEmailCodeResponse {
    success: boolean;
    message: string;
}

interface SignupRequest {
    email: string;
    password: string;
    nickname: string;
    character?: string;
    authCode: string;
    agreedTermsIds: number[];
}

interface SignupResponse {
    success: boolean;
    message: string;
    userId?: number;
}

interface LoginRequest {
    email: string;
    password: string;
}

interface User {
    userId?: number;
    email: string;
    nickname: string;
    role: 'USER' | 'ADMIN';
    esgScore?: number;
    character?: string;
}

interface LoginResponse {
    success: boolean;
    message: string;
    token?: string;
    user?: User;
}

interface ResetPasswordRequest {
    email: string;
    authCode: string;
    newPassword: string;
}

interface ResetPasswordResponse {
    success: boolean;
    message: string;
}

// ==================== API 함수들 ====================

export const authApi = {
    // 이메일 인증번호 전송 (회원가입용)
    sendSignupEmailCode: async (email: string): Promise<SendEmailCodeResponse> => {
        try {
            const response = await apiClient.post<SendEmailCodeResponse>('/api/auth/signup/send-code', {
                email,
            });
            return response.data;
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                throw new Error(error.response.data.message || '이메일 전송에 실패했습니다.');
            }
            throw new Error('네트워크 오류가 발생했습니다.');
        }
    },

    // 이메일 인증번호 전송 (비밀번호 찾기용)
    sendPasswordResetCode: async (email: string): Promise<SendEmailCodeResponse> => {
        try {
            const response = await apiClient.post<SendEmailCodeResponse>('/api/auth/password-reset/send-code', {
                email,
            });
            return response.data;
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                throw new Error(error.response.data.message || '이메일 전송에 실패했습니다.');
            }
            throw new Error('네트워크 오류가 발생했습니다.');
        }
    },

    // 이메일 인증번호 확인
    verifyEmailCode: async (email: string, code: string): Promise<VerifyEmailCodeResponse> => {
        try {
            const response = await apiClient.post<VerifyEmailCodeResponse>('/api/auth/verify-code', {
                email,
                code,
            });
            return response.data;
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                throw new Error(error.response.data.message || '인증번호 확인에 실패했습니다.');
            }
            throw new Error('네트워크 오류가 발생했습니다.');
        }
    },

    // 회원가입
    signup: async (data: SignupRequest) => {
        try {
            const response = await apiClient.post('/api/auth/signup', data);
            return response.data;
        } catch (error: any) {
            if (axios.isAxiosError(error) && error.response) {
                throw new Error(error.response.data.message || '회원가입에 실패했습니다.');
            }
            throw new Error('네트워크 오류가 발생했습니다.');
        }
    },

    // 로그인
    login: async (data: LoginRequest): Promise<LoginResponse> => {
        try {
            clearAccessToken();
            // const response = await apiClient.post<LoginResponse>('/auth/login', data);
            const response = await apiClient.post<LoginResponse>('/api/auth/login', data);
            return response.data;
        } catch (error: any) {
            if (axios.isAxiosError(error) && error.response) {
                throw new Error(error.response.data.message || '로그인에 실패했습니다.');
            }
            throw new Error('네트워크 오류가 발생했습니다.');
        }
    },

    // 로그아웃
    logout: async () => {
        try {
            const response = await apiClient.post('/api/auth/logout');
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            localStorage.removeItem('userRole');
            return response.data;
        } catch (error: any) {
            if (axios.isAxiosError(error) && error.response) {
                throw new Error(error.response.data.message || '로그아웃에 실패했습니다.');
            }
            throw new Error('네트워크 오류가 발생했습니다.');
        }
    },

    // 비밀번호 재설정
    resetPassword: async (data: ResetPasswordRequest): Promise<ResetPasswordResponse> => {
        try {
            const response = await apiClient.post<ResetPasswordResponse>('/api/auth/password-reset', data);
            return response.data;
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                throw new Error(error.response.data.message || '비밀번호 재설정에 실패했습니다.');
            }
            throw new Error('네트워크 오류가 발생했습니다.');
        }
    },

    // 모든 약관 조회
    getAllTerms: async (): Promise<Terms[]> => {
        try {
            const response = await apiClient.get<TermsResponse>('/api/auth/terms');
            return response.data.data || [];
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                throw new Error(error.response.data.message || '약관 조회에 실패했습니다.');
            }
            throw new Error('네트워크 오류가 발생했습니다.');
        }
    },

    // 필수 약관만 조회
    getRequiredTerms: async (): Promise<Terms[]> => {
        try {
            const response = await apiClient.get<TermsResponse>('/api/auth/terms/required');
            return response.data.data || [];
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                throw new Error(error.response.data.message || '필수 약관 조회에 실패했습니다.');
            }
            throw new Error('네트워크 오류가 발생했습니다.');
        }
    },

    // 특정 약관 상세 조회
    getTermsById: async (id: number): Promise<Terms> => {
        try {
            const response = await apiClient.get<{ success: boolean; message: string; data: Terms }>(`/api/auth/terms/${id}`);
            if (!response.data.data) {
                throw new Error('약관을 찾을 수 없습니다.');
            }
            return response.data.data;
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                throw new Error(error.response.data.message || '약관 조회에 실패했습니다.');
            }
            throw new Error('네트워크 오류가 발생했습니다.');
        }
    },
};

export { apiClient };
export default authApi;