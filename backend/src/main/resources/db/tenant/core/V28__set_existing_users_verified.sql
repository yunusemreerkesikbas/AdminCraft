-- Set existing users as email verified (they already have passwords set)
UPDATE users SET email_verified = TRUE WHERE email_verified = FALSE OR email_verified IS NULL;
