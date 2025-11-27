// src/components/SignupForm.tsx
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useForm, SubmitHandler, FieldError } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { SignupFormInputs } from './AuthType';
import { signupSchema, emailOnlySchema } from './AuthSchema';
import { authApi, Terms } from '../../api/authApi';

const SignupForm: React.FC = () => {
    const [isCodeSent, setIsCodeSent] = useState<boolean>(false);
    const [isEmailVerified, setIsEmailVerified] = useState<boolean>(false);
    const [timer, setTimer] = useState<number>(180);
    const [verificationMessage, setVerificationMessage] = useState<string>('');
    const [isSendingCode, setIsSendingCode] = useState<boolean>(false);
    const [isVerifyingCode, setIsVerifyingCode] = useState<boolean>(false);

    const [terms, setTerms] = useState<Terms[]>([]);
    const [selectedTermsIds, setSelectedTermsIds] = useState<number[]>([]);
    const [loadingTerms, setLoadingTerms] = useState(true);

    const navigate = useNavigate();
    const location = useLocation();

    const currentResolver = useMemo(() => {
        return yupResolver(isCodeSent || isEmailVerified ? signupSchema : emailOnlySchema);
    }, [isCodeSent, isEmailVerified]);

    const previousFormData = (location.state as any)?.formData as SignupFormInputs | undefined;

    const {
        register,
        handleSubmit,
        getValues,
        setError,
        clearErrors,
        trigger,
        formState: { errors, isSubmitting, isValid },
        reset,
    } = useForm<SignupFormInputs>({
        resolver: currentResolver as any,
        mode: 'onTouched',
        defaultValues: previousFormData || {
            nickname: '',
            email: '',
            authCode: '',
            password: '',
            confirmPassword: '',
        }
    });

    useEffect(() => {
        if (location.state && previousFormData) {
            reset(previousFormData);

            if ((location.state as any)?.isCodeSent) {
                setIsCodeSent(true);
            }
            if ((location.state as any)?.isEmailVerified) {
                setIsEmailVerified(true);
            }

            // 약관 선택 상태 복원
            if ((location.state as any)?.selectedTermsIds) {
                setSelectedTermsIds((location.state as any).selectedTermsIds);
            }
            
        }

    }, [previousFormData, reset, location.state]);

    useEffect(() => {
        if (isCodeSent && !isEmailVerified && timer > 0) {
            const interval: NodeJS.Timeout = setInterval(() => {
                setTimer(prev => prev - 1);
            }, 1000);
            return () => clearInterval(interval);
        } else if (timer === 0) {
            setVerificationMessage('인증 시간이 만료되었습니다. 다시 시도해 주세요.');
            setIsCodeSent(false);
        }
    }, [isCodeSent, isEmailVerified, timer]);

    const formatTime = (seconds: number): string => {
        const minutes: number = Math.floor(seconds / 60);
        const remainingSeconds: number = seconds % 60;
        return `${minutes}:${remainingSeconds < 10 ? '0' : ''}${remainingSeconds}`;
    };

    // 인증번호 전송
    const handleSendCode = useCallback(async (e: React.MouseEvent<HTMLButtonElement>): Promise<void> => {
        e.preventDefault();

        const isEmailValid: boolean = await trigger('email');
        if (!isEmailValid) {
            return;
        }

        const email = getValues('email');
        clearErrors('email');
        setVerificationMessage('인증번호를 전송 중입니다...');
        setIsSendingCode(true);
        
        // 상태 초기화
        setIsCodeSent(false);
        setIsEmailVerified(false);

        try {
            const response = await authApi.sendSignupEmailCode(email);
            
            if (response.success) {
                setIsCodeSent(true);
                setTimer(180);
                setVerificationMessage(response.message || '입력하신 이메일로 인증번호가 전송되었습니다.');
            } else {
                setVerificationMessage(response.message || '인증번호 전송에 실패했습니다.');
                setError('email', { type: 'manual', message: response.message });
            }
        } catch (error: any) {
            console.error('인증번호 전송 실패:', error);
            const errorMessage = error.message || '인증번호 전송에 실패했습니다.';
            setVerificationMessage(`⚠️ ${errorMessage}`);
            setError('email', { type: 'manual', message: errorMessage });
        } finally {
            setIsSendingCode(false);
        }
    }, [trigger, clearErrors, getValues, setError]);

    // 인증번호 확인
    const handleVerifyCode = useCallback(async (e: React.MouseEvent<HTMLButtonElement>): Promise<void> => {
        e.preventDefault();
        
        const isAuthCodeValid: boolean = await trigger('authCode');
        if (!isAuthCodeValid) {
            return;
        }

        const email = getValues('email');
        const authCode = getValues('authCode');
        setIsVerifyingCode(true);
        clearErrors('authCode');
        setVerificationMessage('인증번호를 확인하는 중입니다...');

        try {
            const response = await authApi.verifyEmailCode(email, authCode);
            
            if (response.success) {
                setIsEmailVerified(true);
                // setVerificationMessage(response.message || '✅ 이메일 인증이 완료되었습니다.');
            } else {
                setVerificationMessage('⚠️ ' + (response.message || '인증번호가 일치하지 않습니다.'));
                // setError('authCode', { type: 'manual', message: response.message || '인증번호가 일치하지 않습니다.' });
            }
        } catch (error: any) {
            console.error('인증번호 확인 실패:', error);
            const errorMessage = error.message || '인증번호 확인에 실패했습니다.';
            setVerificationMessage('⚠️ ' + errorMessage);
            setError('authCode', { type: 'manual', message: errorMessage });
        } finally {
            setIsVerifyingCode(false);
        }
    }, [getValues, trigger, setError, clearErrors]);

    // 회원가입
    const onSubmit: SubmitHandler<SignupFormInputs> = async (data) => {
        if (!isEmailVerified) {
            alert('이메일 인증을 먼저 완료해주세요.');
            return;
        }

        // 필수 약관 체크
        const requiredTerms = terms.filter(t => t.isRequired);
        const hasAllRequiredTerms = requiredTerms.every(t => 
            selectedTermsIds.includes(t.id)
        );
        
        if (!hasAllRequiredTerms) {
            alert('필수 약관에 모두 동의해주세요.');
            return;
        }

        try {
            const response = await authApi.signup({
                nickname: data.nickname,
                email: data.email,
                password: data.password,
                authCode: data.authCode,
                agreedTermsIds: selectedTermsIds,
            });

            if (response.success) {
                alert(response.message || `회원가입 성공: ${data.nickname}님, 로그인 페이지로 이동합니다.`);
                navigate('/login');
            } else {
                alert(response.message || '회원가입에 실패했습니다.');
            }
        } catch (error: any) {
            console.error('회원가입 실패:', error);
            const errorMessage = error.message || '회원가입에 실패했습니다.';
            alert(errorMessage);
        }
    };

    const handleLoginClick = (): void => {
        navigate('/login');
    };

    useEffect(() => {
        if (terms.length === 0) {
            fetchTerms();
        }
    }, []);

    const fetchTerms = async () => {
        try {
            const termsData = await authApi.getAllTerms();
            setTerms(termsData);
        } catch (error: any) {
            console.error('약관 조회 실패:', error);
            alert(error.message || '약관을 불러오는데 실패했습니다.');
        } finally {
            setLoadingTerms(false);
        }
    };

    const handleTermsView = (e: React.MouseEvent<HTMLButtonElement>): void => {
        e.preventDefault();
        const currentFormData = getValues();
        navigate('/terms', {
            state: {
                from: '/signup',
                formData: currentFormData,
                isCodeSent,
                isEmailVerified,
                selectedTermsIds
            }
        });
    };

    const handleTermsChange = (termId: number, checked: boolean) => {
        if (checked) {
            setSelectedTermsIds(prev => [...prev, termId]);
        } else {
            setSelectedTermsIds(prev => prev.filter(id => id !== termId));
        }
    };

    const handleAllTermsChange = (checked: boolean) => {
        if (checked) {
            setSelectedTermsIds(terms.map(t => t.id));
        } else {
            setSelectedTermsIds([]);
        }
    };

    const inputStyle = (hasError: FieldError | undefined, isReadOnly: boolean = false): string => {
        const isError: boolean = !!hasError;
        const focusStyle: string = isError
            ? 'focus:ring-danger focus:border-danger'
            : 'focus:ring-main-green focus:border-main-green';
        
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

    return (
        <div className="flex items-center justify-center min-h-screen bg-white-green p-4">
            <div className="w-full max-w-md p-8 space-y-6 bg-sub-ivory rounded-lg shadow-lg">
                <h2 className="text-2xl font-bold text-center text-gray-900">회원가입</h2>
                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    {/* 닉네임 필드 */}
                    <div>
                        <label htmlFor="nickname" className="block text-sm font-medium text-gray-700">닉네임</label>
                        <input
                            id="nickname"
                            type="text"
                            className={inputStyle(errors.nickname)}
                            {...register('nickname')}
                        />
                        {errors.nickname && (
                            <p className="mt-2 text-xs text-danger">{errors.nickname.message}</p>
                        )}
                    </div>
                    
                    {/* 이메일 필드 */}
                    <div>
                        <label htmlFor="email" className="block text-sm font-medium text-gray-700">이메일</label>
                        <div className="flex space-x-2">
                            <input
                                id="email"
                                type="email"
                                readOnly={isEmailVerified}
                                className={inputStyle(errors.email, isEmailVerified)}
                                {...register('email')}
                            />
                            <button
                                type="button"
                                onClick={handleSendCode}
                                disabled={isEmailVerified || isSendingCode}
                                className={`py-2 px-3 text-xs font-medium rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors whitespace-nowrap
                                ${isEmailVerified || isSendingCode
                                    ? 'bg-gray-400 text-gray-700 cursor-not-allowed'
                                    : 'bg-main-green hover:bg-sub-green text-white focus:ring-main-green'
                                }`}
                            >
                                {isSendingCode ? '전송 중...' : isCodeSent ? (isEmailVerified ? '인증 완료' : '재전송') : '인증번호 전송'}
                            </button>
                        </div>
                        {errors.email && (
                            <p className="mt-2 text-xs text-danger">
                                {errors.email?.message}
                            </p>
                        )}
                    </div>

                    {/* 인증번호 필드 */}
                    {isCodeSent && !isEmailVerified && (
                        <div>
                            <label htmlFor="authCode" className="block text-sm font-medium text-gray-700">인증번호</label>
                            <div className="flex space-x-2 items-center relative"> 
                                <input
                                    id="authCode"
                                    type="text"
                                    className={`${inputStyle(errors.authCode)} pr-20`} 
                                    {...register('authCode')}
                                />
                                {timer > 0 && (
                                    <span className={`absolute right-[7rem] text-xs font-medium ${timer <= 30 ? 'text-danger' : 'text-gray-500'}`}>
                                        {formatTime(timer)}
                                    </span>
                                )}

                                <button
                                    type="button"
                                    onClick={handleVerifyCode}
                                    disabled={isVerifyingCode}
                                    className={`py-2 px-3 text-sm font-medium rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors whitespace-nowrap
                                    ${isVerifyingCode 
                                        ? 'bg-gray-400 text-gray-700 cursor-not-allowed'
                                        : 'bg-main-green hover:bg-sub-green text-white focus:ring-main-green'
                                    }`}
                                >
                                    {isVerifyingCode ? '확인 중...' : '인증 확인'}
                                </button>
                            </div>
                            
                            <div className="mt-2 flex justify-between items-center">
                                <p className={`text-xs ${verificationMessage.startsWith('✅') ? 'text-main-green' : 'text-danger'} text-left`}>
                                    {verificationMessage}
                                </p>
                            </div>
                            
                            {errors.authCode && (
                                <p className="mt-2 text-xs text-danger">{errors.authCode.message}</p>
                            )}
                        </div>
                    )}

                    {/* 비밀번호 필드 */}
                    <div>
                        <label htmlFor="password" className="block text-sm font-medium text-gray-700">비밀번호</label>
                        <input
                            id="password"
                            type="password"
                            className={inputStyle(errors.password)}
                            {...register('password')}
                        />
                        <p className="text-xs text-gray-500 mt-1">
                            8자 이상, 특수문자 포함
                        </p>
                        {errors.password && (
                            <p className="mt-2 text-xs text-danger">{errors.password.message}</p>
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

                    {/* 약관 동의 */}
                    <div className="space-y-3">
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            서비스 이용약관 동의
                        </label>
                        
                        {loadingTerms ? (
                            <p className="text-sm text-gray-500">약관을 불러오는 중...</p>
                        ) : (
                            <div className="space-y-2">
                                {/* 전체 동의 체크박스 */}
                                <div className="flex items-center p-3 bg-gray-50 rounded-md">
                                    <input
                                        type="checkbox"
                                        id="allTerms"
                                        checked={selectedTermsIds.length === terms.length && terms.length > 0}
                                        onChange={(e) => handleAllTermsChange(e.target.checked)}
                                        className="h-4 w-4 text-main-green focus:ring-main-green border-gray-300 rounded"
                                    />
                                    <label htmlFor="allTerms" className="ml-2 text-sm font-medium text-gray-900">
                                        전체 동의
                                    </label>
                                </div>

                                {/* 개별 약관 체크박스 */}
                                {terms.map((term) => (
                                    <div key={term.id} className="flex items-center justify-between p-2 hover:bg-gray-50 rounded">
                                        <div className="flex items-center flex-1">
                                            <input
                                                type="checkbox"
                                                id={`term-${term.id}`}
                                                checked={selectedTermsIds.includes(term.id)}
                                                onChange={(e) => handleTermsChange(term.id, e.target.checked)}
                                                className="h-4 w-4 text-main-green focus:ring-main-green border-gray-300 rounded"
                                            />
                                            <label htmlFor={`term-${term.id}`} className="ml-2 text-sm text-gray-700">
                                                {term.title}
                                                {term.isRequired && (
                                                    <span className="ml-1 text-red-600">(필수)</span>
                                                )}
                                            </label>
                                        </div>
                                        <button
                                            type="button"
                                            onClick={handleTermsView}
                                            // onClick={() => {
                                            //     navigate('/terms', {
                                            //         state: {
                                            //             from: '/signup',
                                            //             formData: getValues()
                                            //         }
                                            //     });
                                            // }}
                                            className="text-xs text-main-green hover:underline"
                                        >
                                            내용보기
                                        </button>
                                    </div>
                                ))}
                            </div>
                        )}

                        {/* 필수 약관 미동의 시 에러 메시지 */}
                        {terms.some(t => t.isRequired) && selectedTermsIds.length === 0 && (
                            <p className="mt-1 text-sm text-red-600">
                                필수 약관에 동의해주세요.
                            </p>
                        )}
                    </div>

                    <button
                        type="submit"
                        disabled={isSubmitting || !isValid || !isEmailVerified}
                        className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-main-green hover:bg-sub-green focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-main-green disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {isSubmitting ? '가입 중...' : '회원가입'}
                    </button>
                </form>

                <div className="mt-4 flex flex-col items-center text-sm space-y-1">
                    <p className="font-medium text-gray-400">
                        이미 계정이 있으신가요?
                        <a onClick={handleLoginClick}
                            className="font-medium text-sub-green hover:text-main-green transition-colors cursor-pointer"> 로그인</a>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default SignupForm;