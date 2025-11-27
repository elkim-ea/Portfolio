package com.matchaworld.backend.service.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    /**
     * 회원가입 인증번호 이메일 전송
     */
    public void sendSignupVerificationEmail(String toEmail, String code) {
        String subject = "[Matcha World] 회원가입 인증번호";
        String htmlContent = buildSignupEmailContent(code);
        
        sendHtmlEmail(toEmail, subject, htmlContent);
    }
    
    /**
     * 비밀번호 재설정 인증번호 이메일 전송
     */
    public void sendPasswordResetEmail(String toEmail, String code) {
        String subject = "[Matcha World] 비밀번호 재설정 인증번호";
        String htmlContent = buildPasswordResetEmailContent(code);
        
        sendHtmlEmail(toEmail, subject, htmlContent);
    }
    
    /**
     * HTML 이메일 전송
     */
    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML 형식
            
            mailSender.send(message);
            log.info("이메일 전송 성공: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("이메일 전송 실패: {}", toEmail, e);
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }
    
    /**
     * 회원가입 이메일 HTML 템플릿
     */
    private String buildSignupEmailContent(String code) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { 
                        font-family: 'Malgun Gothic', 'Apple SD Gothic Neo', sans-serif; 
                        line-height: 1.6; 
                        color: #333; 
                        margin: 0;
                        padding: 0;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 0 auto; 
                        padding: 20px; 
                    }
                    .header { 
                        background-color: #66BB6A; 
                        color: white; 
                        padding: 30px 20px; 
                        text-align: center; 
                        border-radius: 10px 10px 0 0; 
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .content { 
                        background-color: #f9f9f9; 
                        padding: 40px 30px; 
                        border-radius: 0 0 10px 10px; 
                    }
                    .code-box { 
                        background-color: white; 
                        border: 3px dashed #66BB6A; 
                        padding: 30px 20px; 
                        text-align: center; 
                        margin: 30px 0; 
                        border-radius: 10px; 
                    }
                    .code { 
                        font-size: 36px; 
                        font-weight: bold; 
                        color: #66BB6A; 
                        letter-spacing: 8px; 
                        font-family: 'Courier New', monospace;
                    }
                    .footer { 
                        text-align: center; 
                        margin-top: 20px; 
                        color: #999; 
                        font-size: 12px; 
                    }
                    .warning { 
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        color: #856404; 
                        padding: 15px;
                        margin-top: 20px; 
                        border-radius: 5px;
                    }
                    .warning strong {
                        margin-bottom: 5px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🌱 Matcha World 회원가입</h1>
                    </div>
                    <div class="content">
                        <p style="font-size: 16px; margin-bottom: 10px;">안녕하세요!</p>
                        <p style="font-size: 16px;">Matcha World에 가입해 주셔서 감사합니다.</p>
                        <p style="font-size: 16px;">아래 인증번호를 입력하여 회원가입을 완료해주세요.</p>
                        
                        <div class="code-box">
                            <p style="margin: 0; color: #666; font-size: 14px;">인증번호</p>
                            <div class="code">%s</div>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ 주의사항</strong>
                            <p style="margin: 5px 0;">• 이 인증번호는 <strong>3분간</strong> 유효합니다.</p>
                            <p style="margin: 5px 0;">• 본인이 요청하지 않은 경우, 이 이메일을 무시하셔도 됩니다.</p>
                            <p style="margin: 5px 0;">• 인증번호는 타인에게 절대 공유하지 마세요.</p>
                        </div>
                    </div>
                    <div class="footer">
                        <p>© 2025 Matcha World. All rights reserved.</p>
                        <p>이 이메일은 회원가입 인증을 위해 자동으로 발송되었습니다.</p>
                    </div>
                </div>
            </body>
            </html>
            """, code);
    }
    
    /**
     * 비밀번호 재설정 이메일 HTML 템플릿
     */
    private String buildPasswordResetEmailContent(String code) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { 
                        font-family: 'Malgun Gothic', 'Apple SD Gothic Neo', sans-serif; 
                        line-height: 1.6; 
                        color: #333; 
                        margin: 0;
                        padding: 0;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 0 auto; 
                        padding: 20px; 
                    }
                    .header { 
                        background-color: #2196F3; 
                        color: white; 
                        padding: 30px 20px; 
                        text-align: center; 
                        border-radius: 10px 10px 0 0; 
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .content { 
                        background-color: #f9f9f9; 
                        padding: 40px 30px; 
                        border-radius: 0 0 10px 10px; 
                    }
                    .code-box { 
                        background-color: white; 
                        border: 3px dashed #2196F3; 
                        padding: 30px 20px; 
                        text-align: center; 
                        margin: 30px 0; 
                        border-radius: 10px; 
                    }
                    .code { 
                        font-size: 36px; 
                        font-weight: bold; 
                        color: #2196F3; 
                        letter-spacing: 8px; 
                        font-family: 'Courier New', monospace;
                    }
                    .footer { 
                        text-align: center; 
                        margin-top: 20px; 
                        color: #999; 
                        font-size: 12px; 
                    }
                    .warning { 
                        background-color: #ffebee;
                        border-left: 4px solid #f44336;
                        color: #c62828; 
                        padding: 15px;
                        margin-top: 20px; 
                        border-radius: 5px;
                    }
                    .warning strong {
                        margin-bottom: 5px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔒 비밀번호 재설정</h1>
                    </div>
                    <div class="content">
                        <p style="font-size: 16px; margin-bottom: 10px;">비밀번호 재설정 요청을 받았습니다.</p>
                        <p style="font-size: 16px;">아래 인증번호를 입력하여 비밀번호를 재설정해주세요.</p>
                        
                        <div class="code-box">
                            <p style="margin: 0; color: #666; font-size: 14px;">인증번호</p>
                            <div class="code">%s</div>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ 보안 알림</strong>
                            <p style="margin: 5px 0;">• 이 인증번호는 <strong>3분간</strong> 유효합니다.</p>
                            <p style="margin: 5px 0;">• 본인이 요청하지 않은 경우, <strong>즉시 비밀번호를 변경</strong>해주세요.</p>
                            <p style="margin: 5px 0;">• 인증번호는 타인에게 절대 공유하지 마세요.</p>
                        </div>
                    </div>
                    <div class="footer">
                        <p>© 2025 Matcha World. All rights reserved.</p>
                        <p>이 이메일은 비밀번호 재설정 요청에 따라 자동으로 발송되었습니다.</p>
                    </div>
                </div>
            </body>
            </html>
            """, code);
    }
}