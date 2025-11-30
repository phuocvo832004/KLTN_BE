ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_link_id VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_code VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_orders_payment_code ON orders(payment_code);
CREATE INDEX IF NOT EXISTS idx_orders_payment_status ON orders(payment_status);

