CREATE TABLE reputation_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    score INTEGER NOT NULL DEFAULT 50,
    total_ratings INTEGER NOT NULL DEFAULT 0,
    rating_sum INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ratings (
    id UUID PRIMARY KEY,
    transaction_type VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    rater_id UUID NOT NULL REFERENCES users(id),
    rated_user_id UUID NOT NULL REFERENCES users(id),
    score INTEGER NOT NULL,
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- A rater may only rate a given transaction once -- the DB-level guard against a
-- double-submit, same pattern as idx_seller_requests_one_pending_per_user in V8.
CREATE UNIQUE INDEX idx_ratings_one_per_transaction_per_rater ON ratings (transaction_id, rater_id);
CREATE INDEX idx_ratings_rated_user_id ON ratings (rated_user_id);

CREATE TABLE reputation_penalties (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    reason VARCHAR(50) NOT NULL,
    points INTEGER NOT NULL,
    reference_id VARCHAR(255),
    applied_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Guards against a redelivered AuctionPaymentTimeoutEvent (Kafka is at-least-once) applying
-- the same penalty twice for the same auction.
CREATE UNIQUE INDEX idx_reputation_penalties_one_per_reference_reason
    ON reputation_penalties (reference_id, reason) WHERE reference_id IS NOT NULL;
CREATE INDEX idx_reputation_penalties_user_id ON reputation_penalties (user_id);

-- REPUTATION.RATE (Buyer/Seller), REPUTATION.VIEW (all roles), REPUTATION.PENALTY.VIEW
-- (Admin/Support) per the SRS privilege table (section 2.7). REPUTATION.PENALTY.APPLY is
-- deliberately not seeded yet -- no endpoint uses it in this pass (penalties are applied
-- automatically from the AuctionPaymentTimeout event, not through a manual admin action).
INSERT INTO privileges (code) VALUES ('REPUTATION.VIEW'), ('REPUTATION.RATE'), ('REPUTATION.PENALTY.VIEW');

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'ADMIN'), id
FROM privileges WHERE code IN ('REPUTATION.VIEW', 'REPUTATION.RATE', 'REPUTATION.PENALTY.VIEW');

INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id FROM roles r, privileges p
WHERE r.code IN ('BUYER', 'SELLER') AND p.code IN ('REPUTATION.VIEW', 'REPUTATION.RATE');

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'SUPPORT_STAFF'), id
FROM privileges WHERE code IN ('REPUTATION.VIEW', 'REPUTATION.PENALTY.VIEW');
