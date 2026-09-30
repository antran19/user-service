-- auction-service's AuctionController/BidController enforce these privilege codes via
-- @RequiresPrivilege (AUCTION.VIEW/LIST/VIEW_BID_HISTORY exist in the SRS's privilege table but
-- are not gated anywhere — their endpoints are public, same treatment as PRODUCT.VIEW/LIST/SEARCH
-- in V4 — so, matching that precedent, they are deliberately NOT seeded here either).
INSERT INTO privileges (code) VALUES
    ('AUCTION.CREATE'), ('AUCTION.UPDATE'), ('AUCTION.CANCEL'), ('AUCTION.ADMIN_CANCEL'), ('AUCTION.BID');

-- ADMIN already gets "every seeded privilege" per V2, but that INSERT ran at V2 time and does
-- not retroactively cover privileges added later (same caveat V4 and V5 both had to repeat).
INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'ADMIN'), id
FROM privileges
WHERE code IN ('AUCTION.CREATE', 'AUCTION.UPDATE', 'AUCTION.CANCEL', 'AUCTION.ADMIN_CANCEL', 'AUCTION.BID');

-- SELLER creates and manages its own auctions, and may also bid on other sellers' auctions.
INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'SELLER'), id
FROM privileges
WHERE code IN ('AUCTION.CREATE', 'AUCTION.UPDATE', 'AUCTION.CANCEL', 'AUCTION.BID');

-- BUYER only ever bids; it cannot create, edit, or cancel an auction.
INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE code = 'BUYER'), id
FROM privileges
WHERE code IN ('AUCTION.BID');

-- AUCTION.ADMIN_CANCEL is intentionally ADMIN-only (not given to SELLER), mirroring
-- PRODUCT.MANAGE_ANY in V5: it is the signal that distinguishes an admin's override power from
-- an ordinary seller's own-auction-only power.
