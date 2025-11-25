-- Migration: Create Branch, Sale, and Stocking tables
-- Version: 2
-- Description: Tạo các bảng để quản lý chi nhánh, sale và tồn kho theo chi nhánh

-- Tạo bảng branches (Chi nhánh)
CREATE TABLE IF NOT EXISTS branches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    address VARCHAR(500),
    phone VARCHAR(50),
    email VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng sales (Chương trình Sale)
CREATE TABLE IF NOT EXISTS sales (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    branch_id BIGINT NOT NULL,
    sale_price DECIMAL(38,2) NOT NULL,
    discount_percentage DECIMAL(5,2),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sale_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_sale_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    CONSTRAINT uk_sale_product_branch UNIQUE (product_id, branch_id)
);

-- Tạo bảng stockings (Tồn kho)
CREATE TABLE IF NOT EXISTS stockings (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    branch_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    min_stock_level INTEGER NOT NULL DEFAULT 0,
    last_restocked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stocking_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_stocking_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    CONSTRAINT uk_stocking_product_branch UNIQUE (product_id, branch_id)
);

-- Tạo indexes cho bảng sales
CREATE INDEX IF NOT EXISTS idx_sale_product_branch ON sales(product_id, branch_id);
CREATE INDEX IF NOT EXISTS idx_sale_active ON sales(is_active, start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_sale_product ON sales(product_id);
CREATE INDEX IF NOT EXISTS idx_sale_branch ON sales(branch_id);

-- Tạo indexes cho bảng stockings
CREATE INDEX IF NOT EXISTS idx_stocking_product_branch ON stockings(product_id, branch_id);
CREATE INDEX IF NOT EXISTS idx_stocking_available ON stockings(available_quantity, branch_id);
CREATE INDEX IF NOT EXISTS idx_stocking_product ON stockings(product_id);
CREATE INDEX IF NOT EXISTS idx_stocking_branch ON stockings(branch_id);

-- Tạo trigger để tự động tính available_quantity khi quantity hoặc reserved_quantity thay đổi
CREATE OR REPLACE FUNCTION update_available_quantity()
RETURNS TRIGGER AS $$
BEGIN
    NEW.available_quantity = GREATEST(0, NEW.quantity - NEW.reserved_quantity);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_available_quantity
    BEFORE INSERT OR UPDATE ON stockings
    FOR EACH ROW
    EXECUTE FUNCTION update_available_quantity();

-- Insert dữ liệu mẫu (tùy chọn)
-- INSERT INTO branches (name, code, address, phone, email) VALUES
-- ('Chi nhánh Hà Nội', 'CN001', '123 Đường ABC, Hà Nội', '0123456789', 'hn@example.com'),
-- ('Chi nhánh TP.HCM', 'CN002', '456 Đường XYZ, TP.HCM', '0987654321', 'hcm@example.com'),
-- ('Chi nhánh Đà Nẵng', 'CN003', '789 Đường DEF, Đà Nẵng', '0111222333', 'dn@example.com');

