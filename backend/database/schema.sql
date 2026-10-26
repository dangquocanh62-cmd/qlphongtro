-- ====================================================================
-- HỆ THỐNG QUẢN LÝ NHÀ TRỌ (QLPhongTro)
-- Cơ sở dữ liệu: PostgreSQL
-- Kiến trúc: RESTful API Backend + Android MVVM Client (Java)
-- ====================================================================

-- 1. BẢNG USERS (Tài khoản người dùng/Chủ trọ/Quản lý)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    role VARCHAR(20) NOT NULL DEFAULT 'OWNER' CHECK (role IN ('OWNER', 'MANAGER')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. BẢNG ROOMS (Danh sách phòng trọ theo tài khoản chủ trọ)
CREATE TABLE IF NOT EXISTS rooms (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    room_code VARCHAR(20) NOT NULL,
    floor INT,
    area NUMERIC(6,2),
    price NUMERIC(12,0) NOT NULL CHECK (price >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE')),
    note TEXT,
    CONSTRAINT uq_rooms_user_room_code UNIQUE (user_id, room_code)
);

-- 3. BẢNG TENANTS (Danh sách khách thuê theo tài khoản chủ trọ)
CREATE TABLE IF NOT EXISTS tenants (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    id_card VARCHAR(20),
    birth_date DATE,
    hometown VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. BẢNG CONTRACTS (Hợp đồng thuê phòng)
CREATE TABLE IF NOT EXISTS contracts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    room_id BIGINT NOT NULL REFERENCES rooms(id) ON DELETE RESTRICT,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL,
    end_date DATE,
    deposit NUMERIC(12,0) NOT NULL DEFAULT 0 CHECK (deposit >= 0),
    rent_price NUMERIC(12,0) NOT NULL CHECK (rent_price >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ENDED', 'CANCELLED')),
    CONSTRAINT chk_contract_dates CHECK (end_date IS NULL OR end_date > start_date)
);

-- 5. BẢNG CONTRACT_TENANTS (Những người ở chung trong hợp đồng)
CREATE TABLE IF NOT EXISTS contract_tenants (
    contract_id BIGINT NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    PRIMARY KEY (contract_id, tenant_id)
);

-- 6. BẢNG SERVICES (Danh mục dịch vụ tính phí: Điện, Nước, Mạng, Rác...)
CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    unit VARCHAR(20) NOT NULL, -- kWh, m³, phòng, người
    unit_price NUMERIC(12,0) NOT NULL CHECK (unit_price >= 0),
    is_metered BOOLEAN NOT NULL DEFAULT FALSE
);

-- 7. BẢNG METER_READINGS (Ghi nhận chỉ số điện/nước hàng tháng theo phòng)
CREATE TABLE IF NOT EXISTS meter_readings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    room_id BIGINT NOT NULL REFERENCES rooms(id) ON DELETE RESTRICT,
    service_id BIGINT NOT NULL REFERENCES services(id) ON DELETE RESTRICT,
    month INT NOT NULL CHECK (month BETWEEN 1 AND 12),
    year INT NOT NULL CHECK (year >= 2000),
    old_value NUMERIC(10,2) NOT NULL CHECK (old_value >= 0),
    new_value NUMERIC(10,2) NOT NULL,
    CONSTRAINT chk_meter_values CHECK (new_value >= old_value),
    CONSTRAINT uq_room_service_month_year UNIQUE (room_id, service_id, month, year)
);

-- 8. BẢNG INVOICES (Hóa đơn tiền phòng & dịch vụ hàng tháng)
CREATE TABLE IF NOT EXISTS invoices (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    contract_id BIGINT NOT NULL REFERENCES contracts(id) ON DELETE RESTRICT,
    month INT NOT NULL CHECK (month BETWEEN 1 AND 12),
    year INT NOT NULL CHECK (year >= 2000),
    total_amount NUMERIC(12,0) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID' CHECK (status IN ('UNPAID', 'PARTIAL', 'PAID')),
    due_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_contract_month_year UNIQUE (contract_id, month, year)
);

-- 9. BẢNG INVOICE_ITEMS (Chi tiết các khoản mục trong hóa đơn)
CREATE TABLE IF NOT EXISTS invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(100) NOT NULL,
    quantity NUMERIC(10,2) NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price NUMERIC(12,0) NOT NULL CHECK (unit_price >= 0),
    amount NUMERIC(12,0) NOT NULL CHECK (amount >= 0)
);

-- 10. BẢNG PAYMENTS (Lịch sử thanh toán hóa đơn)
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    amount NUMERIC(12,0) NOT NULL CHECK (amount > 0),
    method VARCHAR(20) NOT NULL DEFAULT 'CASH' CHECK (method IN ('CASH', 'TRANSFER')),
    paid_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- TẠO CÁC CHỈ MỤC (INDEXES) TỐI ƯU HIỆU NĂNG TRUY VẤN
CREATE INDEX IF NOT EXISTS idx_rooms_user ON rooms(user_id);
CREATE INDEX IF NOT EXISTS idx_rooms_status ON rooms(status);
CREATE INDEX IF NOT EXISTS idx_tenants_user ON tenants(user_id);
CREATE INDEX IF NOT EXISTS idx_contracts_user ON contracts(user_id);
CREATE INDEX IF NOT EXISTS idx_contracts_room ON contracts(room_id);
CREATE INDEX IF NOT EXISTS idx_contracts_tenant ON contracts(tenant_id);
CREATE INDEX IF NOT EXISTS idx_contracts_status ON contracts(status);
CREATE INDEX IF NOT EXISTS idx_services_user ON services(user_id);
CREATE INDEX IF NOT EXISTS idx_meter_room_period ON meter_readings(room_id, year, month);
CREATE INDEX IF NOT EXISTS idx_invoices_user ON invoices(user_id);
CREATE INDEX IF NOT EXISTS idx_invoices_contract ON invoices(contract_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoices_period ON invoices(year, month);
CREATE INDEX IF NOT EXISTS idx_invoice_items_invoice ON invoice_items(invoice_id);
CREATE INDEX IF NOT EXISTS idx_payments_invoice ON payments(invoice_id);

-- TRIGGER TỰ ĐỘNG CẬP NHẬT TRẠNG THÁI PHÒNG KHI CÓ HỢP ĐỒNG ACTIVE
CREATE OR REPLACE FUNCTION update_room_status_on_contract()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        IF NEW.status = 'ACTIVE' THEN
            UPDATE rooms SET status = 'OCCUPIED' WHERE id = NEW.room_id;
        ELSIF NEW.status IN ('ENDED', 'CANCELLED') THEN
            -- Kiểm tra nếu không còn hợp đồng active nào khác cho phòng này
            IF NOT EXISTS (
                SELECT 1 FROM contracts 
                WHERE room_id = NEW.room_id AND status = 'ACTIVE' AND id != NEW.id
            ) THEN
                UPDATE rooms SET status = 'AVAILABLE' WHERE id = NEW.room_id;
            END IF;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_contract_room_status ON contracts;
CREATE TRIGGER trg_contract_room_status
AFTER INSERT OR UPDATE OF status, room_id ON contracts
FOR EACH ROW
EXECUTE FUNCTION update_room_status_on_contract();
