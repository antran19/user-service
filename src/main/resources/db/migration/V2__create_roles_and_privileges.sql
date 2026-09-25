CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE privileges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE role_privileges (
    role_id UUID NOT NULL REFERENCES roles(id),
    privilege_id UUID NOT NULL REFERENCES privileges(id),
    PRIMARY KEY (role_id, privilege_id)
);

INSERT INTO privileges (code) VALUES
    ('AUTH.LOGIN'), ('AUTH.LOGOUT'),
    ('PROFILE.VIEW'), ('PROFILE.UPDATE'), ('PROFILE.CHANGE_PASSWORD'), ('PROFILE.RESET_PASSWORD'),
    ('USER.CREATE'), ('USER.VIEW'), ('USER.LIST'), ('USER.UPDATE'), ('USER.DELETE'), ('USER.CHANGE_PASSWORD'),
    ('ROLE.VIEW'), ('ROLE.LIST');

INSERT INTO roles (code, name) VALUES
    ('ADMIN', 'Administrator'), ('SELLER', 'Seller'), ('BUYER', 'Buyer'), ('SUPPORT_STAFF', 'Support Staff');

-- ADMIN: every seeded privilege
INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'ADMIN'), id FROM privileges;

-- BUYER and SELLER: self-service auth/profile privileges only
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r, privileges p
WHERE r.code IN ('BUYER', 'SELLER')
  AND p.code IN ('AUTH.LOGIN', 'AUTH.LOGOUT', 'PROFILE.VIEW', 'PROFILE.UPDATE', 'PROFILE.CHANGE_PASSWORD', 'PROFILE.RESET_PASSWORD');

-- SUPPORT_STAFF: auth/profile plus read-only user/role visibility
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r, privileges p
WHERE r.code = 'SUPPORT_STAFF'
  AND p.code IN ('AUTH.LOGIN', 'AUTH.LOGOUT', 'PROFILE.VIEW', 'PROFILE.UPDATE', 'PROFILE.CHANGE_PASSWORD',
                 'USER.VIEW', 'USER.LIST', 'ROLE.VIEW', 'ROLE.LIST');
