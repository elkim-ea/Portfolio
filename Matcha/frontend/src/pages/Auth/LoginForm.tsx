// src/components/LoginForm.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm, SubmitHandler, FieldError } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { LoginFormInputs } from './AuthType';
import { loginSchema } from './AuthSchema';
import authApi from '../../api/authApi';

const LoginForm: React.FC = () => {
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [errorMessage, setErrorMessage] = useState<string>('');

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<LoginFormInputs>({
        resolver: yupResolver(loginSchema),
    });

    const navigate = useNavigate();

    const onSubmit: SubmitHandler<LoginFormInputs> = async (data) => {
        setIsLoading(true);
        setErrorMessage('');

        try {
            // API 호출: 로그인
            const response = await authApi.login({
                email: data.email,
                password: data.password,
            });

            if (response.success && response.token) {
                // 토큰 저장
                localStorage.setItem('token', response.token);
                
                // 사용자 정보 저장
                if (response.user) {
                    localStorage.setItem('user', JSON.stringify(response.user));
                    
                    // 사용자 역할 저장
                    if (response.user.role) {
                        localStorage.setItem('userRole', response.user.role);
                    }
                }

                alert(`로그인 성공: ${response.user?.nickname || data.email}`);
                
                // 역할에 따라 페이지 이동
                if (response.user?.role === 'ADMIN') {
                    // 관리자는 관리자 페이지로
                    window.location.href = '/admin/users';
                } else {
                    // 일반 사용자는 홈으로
                    window.location.href = '/home';
                }
            } else {
                // success: false인 경우
                setErrorMessage(response.message || '로그인에 실패했습니다.');
            }
        } catch (error: any) {
            console.error('로그인 실패:', error);
            const errorMsg = error.message || '로그인에 실패했습니다. 다시 시도해 주세요.';
            setErrorMessage(errorMsg);
        } finally {
            setIsLoading(false);
        }
    };

    const handleSignupClick = (): void => {
        navigate('/signup');
    };

    const handleForgotPasswordClick = (): void => {
        navigate('/forgot-password');
    };

    const inputStyle = (hasError: FieldError | undefined): string => {
        const isError = !!hasError;

        return `
            mt-1 block w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none sm:text-sm 
            ${isError 
            ? 'border-danger focus:ring-danger focus:border-danger'
            : 'border-gray-300 focus:ring-main-blue focus:border-main-blue'}
        `;
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-white-blue p-4">
            <div className="w-full max-w-sm p-8 space-y-6 bg-sub-ivory rounded-lg shadow-lg">
                <h2 className="text-2xl font-bold text-center text-gray-900">로그인</h2>
                
                {/* 에러 메시지 표시 */}
                {errorMessage && (
                    <div className="p-3 rounded-md bg-red-50 border border-red-200">
                        <p className="text-sm text-danger text-center">{errorMessage}</p>
                    </div>
                )}

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    <div>
                        <label htmlFor="email" className="block text-sm font-medium text-gray-700">이메일</label>
                        <input
                            id="email"
                            type="email"
                            className={inputStyle(errors.email)}
                            {...register('email')}
                        />
                        {errors.email && (
                            <p className="mt-2 text-xs text-danger">{errors.email.message}</p>
                        )}
                    </div>

                    <div>
                        <label htmlFor="password" className="block text-sm font-medium text-gray-700">비밀번호</label>
                        <input
                            id="password"
                            type="password"
                            className={inputStyle(errors.password)}
                            {...register('password')}
                        />
                        {errors.password && (
                            <p className="mt-2 text-xs text-danger">{errors.password.message}</p>
                        )}
                    </div>

                    <button
                        type="submit"
                        disabled={isSubmitting || isLoading}
                        className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-main-blue hover:bg-sub-blue focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-main-blue disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {isLoading ? '로그인 중...' : '로그인'}
                    </button>
                </form>
                
                <div className="mt-4 flex flex-col items-center text-sm space-y-1">
                    {/* 비밀번호 변경 */}
                    <p className="font-medium text-gray-400">
                        비밀번호를 잊어버리셨나요?
                        <a onClick={handleForgotPasswordClick}
                            className="font-medium text-sub-blue hover:text-main-blue transition-colors cursor-pointer"> 비밀번호 변경</a>
                    </p>

                    {/* 회원가입 */}
                    <p className="font-medium text-gray-400">
                        새로운 계정을 생성하세요!
                        <a onClick={handleSignupClick}
                            className="font-medium text-sub-blue hover:text-main-blue transition-colors cursor-pointer"> 회원가입</a>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default LoginForm;