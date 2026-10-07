-- commerce-service's CartController/OrderController/PaymentController enforce these privilege
-- codes via @RequiresPrivilege. Role assignment follows the SRS privilege table exactly
-- (section 2.7): CART.* and CHECKOUT.START are Buyer-only; ORDER.CREATE is Buyer-only;
-- ORDER.CANCEL/VIEW/LIST/REFUND follow the table's X marks per role.
INSERT INTO privileges (code) VALUES
    ('CART.CREATE'), ('CART.UPDATE'), ('CART.REMOVE_ITEM'), ('CART.VIEW'),
    ('CHECKOUT.START'), ('ORDER.CREATE'), ('ORDER.CANCEL'), ('ORDER.VIEW'),
    ('ORDER.LIST'), ('ORDER.REFUND');

-- ADMIN already gets "every seeded privilege" per V2, but that INSERT ran at V2 time and does
-- not retroactively cover privileges added later (same caveat V4/V5/V6/V7 each had to repeat).
INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'ADMIN'), id
FROM privileges
WHERE code IN ('CART.CREATE', 'CART.UPDATE', 'CART.REMOVE_ITEM', 'CART.VIEW', 'CHECKOUT.START',
               'ORDER.CREATE', 'ORDER.CANCEL', 'ORDER.VIEW', 'ORDER.LIST', 'ORDER.REFUND');

-- BUYER: the only role that shops -- full cart/checkout/order-creation/cancel/view rights.
INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'BUYER'), id
FROM privileges
WHERE code IN ('CART.CREATE', 'CART.UPDATE', 'CART.REMOVE_ITEM', 'CART.VIEW', 'CHECKOUT.START',
               'ORDER.CREATE', 'ORDER.CANCEL', 'ORDER.VIEW', 'ORDER.LIST');

-- SELLER: per the SRS table, sellers only ever view orders (their own sales), never cart/
-- checkout/cancel/refund -- those are buyer- or admin/support-side actions.
INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'SELLER'), id
FROM privileges
WHERE code IN ('ORDER.VIEW', 'ORDER.LIST');

-- SUPPORT_STAFF: handles disputes -- can view/list/cancel any order and process refunds, but
-- never shops (no CART.*/CHECKOUT.START/ORDER.CREATE).
INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'SUPPORT_STAFF'), id
FROM privileges
WHERE code IN ('ORDER.CANCEL', 'ORDER.VIEW', 'ORDER.LIST', 'ORDER.REFUND');
