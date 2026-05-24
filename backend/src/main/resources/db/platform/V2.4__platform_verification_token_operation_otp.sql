-- Allow OPERATION_OTP for platform admin policy-change verification
ALTER TABLE platform_verification_tokens
    MODIFY COLUMN token_type ENUM('LOGIN_OTP', 'OPERATION_OTP') NOT NULL;
