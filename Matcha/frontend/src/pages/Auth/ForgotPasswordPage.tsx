import React, { useState, useCallback, useEffect, useMemo } from 'react';
import { useForm, SubmitHandler, FieldError } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { useNavigate } from 'react-router-dom';
import { ForgotPasswordInputs } from './AuthType';
import { forgotPasswordSchema, emailOnlySchema } from './AuthSchema';
import authApi from '../../api/authApi';

const ForgotPasswordPage: React.FC = () => {
    const navigate = useNavigate();
    
    // 인증 및 상태 관리
    const [isCodeSent, setIsCodeSent] = useState<boolean>(false);
    const [isVerified, setIsVerified] = useState<boolean>(false);
    const [timer, setTimer] = useState<number>(180);
    const [verificationMessage, setVerificationMessage] = useState<string>('');
    const [isLoading, setIsLoading] = useState<boolean>(false);

    // 인증 코드가 전송되었거나 인증이 완료되면 전체 스키마를 적용
    // 그렇지 않으면 이메일만 검사하는 스키마를 적용
    const currentResolver = useMemo(() => {
        return yupResolver(isCodeSent || isVerified ? forgotPasswordSchema : emailOnlySchema);
    }, [isCodeSent, isVerified]);

    const {
        register,
        handleSubmit,
        getValues,
        setError,
        clearErrors,
        trigger,
        formState: { errors, isSubmitting, isValid },
    } = useForm<ForgotPasswordInputs>({
        resolver: currentResolver as any,
        mode: 'onTouched',
        defaultValues: {
            email: '',
            authCode: '',
            newPassword: '',
            confirmPassword: '',
        }
    });

    // 타이머 로직
    useEffect(() => {
        if (isCodeSent && !isVerified && timer > 0) {
            const interval: NodeJS.Timeout = setInterval(() => {
                setTimer(prev => prev - 1);
            }, 1000);
            return () => clearInterval(interval);
        } else if (timer === 0) {
            setVerificationMessage('인증 시간이 만료되었습니다. 다시 시도해 주세요.');
            setIsCodeSent(false);
        }
    }, [isCodeSent, isVerified, timer]);

    const formatTime = (seconds: number): string => {
        const minutes: number = Math.floor(seconds / 60);
        const remainingSeconds: number = seconds % 60;
        return `${minutes}:${remainingSeconds < 10 ? '0' : ''}${remainingSeconds}`;
    };

    // 공통 입력 필드 스타일
    const inputStyle = (hasError: FieldError | undefined, isReadOnly: boolean = false): string => {
        const isError: boolean = !!hasError;
        const focusStyle: string = isError 
            ? 'focus:ring-danger focus:border-danger' 
            : 'focus:ring-main-blue focus:border-main-blue';
        
        const readOnlyStyle: string = isReadOnly 
            ? 'bg-gray-100 text-gray-500 cursor-not-allowed'
            : 'bg-white';

        return `
            mt-1 block w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none sm:text-sm 
            ${isError ? 'border-danger' : 'border-gray-300'}
            ${focusStyle}
            ${readOnlyStyle}
        `;
    };
    
    // 인증번호 전송 로직
    const handleSendCode = useCallback(async (e: React.MouseEvent<HTMLButtonElement>): Promise<void> => {
        e.preventDefault();

        const isEmailValid: boolean = await trigger('email');
        
        if (!isEmailValid) {
            return;
        }

        clearErrors('email');
        setVerificationMessage('인증번호를 전송 중입니다...');
        setIsLoading(true);
        
        // 먼저 상태 초기화
        setIsCodeSent(false);
        setIsVerified(false);

        try {
            const email = getValues('email');
            const response = await authApi.sendPasswordResetCode(email);
            
            // success 필드를 확인하여 실제 성공 여부 판단
            if (response.success) {
                // 성공 시에만 isCodeSent를 true로 설정
                setIsCodeSent(true);
                setTimer(180);
                setVerificationMessage(response.message || `입력하신 이메일로 인증번호가 전송되었습니다.`);
            } else {
                // success: false인 경우 (가입되지 않은 이메일 등)
                setVerificationMessage(`⚠️ ${response.message}`);
                setError('email', { 
                    type: 'manual', 
                    message: response.message
                });
            }
        } catch (error: any) {
            console.error('인증번호 전송 실패:', error);
            
            // Error 객체에서 메시지 추출
            const errorMessage = error.message || '인증번호 전송에 실패했습니다. 다시 시도해 주세요.';
            setVerificationMessage(`⚠️ ${errorMessage}`);
            setError('email', { 
                type: 'manual', 
                message: errorMessage
            });
        } finally {
            setIsLoading(false);
        }
    }, [trigger, clearErrors, getValues, setError]);

    // 인증번호 확인 로직
    const handleVerifyCode = useCallback(async (e: React.MouseEvent<HTMLButtonElement>): Promise<void> => {
        e.preventDefault();

        const isAuthCodeValid: boolean = await trigger('authCode');
        
        if (!isAuthCodeValid) {
            return;
        }

        setIsLoading(true);
        clearErrors('authCode');

        try {
            const email = getValues('email');
            const authCode = getValues('authCode');
            
            // API 호출: 인증번호 확인
            const response = await authApi.verifyEmailCode(email, authCode);

            // success 필드를 확인하여 실제 성공 여부 판단
            if (response.success) {
                // 성공 시에만 isVerified를 true로 설정
                setIsVerified(true);
                setVerificationMessage(response.message || '✅ 인증이 완료되었습니다. 새 비밀번호를 설정할 수 있습니다.');
            } else {
                setVerificationMessage('⚠️ ' + (response.message || '인증번호가 일치하지 않습니다.'));
            }
            
        } catch (error: any) {
            console.error('인증번호 확인 실패:', error);
            
            const errorMessage = error.message || '인증번호가 일치하지 않습니다.';
            setVerificationMessage(`⚠️ ${errorMessage}`);
            setError('authCode', { 
                type: 'manual', 
                message: errorMessage
            });
        } finally {
            setIsLoading(false);
        }
    }, [getValues, trigger, setError, clearErrors]);

    // 비밀번호 재설정 제출
    const onSubmit: SubmitHandler<ForgotPasswordInputs> = async (data): Promise<void> => {
        if (!isVerified) {
            console.error('이메일 인증을 먼저 완료해주세요.'); 
            setVerificationMessage('🚨 비밀번호를 변경하려면 이메일 인증을 완료해야 합니다.');
            return;
        }
        
        setIsLoading(true);

        try {
            // API 호출: 비밀번호 재설정
            const response = await authApi.resetPassword({
                email: data.email,
                authCode: data.authCode,
                newPassword: data.newPassword,
            });
            
            alert(response.message || '비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해 주세요.');
            navigate('/login');
        } catch (error: any) {
            console.error('비밀번호 재설정 실패:', error);
            
            const errorMessage = error.message || '비밀번호 재설정에 실패했습니다.';
            setVerificationMessage(`⚠️ ${errorMessage}`);
            alert(errorMessage);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-white-blue p-4">
            <div className="w-full max-w-sm p-8 space-y-6 bg-sub-ivory rounded-lg shadow-lg">
                <h2 className="text-2xl font-bold text-center text-gray-900">비밀번호 변경</h2>
                
                {/* 설명 텍스트 */}
                <p className="text-sm text-center text-gray-600">
                    가입 시 사용한 이메일로 인증번호를 전송합니다.
                </p>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                
                    {/* 이메일 필드 */}
                    <div>
                        <label htmlFor="email" className="block text-sm font-medium text-gray-700">이메일</label>
                        <div className="flex space-x-2">
                            <input
                                id="email"
                                type="email"
                                readOnly={isVerified}
                                className={inputStyle(errors.email, isVerified)}
                                {...register('email')}
                            />
                            <button
                                type="button"
                                onClick={handleSendCode}
                                disabled={isVerified || isLoading}
                                className={`py-2 px-3 text-sm font-medium rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors whitespace-nowrap
                                ${isVerified || isLoading
                                    ? 'bg-gray-400 text-gray-700 cursor-not-allowed'
                                    : 'bg-main-blue hover:bg-sub-blue text-white focus:ring-main-blue'
                                }`}
                            >
                                {isLoading ? '전송 중...' : isCodeSent ? (isVerified ? '인증 완료' : '재전송') : '인증번호 전송'}
                            </button>
                        </div>
                        
                        {/* 에러 메시지 표시 */}
                        {errors.email && (
                            <p className="mt-2 text-xs text-danger">
                                {errors.email?.message}
                            </p>
                        )}
                    </div>
                    
                    {/* 인증번호 필드 */}
                    {isCodeSent && !isVerified && (
                        <div>
                            <label htmlFor="authCode" className="block text-sm font-medium text-gray-700">인증번호</label>
                            
                            <div className="flex space-x-2 items-center relative"> 
                                <input
                                    id="authCode"
                                    type="text"
                                    className={`${inputStyle(errors.authCode)} pr-20`}
                                    {...register('authCode')}
                                />
                                
                                {/* 타이머 표시 */}
                                {timer > 0 && (
                                    <span className={`absolute right-[7rem] text-xs font-medium ${timer <= 30 ? 'text-danger' : 'text-gray-500'}`}>
                                        {formatTime(timer)}
                                    </span>
                                )}
                                
                                <button
                                    type="button"
                                    onClick={handleVerifyCode}
                                    disabled={isLoading}
                                    className={`py-2 px-3 text-sm font-medium rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors whitespace-nowrap
                                    ${isLoading 
                                        ? 'bg-gray-400 text-gray-700 cursor-not-allowed'
                                        : 'bg-main-blue hover:bg-sub-blue text-white focus:ring-main-blue'
                                    }`}
                                >
                                    {isLoading ? '확인 중...' : '인증 확인'}
                                </button>
                            </div>
                            
                            <div className="mt-2 text-left"> 
                                <p className={`text-xs ${verificationMessage.startsWith('✅') ? 'text-main-blue' : 'text-danger'} text-left`}>
                                    {verificationMessage}
                                </p>
                            </div>
                            
                            {errors.authCode && (
                                <p className="mt-2 text-xs text-danger">{errors.authCode.message}</p>
                            )}
                        </div>
                    )}

                    {/* 새 비밀번호 설정 필드 (인증 완료 시 표시) */}
                    {isVerified && (
                        <div className="space-y-4 pt-4 border-t border-gray-200">
                            <p className="text-sm font-semibold text-gray-700">새 비밀번호를 설정해 주세요.</p>
                            
                            {/* 새 비밀번호 필드 */}
                            <div>
                                <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700">새 비밀번호</label>
                                <input
                                    id="newPassword"
                                    type="password"
                                    className={inputStyle(errors.newPassword)}
                                    {...register('newPassword')}
                                />
                                {errors.newPassword && (
                                    <p className="mt-2 text-xs text-danger">{errors.newPassword.message}</p>
                                )}
                            </div>

                            {/* 비밀번호 확인 필드 */}
                            <div>
                                <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">비밀번호 확인</label>
                                <input
                                    id="confirmPassword"
                                    type="password"
                                    className={inputStyle(errors.confirmPassword)}
                                    {...register('confirmPassword')}
                                />
                                {errors.confirmPassword && (
                                    <p className="mt-2 text-xs text-danger">{errors.confirmPassword.message}</p>
                                )}
                            </div>
                        </div>
                    )}

                    <button
                        type="submit" 
                        disabled={isSubmitting || isLoading || !isVerified || (isVerified && !isValid)}
                        className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-main-blue hover:bg-sub-blue focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-main-blue disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {isLoading ? '처리 중...' : isVerified ? '새 비밀번호로 변경하기' : '인증을 완료해 주세요'}
                    </button>
                </form>
                
                {/* 로그인 페이지로 돌아가기 */}
                <div className="mt-4 text-center text-sm">
                    <p className="font-medium text-gray-400">
                        로그인 페이지로
                        <a onClick={() => navigate('/login')}
                            className="font-medium text-sub-blue hover:text-main-blue transition-colors cursor-pointer"> 돌아가기</a>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default ForgotPasswordPage;