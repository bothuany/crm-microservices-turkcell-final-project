-- test_data.sql
-- Test data for development and testing environments

-- Insert test bills
INSERT INTO bills (id, customer_id, contract_id, plan_id, amount, due_date, status)
VALUES 
    ('11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', 100.00, CURRENT_TIMESTAMP + INTERVAL '30 days', 'PENDING'),
    ('55555555-5555-5555-5555-555555555555', '66666666-6666-6666-6666-666666666666', '77777777-7777-7777-7777-777777777777', '88888888-8888-8888-8888-888888888888', 200.00, CURRENT_TIMESTAMP + INTERVAL '15 days', 'PENDING');

-- Insert test payments
INSERT INTO payments (id, bill_id, amount, payment_method, status, transaction_id)
VALUES 
    ('99999999-9999-9999-9999-999999999999', '11111111-1111-1111-1111-111111111111', 100.00, 'CREDIT_CARD', 'COMPLETED', 'TRX-123456'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '55555555-5555-5555-5555-555555555555', 200.00, 'BANK_TRANSFER', 'COMPLETED', 'TRX-789012');

-- Insert test refunds
INSERT INTO refunds (id, payment_id, bill_id, amount, reason, status)
VALUES 
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '99999999-9999-9999-9999-999999999999', '11111111-1111-1111-1111-111111111111', 50.00, 'Partial refund requested by customer', 'COMPLETED'); 