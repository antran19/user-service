-- SellerRequestStatus is an enum in Java, but the column is a plain VARCHAR(20). The partial unique
-- index from V8 relies on the exact value 'PENDING', so a misspelt status written outside the
-- application would silently slip past "one pending request per user".
ALTER TABLE seller_requests
    ADD CONSTRAINT chk_seller_requests_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'));
