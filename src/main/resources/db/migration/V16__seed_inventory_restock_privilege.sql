-- INVENTORY.RESTOCK: lets a Seller add stock to their OWN products (POST /inventory/{productId}/
-- restock). Ownership is enforced by fulfillment-service itself, which records the seller from
-- the ProductCreated event, so the privilege alone does not allow touching someone else's stock.
-- Admin gets it too so the same endpoint works for them (Admin also holds INVENTORY.MANAGE,
-- which lets them restock any product).
INSERT INTO privileges (code) VALUES ('INVENTORY.RESTOCK');

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = r.code), p.id
FROM (VALUES ('SELLER'), ('ADMIN')) AS r(code), privileges p
WHERE p.code = 'INVENTORY.RESTOCK';
