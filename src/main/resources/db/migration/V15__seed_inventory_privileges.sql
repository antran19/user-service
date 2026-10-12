-- INVENTORY.MANAGE / INVENTORY.VIEW: not in the SRS privilege table (fulfillment's REST API
-- had no gating beyond "authenticated"). MANAGE covers intake and manual reserve/commit/
-- release -- Admin only, since in normal operation commerce-service drives those via Kafka.
-- VIEW is read-only stock/reservation lookup.
INSERT INTO privileges (code) VALUES ('INVENTORY.MANAGE'), ('INVENTORY.VIEW');

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'ADMIN'), id
FROM privileges WHERE code IN ('INVENTORY.MANAGE', 'INVENTORY.VIEW');

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = r.code), p.id
FROM (VALUES ('SELLER'), ('SUPPORT_STAFF')) AS r(code), privileges p
WHERE p.code = 'INVENTORY.VIEW';
