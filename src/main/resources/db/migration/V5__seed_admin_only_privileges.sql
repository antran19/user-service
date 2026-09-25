-- PRODUCT.MANAGE_ANY lets its holder update/delete/change the status of ANY seller's product,
-- bypassing catalog-service's owner-only check (ProductOwnershipPolicy). It exists because
-- JwtAuthenticationFilter only turns the JWT's privileges into authorities (the role claim is
-- dropped), and ADMIN and SELLER hold the same PRODUCT.* privileges -- so without a dedicated
-- privilege there is no signal that distinguishes an admin from a seller at all.
INSERT INTO privileges (code) VALUES
    ('PRODUCT.MANAGE_ANY');

-- ADMIN only. SELLER must NOT receive this: sellers may only manage their own products.
INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'ADMIN'), id
FROM privileges
WHERE code IN ('PRODUCT.MANAGE_ANY');
