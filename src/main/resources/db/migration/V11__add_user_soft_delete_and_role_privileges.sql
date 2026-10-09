-- SRS config USER_SOFT_DELETE=true: keep the row (FK references from ratings/orders/etc.
-- stay intact) but the account can no longer log in once deleted.
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMPTZ;

-- ROLE.CREATE/UPDATE/DELETE weren't seeded in V2 -- ROLE.VIEW/LIST were, and ADMIN already
-- has those from V2's blanket grant, but privileges added later never retroactively reach
-- that blanket grant (same caveat every migration since V4 has had to repeat).
INSERT INTO privileges (code) VALUES ('ROLE.CREATE'), ('ROLE.UPDATE'), ('ROLE.DELETE');

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'ADMIN'), id
FROM privileges WHERE code IN ('ROLE.CREATE', 'ROLE.UPDATE', 'ROLE.DELETE');
