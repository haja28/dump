# Email Verification Setup Guide

## Overview
This guide provides step-by-step instructions to set up Google email configuration for user email verification in the Makan For You application.

## Implementation Summary

### New Components Created

#### 1. **Entities**
- `EmailVerificationToken.java` - Stores email verification tokens with expiration
- Updated `User.java` - Added `emailVerified` and `emailVerifiedAt` fields

#### 2. **Repositories**
- `EmailVerificationTokenRepository.java` - Repository for managing verification tokens

#### 3. **Services**
- `EmailService.java` - Handles sending verification emails using Google SMTP
- `VerificationService.java` - Manages verification token generation and validation

#### 4. **DTOs**
- `VerifyEmailRequest.java` - Request DTO for email verification
- `ResendVerificationEmailRequest.java` - Request DTO for resending verification emails
- `VerificationResponse.java` - Response DTO for verification operations
- Updated `UserDTO.java` - Added `emailVerified` field

#### 5. **Controllers**
- Updated `AuthController.java` - Added email verification endpoints

#### 6. **Dependencies**
- Added `spring-boot-starter-mail` - For email sending
- Added `spring-boot-starter-thymeleaf` - For HTML email templates

---

## Google Mail Configuration

### Step 1: Create a Google App Password

1. Go to [Google Account Security Settings](https://myaccount.google.com/security)
2. Enable 2-Step Verification if not already enabled
3. Navigate to **App passwords**
4. Select "Mail" and "Windows Computer" (or your device)
5. Google will generate a 16-character password
6. Copy this password (you'll need it in Step 4)

### Step 2: Update Environment Variables

Set the following environment variables on your system:

**Windows (PowerShell):**
```powershell
[Environment]::SetEnvironmentVariable("MAIL_USERNAME", "your-email@gmail.com", "User")
[Environment]::SetEnvironmentVariable("MAIL_PASSWORD", "your-16-char-app-password", "User")
[Environment]::SetEnvironmentVariable("VERIFICATION_LINK_BASE_URL", "http://localhost:3000", "User")
[Environment]::SetEnvironmentVariable("TOKEN_EXPIRATION_HOURS", "24", "User")
```

**Windows (Command Prompt):**
```cmd
setx MAIL_USERNAME "your-email@gmail.com"
setx MAIL_PASSWORD "your-16-char-app-password"
setx VERIFICATION_LINK_BASE_URL "http://localhost:3000"
setx TOKEN_EXPIRATION_HOURS "24"
```

**Linux/Mac (add to ~/.bashrc or ~/.zshrc):**
```bash
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-16-char-app-password"
export VERIFICATION_LINK_BASE_URL="http://localhost:3000"
export TOKEN_EXPIRATION_HOURS="24"
```

### Step 3: Update application.yml (if using local overrides)

The `application.yml` already contains the mail configuration with sensible defaults:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000

app:
  email:
    verification-link-base-url: ${VERIFICATION_LINK_BASE_URL:http://localhost:3000}
    verification-token-expiration-hours: ${TOKEN_EXPIRATION_HOURS:24}
```

---

## Email Verification Workflow

### 1. User Registration
- User registers with email and password
- New `User` record created with `emailVerified = false`
- Verification email sent to user's email address
- Registration response includes `emailVerified: false`

### 2. Email Verification
- User clicks verification link in email
- Frontend sends token to `/api/v1/auth/verify-email`
- Backend validates token (not expired, not used)
- `User.emailVerified` set to `true`
- `EmailVerificationToken.isUsed` set to `true`
- Welcome email sent to user
- User can now access full application features

### 3. Resend Verification Email
- If user doesn't receive email, can request resend
- POST to `/api/v1/auth/resend-verification-email`
- Existing unused tokens are deleted
- New verification email sent

---

## API Endpoints

### 1. Register User
```
POST /api/v1/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "1234567890",
  "password": "SecurePass123!",
  "role": "CUSTOMER"
}

Response:
{
  "status": "SUCCESS",
  "message": "User registered successfully",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "expiresIn": 900000,
    "user": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "phoneNumber": "1234567890",
      "role": "CUSTOMER",
      "isActive": true,
      "emailVerified": false,
      "createdAt": "2026-02-07T10:30:00",
      "lastLogin": null
    }
  }
}
```

### 2. Verify Email
```
POST /api/v1/auth/verify-email
Content-Type: application/json

{
  "token": "550e8400-e29b-41d4-a716-446655440000"
}

Response:
{
  "status": "SUCCESS",
  "message": "Email verified successfully",
  "data": {
    "message": "Email verified successfully",
    "verified": true,
    "user": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "phoneNumber": "1234567890",
      "role": "CUSTOMER",
      "isActive": true,
      "emailVerified": true,
      "createdAt": "2026-02-07T10:30:00",
      "lastLogin": null
    }
  }
}
```

### 3. Resend Verification Email
```
POST /api/v1/auth/resend-verification-email
Content-Type: application/json

{
  "email": "john.doe@example.com"
}

Response:
{
  "status": "SUCCESS",
  "message": "Verification email sent successfully",
  "data": "Verification email sent successfully"
}
```

### 4. Login
```
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "SecurePass123!"
}

