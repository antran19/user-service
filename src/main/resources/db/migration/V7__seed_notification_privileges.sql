-- notification-service's NotificationController enforces this via @RequiresPrivilege
-- on GET /api/v1/notifications (the audit endpoint). /me and /{id}/read need no
-- privilege beyond being authenticated, matching how the audit-only distinction
-- is drawn in the notification-service design spec.
INSERT INTO privileges (code) VALUES ('NOTIFICATION.AUDIT');

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'ADMIN'), id
FROM privileges
WHERE code = 'NOTIFICATION.AUDIT';
