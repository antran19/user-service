-- catalog-service's ProductController/CategoryController enforce these privilege codes via
-- @RequiresPrivilege, but they were never seeded here when those endpoints were built (a gap
-- surfaced only once Task 19 wired everything together end-to-end through docker-compose).
-- Without this migration, no role -- not even ADMIN -- can create a category or product.
INSERT INTO privileges (code) VALUES
    ('CATEGORY.CREATE'), ('CATEGORY.UPDATE'), ('CATEGORY.DELETE'),
    ('PRODUCT.CREATE'), ('PRODUCT.UPDATE'), ('PRODUCT.DELETE');

-- ADMIN already gets "every seeded privilege" per V2, but that INSERT ran at V2 time and
-- does not retroactively cover privileges added later, so it must be repeated here.
INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'ADMIN'), id
FROM privileges
WHERE code IN ('CATEGORY.CREATE', 'CATEGORY.UPDATE', 'CATEGORY.DELETE',
               'PRODUCT.CREATE', 'PRODUCT.UPDATE', 'PRODUCT.DELETE');

-- SELLER manages its own products only. CATEGORY.* stays ADMIN-only per the catalog-service
-- design spec: categories are shared global taxonomy with no owner, so letting any seller
-- rename/delete them would let one seller break every other seller's listings. (Per-product
-- ownership is enforced in catalog-service itself; see V5 for the ADMIN-only override.)
INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'SELLER'), id
FROM privileges
WHERE code IN ('PRODUCT.CREATE', 'PRODUCT.UPDATE', 'PRODUCT.DELETE');
