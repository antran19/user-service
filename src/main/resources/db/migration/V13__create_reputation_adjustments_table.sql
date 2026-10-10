-- USER.REPUTATION.ADJUST (SRS privilege table): a manual admin override, separate from the
-- automatic AUCTION_PAYMENT_TIMEOUT penalty in reputation_penalties. delta is signed (an
-- admin override can raise or lower the score), unlike reputation_penalties.points which is
-- always a positive magnitude subtracted.
CREATE TABLE reputation_adjustments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    admin_id UUID NOT NULL REFERENCES users(id),
    delta INTEGER NOT NULL,
    reason TEXT NOT NULL,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_reputation_adjustments_user_id ON reputation_adjustments (user_id);

INSERT INTO privileges (code) VALUES ('USER.REPUTATION.ADJUST');

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'ADMIN'), id
FROM privileges WHERE code = 'USER.REPUTATION.ADJUST';
