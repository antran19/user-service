-- Which (order or auction, buyer, seller) triples have really happened, so a rating can be checked
-- against them instead of trusting whatever transactionId the caller sends. Filled from the
-- OrderShipped event of commerce-service; it is this service's own copy, there is no foreign key
-- into another service. transaction_id is the order id (type ORDER) or the auction id (type AUCTION).
-- Ids are plain text: they come from events and the table must never reject one.
CREATE TABLE ratable_transactions (
    transaction_id VARCHAR(255) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    buyer_id VARCHAR(255) NOT NULL,
    seller_id VARCHAR(255) NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (transaction_id, transaction_type)
);
