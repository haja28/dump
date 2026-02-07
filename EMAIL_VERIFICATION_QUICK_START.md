# Email Verification - Quick Start

## 5-Minute Setup

### Step 1: Get Google App Password (2 minutes)

1. Go to https://myaccount.google.com/security
2. Scroll to "Your devices" section
3. Enable 2-Step Verification (if not enabled)
4. Navigate to "App passwords"
5. Select "Mail" and "Windows Computer"
6. Copy the 16-character password

### Step 2: Set Environment Variables (1 minute)

**PowerShell (Windows):**
```powershell
$email = "your-email@gmail.com"
$password = "your-16-char-app-password"
$verificationUrl = "http://localhost:3000"

[Environment]::SetEnvironmentVariable("MAIL_USERNAME", $email, "User")
[Environment]::SetEnvironmentVariable("MAIL_PASSWORD", $password, "User")
[Environment]::SetEnvironmentVariable("VERIFICATION_LINK_BASE_URL", $verificationUrl, "User")
[Environment]::SetEnvironmentVariable("TOKEN_EXPIRATION_HOURS", "24", "User")

# Verify
Get-ChildItem env: | Where-Object {$_.Name -like "MAIL_*" -or $_.Name -like "VERIFICATION_*"}
```

**Command Prompt (Windows):**
```cmd
setx MAIL_USERNAME "your-email@gmail.com"
setx MAIL_PASSWORD "your-16-char-app-password"
setx VERIFICATION_LINK_BASE_URL "http://localhost:3000"
setx TOKEN_EXPIRATION_HOURS "24"

# Restart command prompt to see changes
```

### Step 3: Restart Application (2 minutes)

1. Stop the auth-service if running
2. Restart the application
3. Verify startup logs show email configuration loaded

```
Spring Mail Configuration:
- Host: smtp.gmail.com
- Port: 587
- Username: your-email@gmail.com
```

---

## Test the Integration

### Test 1: User Registration
```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "1234567890",
    "password": "SecurePass123!",
    "role": "CUSTOMER"
  }'
```

**Expected Response:**
```json
{
  "status": "SUCCESS",
  "message": "User registered successfully",
  "data": {
    "user": {
      "emailVerified": false
    }
  }
}
```

**Check Email:** Look for verification email in inbox

### Test 2: Verify Email
```bash
curl -X POST http://localhost:8081/api/v1/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "token": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

**Expected Response:**
```json
{
  "status": "SUCCESS",
  "message": "Email verified successfully",
  "data": {
    "verified": true,
    "user": {
      "emailVerified": true
    }
  }
}
```

### Test 3: Resend Verification Email
```bash
curl -X POST http://localhost:8081/api/v1/auth/resend-verification-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com"
  }'
```

**Expected Response:**
```json
{
  "status": "SUCCESS",
  "message": "Verification email sent successfully"
}
```

---

## Key Features

✅ **User Registration**
- Creates user account
- Sets `emailVerified = false`
- Sends verification email immediately

✅ **Email Verification**
- Validates unique token
- Checks expiration (24 hours default)
- Updates user status to verified
- Sends welcome email

✅ **Resend Email**
- Deletes old unused tokens
- Generates new token
- Resends verification email

✅ **Security**
- UUID tokens (cryptographically secure)
- One-time use tokens
- Automatic token cleanup
- SMTP with TLS encryption

---

## Database Changes

The application will automatically create/update tables on startup:

```sql
-- New columns added to users table
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN email_verified_at DATETIME NULL;

-- New table for verification tokens
CREATE TABLE email_verification_tokens (
    token_id BIGINT AUTO_INCREMENT PRIMARY KEY,
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

---

## Troubleshooting Checklist

- [ ] Environment variables set and persisted
- [ ] Application restarted after env vars
- [ ] Gmail account has 2FA enabled
- [ ] App password created (16 characters)
- [ ] No firewall blocking port 587
- [ ] Check application logs for email errors
- [ ] Verify database tables created

---

## Email Template Features

### Verification Email
- Professional branded design
- Clear verification button
- Direct link fallback
- 24-hour expiration notice
- Company footer

### Welcome Email
- Sent after successful verification
- Lists features available
- Encourages exploration

### HTML Emails
- Responsive design
- Works on all clients
- Custom brand colors

---

## Configuration Properties

Location: `auth-service/src/main/resources/application.yml`

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

app:
  email:
    verification-link-base-url: ${VERIFICATION_LINK_BASE_URL:http://localhost:3000}
    verification-token-expiration-hours: ${TOKEN_EXPIRATION_HOURS:24}
```

---

## API Reference

### Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/v1/auth/register` | Register new user + send verification |
| POST | `/api/v1/auth/verify-email` | Verify email with token |
| POST | `/api/v1/auth/resend-verification-email` | Resend verification email |
| POST | `/api/v1/auth/login` | Login user |
| GET | `/api/v1/auth/me` | Get current user |

---

## Architecture Overview

```
User Registration
    ↓
Create User (emailVerified=false)
    ↓
Generate Verification Token
    ↓
Send Verification Email (via Gmail SMTP)
    ↓
User clicks link
    ↓
Frontend extracts token
    ↓
POST /verify-email with token
    ↓
Backend validates token
    ↓
Set emailVerified=true
    ↓
Mark token as used
    ↓
Send Welcome Email
    ↓
User can login and access features
```

---

## Production Checklist

- [ ] Update `VERIFICATION_LINK_BASE_URL` to production domain
- [ ] Update `MAIL_USERNAME` to production email
- [ ] Update `MAIL_PASSWORD` to production app password
- [ ] Use HTTPS in production (not HTTP)
- [ ] Consider adding rate limiting
- [ ] Monitor email delivery in logs
- [ ] Set up email bounce handling (future)
- [ ] Implement email unsubscribe (future)

---

## File Summary

### New Files Created
- `EmailService.java` - Email sending logic
- `VerificationService.java` - Verification token management
- `EmailVerificationToken.java` - Token entity
- `EmailVerificationTokenRepository.java` - Token repository
- `VerifyEmailRequest.java` - Request DTO
- `ResendVerificationEmailRequest.java` - Request DTO
- `VerificationResponse.java` - Response DTO

### Modified Files
- `User.java` - Added email verification fields
- `UserDTO.java` - Added emailVerified field
- `AuthService.java` - Integration with verification service
- `AuthController.java` - New verification endpoints
- `application.yml` - Email configuration
- `pom.xml` - Added mail and thymeleaf dependencies

---

## Support & Documentation

For detailed documentation, see: `EMAIL_VERIFICATION_SETUP_GUIDE.md`

For any issues:
1. Check application logs in `auth-service` directory
2. Verify environment variables are set
3. Test email credentials with external SMTP client
4. Review troubleshooting section above
