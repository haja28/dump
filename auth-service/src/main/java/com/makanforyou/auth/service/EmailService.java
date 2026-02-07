package com.makanforyou.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending emails
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@makanforyou.com}")
    private String fromEmail;

    @Value("${app.email.verification-link-base-url:http://localhost:3000}")
    private String verificationLinkBaseUrl;

    /**
     * Send email verification link
     */
    public void sendVerificationEmail(String toEmail, String userName, String verificationToken) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String verificationLink = verificationLinkBaseUrl + "/verify-email?token=" + verificationToken;
            String htmlContent = buildVerificationEmailHtml(userName, verificationLink);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Email Verification - Makan For You");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Send welcome email after verification
     */
    public void sendWelcomeEmail(String toEmail, String userName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String htmlContent = buildWelcomeEmailHtml(userName);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Makan For You!");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String resetLink = verificationLinkBaseUrl + "/reset-password?token=" + resetToken;
            String htmlContent = buildPasswordResetEmailHtml(userName, resetLink);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - Makan For You");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Build HTML content for verification email
     */
    private String buildVerificationEmailHtml(String userName, String verificationLink) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n" +
                "    .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                "    .header { background-color: #FF6B6B; color: white; padding: 20px; border-radius: 5px 5px 0 0; text-align: center; }\n" +
                "    .content { background-color: #f4f4f4; padding: 20px; border-radius: 0 0 5px 5px; }\n" +
                "    .btn { display: inline-block; padding: 12px 30px; background-color: #FF6B6B; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }\n" +
                "    .footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <div class=\"header\">\n" +
                "      <h1>Makan For You</h1>\n" +
                "    </div>\n" +
                "    <div class=\"content\">\n" +
                "      <h2>Email Verification</h2>\n" +
                "      <p>Hi " + userName + ",</p>\n" +
                "      <p>Thank you for signing up! Please verify your email address by clicking the button below.</p>\n" +
                "      <p><a href=\"" + verificationLink + "\" class=\"btn\">Verify Email</a></p>\n" +
                "      <p>Or copy and paste this link in your browser:<br/>" + verificationLink + "</p>\n" +
                "      <p>This link will expire in 24 hours.</p>\n" +
                "      <p>If you didn't sign up for this account, please ignore this email.</p>\n" +
                "      <div class=\"footer\">\n" +
                "        <p>&copy; 2026 Makan For You. All rights reserved.</p>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Build HTML content for welcome email
     */
    private String buildWelcomeEmailHtml(String userName) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n" +
                "    .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                "    .header { background-color: #FF6B6B; color: white; padding: 20px; border-radius: 5px 5px 0 0; text-align: center; }\n" +
                "    .content { background-color: #f4f4f4; padding: 20px; border-radius: 0 0 5px 5px; }\n" +
                "    .footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <div class=\"header\">\n" +
                "      <h1>Welcome to Makan For You!</h1>\n" +
                "    </div>\n" +
                "    <div class=\"content\">\n" +
                "      <p>Hi " + userName + ",</p>\n" +
                "      <p>Your email has been verified successfully! Your account is now active and ready to use.</p>\n" +
                "      <p>You can now:</p>\n" +
                "      <ul>\n" +
                "        <li>Browse and order from our kitchen partners</li>\n" +
                "        <li>Track your orders in real-time</li>\n" +
                "        <li>Save your favorite meals</li>\n" +
                "        <li>Manage your profile and preferences</li>\n" +
                "      </ul>\n" +
                "      <p>Start exploring now and enjoy delicious food from our partner kitchens!</p>\n" +
                "      <div class=\"footer\">\n" +
                "        <p>&copy; 2026 Makan For You. All rights reserved.</p>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Build HTML content for password reset email
     */
    private String buildPasswordResetEmailHtml(String userName, String resetLink) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n" +
                "    .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                "    .header { background-color: #FF6B6B; color: white; padding: 20px; border-radius: 5px 5px 0 0; text-align: center; }\n" +
                "    .content { background-color: #f4f4f4; padding: 20px; border-radius: 0 0 5px 5px; }\n" +
                "    .btn { display: inline-block; padding: 12px 30px; background-color: #FF6B6B; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }\n" +
                "    .footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <div class=\"header\">\n" +
                "      <h1>Makan For You</h1>\n" +
                "    </div>\n" +
                "    <div class=\"content\">\n" +
                "      <h2>Password Reset Request</h2>\n" +
                "      <p>Hi " + userName + ",</p>\n" +
                "      <p>We received a request to reset your password. Click the button below to create a new password.</p>\n" +
                "      <p><a href=\"" + resetLink + "\" class=\"btn\">Reset Password</a></p>\n" +
                "      <p>Or copy and paste this link in your browser:<br/>" + resetLink + "</p>\n" +
                "      <p>This link will expire in 1 hour.</p>\n" +
                "      <p>If you didn't request this, please ignore this email.</p>\n" +
                "      <div class=\"footer\">\n" +
                "        <p>&copy; 2026 Makan For You. All rights reserved.</p>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
