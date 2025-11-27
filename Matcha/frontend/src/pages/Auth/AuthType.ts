// src/Auth/AuthType.ts
export interface LoginFormInputs {
    email: string;
    password: string;
}

export interface SignupFormInputs extends LoginFormInputs {
    nickname: string;
    confirmPassword: string;
    authCode: string;
}

export interface ForgotPasswordInputs {
    email: string;
    authCode: string;
    newPassword: string;
    confirmPassword: string;
}