Response:
{
  "status": "SUCCESS",
  "message": "Login successful",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "expiresIn": 900000,
    "user": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "phoneNumber": "1234567890",
      "role": "CUSTOMER",
      "isActive": true,
      "emailVerified": true,
      "createdAt": "2026-02-07T10:30:00",
      "lastLogin": "2026-02-07T10:35:00"
    }
  }
}
```

---

## Database Schema Updates

### Users Table
```sql
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN email_verified_at DATETIME NULL;
```

### Email Verification Tokens Table
```sql
CREATE TABLE email_verification_tokens (
    token_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at DATETIME NOT NULL,
    verified_at DATETIME NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
);
```

The Hibernate JPA configuration is set to `ddl-auto: update`, so tables will be created automatically on startup.

---

## Frontend Integration

### Verification Link Format
The verification links will be in the following format:
```
http://localhost:3000/verify-email?token=550e8400-e29b-41d4-a716-446655440000
```

### Frontend Implementation Steps

1. **On Registration Success:**
   - Display message: "Verification email sent. Please check your inbox."
   - Optionally show "Resend email" option after a delay

2. **Email Verification Page:**
   - Extract token from URL query parameter
   - Send token to `/api/v1/auth/verify-email`
   - Show success/error message
   - Redirect to login or dashboard

3. **Resend Email Page:**
   - Input field for email
   - Button to submit resend request
   - Show confirmation message

---

## Email Templates

### Verification Email
- Professional HTML template with brand colors
- Clear call-to-action button
- Direct link fallback
- 24-hour expiration notice

### Welcome Email
- Sent after successful verification
- Lists available features
- Invitation to explore application

### Password Reset Email (Future)
- Template already implemented in EmailService
- Ready for password reset functionality

---

## Security Considerations

1. **Token Security:**
   - Tokens are UUIDs (cryptographically secure)
   - Tokens expire after 24 hours (configurable)
   - Tokens can only be used once
   - Old tokens are automatically cleaned up

2. **Email Spoofing Prevention:**
   - Tokens are unique and non-guessable
   - Users must have access to registered email
   - Verification performed on backend

3. **Rate Limiting (Recommended):**
   - Consider adding rate limiting to resend endpoint
   - Limit failed verification attempts

4. **HTTPS (Required in Production):**
   - Always use HTTPS in production
   - Update `VERIFICATION_LINK_BASE_URL` to use https://

---

## Troubleshooting

### Email Not Sending

1. **Check Gmail App Password:**
   - Ensure you created a 16-character app password
   - Not the regular Gmail password
   - Verify 2FA is enabled on Google Account

2. **Check Environment Variables:**
   - Restart application after setting env vars
   - Verify they're set: `$env:MAIL_USERNAME` (PowerShell)

3. **Check Email Configuration:**
   - SMTP host: `smtp.gmail.com`
   - Port: `587` (TLS)
   - Not `465` (SSL)

4. **Check Firewall/Network:**
   - Port 587 must be open
   - Some networks block SMTP

### Tokens Expiring Too Quickly

1. Update `TOKEN_EXPIRATION_HOURS` environment variable
2. Default is 24 hours
3. Change via environment or application-dev.yml

### Database Migration Issues

1. Check Hibernate logs
2. Ensure JPA annotations are correct
3. Tables should be created on first startup

---

## Additional Features

### Future Enhancements

1. **Password Reset via Email:**
   - `EmailService.sendPasswordResetEmail()` already implemented
   - Requires password reset endpoint in AuthService

2. **Email Change Verification:**
   - Implement email change with verification
   - Store pending email in User entity

3. **Two-Factor Authentication:**
   - Use email for OTP delivery
   - Enhanced security for sensitive operations

4. **Email Notifications:**
   - Order confirmations
   - Delivery updates
   - Account activity alerts

---

## Testing

### Manual Testing Steps

1. **Register new user:**
   ```bash
   POST http://localhost:8081/api/v1/auth/register
   {
     "firstName": "Test",
     "lastName": "User",
     "email": "test@example.com",
     "phoneNumber": "1234567890",
     "password": "Password123!"
   }
   ```

2. **Check email inbox** for verification link

3. **Extract token** from email link

4. **Verify email:**
   ```bash
   POST http://localhost:8081/api/v1/auth/verify-email
   {
     "token": "extracted-token-here"
   }
   ```

5. **Verify response shows** `emailVerified: true`

---

## Configuration Summary

| Variable | Default | Description |
|----------|---------|-------------|
| `MAIL_USERNAME` | your-email@gmail.com | Gmail address |
| `MAIL_PASSWORD` | your-app-password | 16-char app password |
| `VERIFICATION_LINK_BASE_URL` | http://localhost:3000 | Frontend base URL |
| `TOKEN_EXPIRATION_HOURS` | 24 | Token validity period |

---

## File Structure

```
auth-service/
├── src/main/java/com/makanforyou/auth/
│   ├── entity/
│   │   ├── User.java (UPDATED)
│   │   └── EmailVerificationToken.java (NEW)
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── EmailVerificationTokenRepository.java (NEW)
│   ├── service/
│   │   ├── AuthService.java (UPDATED)
│   │   ├── EmailService.java (NEW)
│   │   └── VerificationService.java (NEW)
│   ├── dto/
│   │   ├── UserDTO.java (UPDATED)
│   │   ├── VerifyEmailRequest.java (NEW)
│   │   ├── ResendVerificationEmailRequest.java (NEW)
│   │   └── VerificationResponse.java (NEW)
│   └── controller/
│       └── AuthController.java (UPDATED)
└── src/main/resources/
    └── application.yml (UPDATED)
```

---

## Next Steps

1. Set Google email credentials in environment variables
2. Restart auth-service application
3. Test user registration and email verification
4. Integrate verification flow in frontend
5. Deploy to production with HTTPS
6. Monitor email delivery in logs
