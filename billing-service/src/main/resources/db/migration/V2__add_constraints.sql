-- V2__add_constraints.sql
-- Add constraints to existing tables

-- Bill constraints
ALTER TABLE bills
    ADD CONSTRAINT chk_bill_amount CHECK (amount > 0),
    ADD CONSTRAINT chk_bill_status CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED', 'REFUNDED'));

-- Payment constraints
ALTER TABLE payments
    ADD CONSTRAINT chk_payment_amount CHECK (amount > 0),
    ADD CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    ADD CONSTRAINT chk_payment_method CHECK (payment_method IN ('CREDIT_CARD', 'BANK_TRANSFER', 'WALLET'));

-- Refund constraints
ALTER TABLE refunds
    ADD CONSTRAINT chk_refund_amount CHECK (amount > 0),
    ADD CONSTRAINT chk_refund_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'));

-- Add foreign key constraints
ALTER TABLE payments
    ADD CONSTRAINT fk_payment_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE;

ALTER TABLE refunds
    ADD CONSTRAINT fk_refund_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_refund_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE; 