// src/Auth/AuthSchema.ts
import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { LoginFormInputs, SignupFormInputs, ForgotPasswordInputs } from './AuthType';

// 로그인 유효성 검사 스키마
export const loginSchema: ObjectSchema<LoginFormInputs> = yup.object().shape({
    email: yup
        .string()
        .email('유효하지 않은 이메일 형식입니다.')
        .required('이메일을 입력해주세요.'),
    password: yup
        .string()
        .min(8, '비밀번호는 최소 8자 이상이어야 합니다.')
        .matches(
            /^(?=.*[@$!%*?&#^()_+\-=\[\]{}|\\:";'<>,.\/~`])[A-Za-z\d@$!%*?&#^()_+\-=\[\]{}|\\:";'<>,.\/~`]{8,}$/,
            '비밀번호는 특수문자를 포함해야 합니다.'
        )
        .required('비밀번호를 입력해주세요.'),
});

// 회원가입 유효성 검사 스키마
export const signupSchema: ObjectSchema<SignupFormInputs> = yup.object().shape({
    nickname: yup
        .string()
        .required('닉네임을 입력해주세요.'),
    email: yup
        .string()
        .email('유효하지 않은 이메일 형식입니다.')
        .required('이메일을 입력해주세요.'),
    authCode: yup
        .string()
        .min(6, '인증번호는 6자리여야 합니다.')
        .required('인증번호를 입력해주세요.'),
    password: yup
        .string()
        .min(8, '비밀번호는 최소 8자 이상이어야 합니다.')
        .matches(
            /^(?=.*[@$!%*?&#^()_+\-=\[\]{}|\\:";'<>,.\/~`])[A-Za-z\d@$!%*?&#^()_+\-=\[\]{}|\\:";'<>,.\/~`]{8,}$/,
            '비밀번호는 특수문자를 포함해야 합니다.'
        )
        .required('비밀번호를 입력해주세요.'),
    confirmPassword: yup
        .string()
        .oneOf([yup.ref('password')], '비밀번호가 일치하지 않습니다.')
        .required('비밀번호를 확인해주세요.'),
});

// 이메일만 검증하는 스키마 (인증번호 전송 전 단계용)
export const emailOnlySchema = yup.object().shape({
    email: yup
        .string()
        .email('유효하지 않은 이메일 형식입니다.')
        .required('이메일을 입력해주세요.'),
});

// 비밀번호 재설정 유효성 검사 스키마
export const forgotPasswordSchema: ObjectSchema<ForgotPasswordInputs> = yup.object().shape({
    email: yup
        .string()
        .email('유효하지 않은 이메일 형식입니다.')
        .required('이메일을 입력해주세요.'),
    authCode: yup
        .string()
        .min(4, '인증번호는 6자리 이상이어야 합니다.')
        .required('인증번호를 입력해주세요.'),
    newPassword: yup
        .string()
        .min(8, '비밀번호는 8자 이상이어야 합니다.')
        .matches(
            /^(?=.*[@$!%*?&#^()_+\-=\[\]{}|\\:";'<>,.\/~`])[A-Za-z\d@$!%*?&#^()_+\-=\[\]{}|\\:";'<>,.\/~`]{8,}$/,
            '비밀번호는 특수문자를 포함해야 합니다.'
        )
        .required('새 비밀번호를 입력해주세요.'),
    confirmPassword: yup
        .string()
        .oneOf([yup.ref('newPassword')], '비밀번호가 일치하지 않습니다.')
        .required('비밀번호를 다시 한 번 입력해주세요.'),
});