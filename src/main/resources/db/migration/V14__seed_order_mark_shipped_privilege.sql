-- ORDER.MARK_SHIPPED: not in the original SRS privilege table (shipping wasn't scoped for
-- v1.0 beyond inventory/warehouse). New privilege for commerce-service's minimal "Seller
-- declares an order shipped" flow (no carrier/tracking, see RefundOrderUseCase/ReturnRequest
-- for why this flag matters). Seller-only, same as the Seller-owns-their-own-sales reasoning
-- V9 used for ORDER.VIEW/ORDER.LIST.
INSERT INTO privileges (code) VALUES ('ORDER.MARK_SHIPPED');

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'ADMIN'), id
FROM privileges WHERE code = 'ORDER.MARK_SHIPPED';

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'SELLER'), id
FROM privileges WHERE code = 'ORDER.MARK_SHIPPED';
