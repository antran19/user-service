-- The registration form collects more than the three fields users stored until now. These are the
-- ones that matter for an auction site: a phone number, the date of birth (the 18+ rule), and the
-- moment the person accepted the terms of service and the escrow rules (proof of consent).
-- All four are nullable: accounts created before this migration, or by an admin, do not have them.
ALTER TABLE users
    ADD COLUMN phone VARCHAR(20),
    ADD COLUMN birth_date DATE,
    ADD COLUMN terms_accepted_at TIMESTAMPTZ,
    ADD COLUMN escrow_acknowledged_at TIMESTAMPTZ;

-- The application normalises and validates these values; the database refuses anything else too.
ALTER TABLE users
    ADD CONSTRAINT chk_users_phone_format
        CHECK (phone IS NULL OR phone ~ '^(0[0-9]{9}|\+84[0-9]{9})$'),
    ADD CONSTRAINT chk_users_birth_date_plausible
        CHECK (birth_date IS NULL OR birth_date >= DATE '1900-01-01');
