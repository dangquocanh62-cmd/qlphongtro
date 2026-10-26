-- ====================================================================
-- DỮ LIỆU MẪU (SEED DATA) CHO QUẢN LÝ NHÀ TRỌ
-- ====================================================================

-- 1. Thêm người dùng mẫu
-- Password hash cho '123456' (SHA256 hoặc bcrypt)
INSERT INTO users (email, password_hash, full_name, phone, role) VALUES
('admin@nhatro.vn', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5M8d/Xp58w3VfD9rW9100Z.9wG0OQvG', 'Nguyễn Văn Chủ', '0901234567', 'OWNER'),
('quanly@nhatro.vn', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5M8d/Xp58w3VfD9rW9100Z.9wG0OQvG', 'Trần Thị Quản Lý', '0912345678', 'MANAGER')
ON CONFLICT (email) DO NOTHING;

-- 2. Thêm phòng trọ
INSERT INTO rooms (room_code, floor, area, price, status, note) VALUES
('P101', 1, 22.5, 3000000, 'OCCUPIED', 'Phòng tầng 1, có gác lửng, tủ lạnh'),
('P102', 1, 20.0, 2800000, 'AVAILABLE', 'Phòng tầng 1, thoáng mát, gần cửa sổ'),
('P201', 2, 25.0, 3500000, 'OCCUPIED', 'Phòng tầng 2, ban công riêng, máy lạnh'),
('P202', 2, 25.0, 3500000, 'OCCUPIED', 'Phòng tầng 2, máy lạnh, máy giặt riêng'),
('P301', 3, 28.0, 3800000, 'AVAILABLE', 'Phòng tầng 3, ban công rộng, view đẹp'),
('P302', 3, 24.0, 3200000, 'MAINTENANCE', 'Đang sơn sửa và bảo dưỡng máy lạnh')
ON CONFLICT (room_code) DO NOTHING;

-- 3. Thêm khách thuê
INSERT INTO tenants (full_name, phone, id_card, birth_date, hometown) VALUES
('Lê Hoàng Long', '0987654321', '001200001234', '2000-05-15', 'Hà Nội'),
('Trần Mai Anh', '0978123456', '001201004567', '2001-09-20', 'Hải Phòng'),
('Phạm Quốc Đạt', '0934567890', '036200007890', '1999-11-10', 'Nam Định'),
('Đỗ Thị Hoa', '0965432109', '037201009876', '2002-03-25', 'Ninh Bình'),
('Vũ Minh Trí', '0943210987', '040200003456', '1998-07-08', 'Nghệ An')
ON CONFLICT (id_card) DO NOTHING;

-- 4. Thêm dịch vụ
INSERT INTO services (id, name, unit, unit_price, is_metered) VALUES
(1, 'Điện', 'kWh', 3500, TRUE),
(2, 'Nước sinh hoạt', 'm³', 25000, TRUE),
(3, 'Internet cáp quang', 'phòng', 100000, FALSE),
(4, 'Rác & Vệ sinh chung', 'phòng', 50000, FALSE),
(5, 'Gửi xe máy', 'người', 80000, FALSE)
ON CONFLICT (id) DO NOTHING;
SELECT setval('services_id_seq', (SELECT COALESCE(MAX(id), 1) FROM services));

-- 5. Thêm hợp đồng
-- Hợp đồng cho phòng P101 (id=1) với khách Lê Hoàng Long (id=1)
INSERT INTO contracts (id, room_id, tenant_id, start_date, end_date, deposit, rent_price, status) VALUES
(1, 1, 1, '2024-01-01', '2025-01-01', 3000000, 3000000, 'ACTIVE'),
(2, 3, 3, '2024-03-01', '2025-03-01', 3500000, 3500000, 'ACTIVE'),
(3, 4, 4, '2024-02-15', '2025-02-15', 3500000, 3500000, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
SELECT setval('contracts_id_seq', (SELECT COALESCE(MAX(id), 1) FROM contracts));

-- 6. Người ở cùng
-- Hợp đồng 1 có thêm người ở cùng là Trần Mai Anh (id=2)
INSERT INTO contract_tenants (contract_id, tenant_id) VALUES
(1, 2)
ON CONFLICT DO NOTHING;

-- 7. Chỉ số điện nước các tháng
-- P101: Tháng 9 và 10 năm 2024
INSERT INTO meter_readings (room_id, service_id, month, year, old_value, new_value) VALUES
(1, 1, 9, 2024, 120.0, 215.0),  -- Điện P101 T9: 95 kWh
(1, 2, 9, 2024, 45.0, 53.0),    -- Nước P101 T9: 8 m3
(1, 1, 10, 2024, 215.0, 320.0), -- Điện P101 T10: 105 kWh
(1, 2, 10, 2024, 53.0, 62.0),   -- Nước P101 T10: 9 m3
(3, 1, 10, 2024, 300.0, 420.0), -- Điện P201 T10: 120 kWh
(3, 2, 10, 2024, 60.0, 71.0)    -- Nước P201 T10: 11 m3
ON CONFLICT (room_id, service_id, month, year) DO NOTHING;

-- 8. Hóa đơn
-- Hóa đơn T10/2024 cho hợp đồng 1 (P101)
-- Tiền phòng: 3,000,000 + Điện: 105 * 3,500 = 367,500 + Nước: 9 * 25,000 = 225,000 + Mạng: 100,000 + Rác: 50,000 = 3,742,500
INSERT INTO invoices (id, contract_id, month, year, total_amount, status, due_date) VALUES
(1, 1, 10, 2024, 3742500, 'PARTIAL', '2024-11-05'),
(2, 2, 10, 2024, 4195000, 'UNPAID', '2024-11-05')
ON CONFLICT (contract_id, month, year) DO NOTHING;
SELECT setval('invoices_id_seq', (SELECT COALESCE(MAX(id), 1) FROM invoices));

-- 9. Chi tiết hóa đơn 1
INSERT INTO invoice_items (id, invoice_id, description, quantity, unit_price, amount) VALUES
(1, 1, 'Tiền thuê phòng tháng 10/2024', 1.0, 3000000, 3000000),
(2, 1, 'Tiền điện (Chỉ số: 215 -> 320)', 105.0, 3500, 367500),
(3, 1, 'Tiền nước (Chỉ số: 53 -> 62)', 9.0, 25000, 225000),
(4, 1, 'Internet cáp quang', 1.0, 100000, 100000),
(5, 1, 'Rác & Vệ sinh môi trường', 1.0, 50000, 50000),
(6, 2, 'Tiền thuê phòng tháng 10/2024', 1.0, 3500000, 3500000),
(7, 2, 'Tiền điện (Chỉ số: 300 -> 420)', 120.0, 3500, 420000),
(8, 2, 'Tiền nước (Chỉ số: 60 -> 71)', 11.0, 25000, 275000)
ON CONFLICT (id) DO NOTHING;
SELECT setval('invoice_items_id_seq', (SELECT COALESCE(MAX(id), 1) FROM invoice_items));

-- 10. Thanh toán
-- Khách phòng P101 đã thanh toán trước 2,000,000 VND chuyển khoản
INSERT INTO payments (id, invoice_id, amount, method, paid_at) VALUES
(1, 1, 2000000, 'TRANSFER', '2024-10-31 09:30:00+07')
ON CONFLICT (id) DO NOTHING;
SELECT setval('payments_id_seq', (SELECT COALESCE(MAX(id), 1) FROM payments));

