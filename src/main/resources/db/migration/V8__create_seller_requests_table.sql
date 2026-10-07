CREATE TABLE seller_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    reviewed_at TIMESTAMPTZ,
    reviewed_by UUID
);

-- Enforces "at most one PENDING request per user" at the database level, not just in
-- application code -- a partial unique index only applies to rows where status = 'PENDING',
-- so a user can freely accumulate APPROVED/REJECTED history without hitting the constraint.
CREATE UNIQUE INDEX idx_seller_requests_one_pending_per_user
    ON seller_requests (user_id) WHERE status = 'PENDING';

-- Lets an admin review the request queue without a full table scan.
CREATE INDEX idx_seller_requests_status ON seller_requests (status);

-- ADMIN-only: distinguishes an admin's review power over seller requests from a seller's
-- own privileges, same reasoning as PRODUCT.MANAGE_ANY in V5.
INSERT INTO privileges (code) VALUES
    ('USER.REVIEW_SELLER_REQUESTS');

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'ADMIN'), id
FROM privileges
WHERE code IN ('USER.REVIEW_SELLER_REQUESTS');
