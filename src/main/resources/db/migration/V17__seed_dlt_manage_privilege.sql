-- SYSTEM.DLT.MANAGE: lets an Admin inspect and replay Kafka dead letters on any service that
-- consumes events (GET /internal/dlt, POST /internal/dlt/replay, from common-kafka). Those
-- endpoints are not routed by the gateway; they are called on each service's own port.
INSERT INTO privileges (code) VALUES ('SYSTEM.DLT.MANAGE');

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'ADMIN'), p.id
FROM privileges p
WHERE p.code = 'SYSTEM.DLT.MANAGE';
