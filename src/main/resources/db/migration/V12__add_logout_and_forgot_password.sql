-- Logout: blacklist a token's jti until it would have expired naturally anyway (checked by
-- JwtAuthenticationFilter in common-security on every request).
CREATE TABLE blacklisted_tokens (
    jti VARCHAR(64) PRIMARY KEY,
    expires_at TIMESTAMPTZ NOT NULL
);

-- Forget password: token_hash is a deterministic SHA-256 digest of the raw one-time token
-- (not bcrypt -- the raw value already has enough entropy, and a deterministic hash lets
-- ResetPasswordUseCase look the row up directly instead of scanning every unexpired row).
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_password_reset_tokens_token_hash ON password_reset_tokens (token_hash);
