-- ====================================================================
-- DỮ LIỆU MẪU (SEED DATA) CHO QUẢN LÝ NHÀ TRỌ (MULTI-TENANT USER_ID = 1)
-- ====================================================================

-- 1. Thêm người dùng mẫu
INSERT INTO users (id, email, password_hash, full_name, phone, role) VALUES
(1, 'admin@nhatro.vn', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5M8d/Xp58w3VfD9rW9100Z.9wG0OQvG', 'Nguyễn Văn Chủ', '0901234567', 'OWNER'),
(2, 'quanly@nhatro.vn', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5M8d/Xp58w3VfD9rW9100Z.9wG0OQvG', 'Trần Thị Quản Lý', '0912345678', 'MANAGER')
ON CONFLICT (email) DO NOTHING;
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM users));

-- 2. Thêm phòng trọ (gắn với user_id = 1)
INSERT INTO rooms (id, user_id, room_code, floor, area, price, status, note) VALUES
(1, 1, 'P101', 1, 22.5, 3000000, 'OCCUPIED', 'Phòng tầng 1, có gác lửng, tủ lạnh'),
(2, 1, 'P102', 1, 20.0, 2800000, 'AVAILABLE', 'Phòng tầng 1, thoáng mát, gần cửa sổ'),
(3, 1, 'P201', 2, 25.0, 3500000, 'OCCUPIED', 'Phòng tầng 2, ban công riêng, máy lạnh'),
(4, 1, 'P202', 2, 25.0, 3500000, 'OCCUPIED', 'Phòng tầng 2, máy lạnh, máy giặt riêng'),
(5, 1, 'P301', 3, 28.0, 3800000, 'AVAILABLE', 'Phòng tầng 3, ban công rộng, view đẹp'),
(6, 1, 'P302', 3, 24.0, 3200000, 'MAINTENANCE', 'Đang sơn sửa và bảo dưỡng máy lạnh')
ON CONFLICT (id) DO NOTHING;
SELECT setval('rooms_id_seq', (SELECT COALESCE(MAX(id), 1) FROM rooms));

-- 3. Thêm khách thuê (gắn với user_id = 1)
INSERT INTO tenants (id, user_id, full_name, phone, id_card, birth_date, hometown) VALUES
(1, 1, 'Lê Hoàng Long', '0987654321', '001200001234', '2000-05-15', 'Hà Nội'),
(2, 1, 'Trần Mai Anh', '0978123456', '001201004567', '2001-09-20', 'Hải Phòng'),
(3, 1, 'Phạm Quốc Đạt', '0934567890', '036200007890', '1999-11-10', 'Nam Định'),
(4, 1, 'Đỗ Thị Hoa', '0965432109', '037201009876', '2002-03-25', 'Ninh Bình'),
(5, 1, 'Vũ Minh Trí', '0943210987', '040200003456', '1998-07-08', 'Nghệ An')
ON CONFLICT (id) DO NOTHING;
SELECT setval('tenants_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tenants));

-- 4. Thêm dịch vụ (gắn với user_id = 1)
INSERT INTO services (id, user_id, name, unit, unit_price, is_metered) VALUES
(1, 1, 'Điện', 'kWh', 3500, TRUE),
(2, 1, 'Nước sinh hoạt', 'm³', 25000, TRUE),
(3, 1, 'Internet cáp quang', 'phòng', 100000, FALSE),
(4, 1, 'Rác & Vệ sinh chung', 'phòng', 50000, FALSE),
(5, 1, 'Gửi xe máy', 'người', 80000, FALSE)
ON CONFLICT (id) DO NOTHING;
SELECT setval('services_id_seq', (SELECT COALESCE(MAX(id), 1) FROM services));

-- 5. Thêm hợp đồng (gắn với user_id = 1)
INSERT INTO contracts (id, user_id, room_id, tenant_id, start_date, end_date, deposit, rent_price, status) VALUES
(1, 1, 1, 1, '2024-01-01', '2025-01-01', 3000000, 3000000, 'ACTIVE'),
(2, 1, 3, 3, '2024-03-01', '2025-03-01', 3500000, 3500000, 'ACTIVE'),
(3, 1, 4, 4, '2024-02-15', '2025-02-15', 3500000, 3500000, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
SELECT setval('contracts_id_seq', (SELECT COALESCE(MAX(id), 1) FROM contracts));

-- 6. Người ở cùng
INSERT INTO contract_tenants (contract_id, tenant_id) VALUES
(1, 2)
ON CONFLICT DO NOTHING;

-- 7. Chỉ số điện nước (gắn với user_id = 1)
INSERT INTO meter_readings (id, user_id, room_id, service_id, month, year, old_value, new_value) VALUES
(1, 1, 1, 1, 9, 2024, 120.0, 215.0),
(2, 1, 1, 2, 9, 2024, 45.0, 53.0),
(3, 1, 1, 1, 10, 2024, 215.0, 320.0),
(4, 1, 1, 2, 10, 2024, 53.0, 62.0),
(5, 1, 3, 1, 10, 2024, 300.0, 420.0),
(6, 1, 3, 2, 10, 2024, 60.0, 71.0)
ON CONFLICT (id) DO NOTHING;
SELECT setval('meter_readings_id_seq', (SELECT COALESCE(MAX(id), 1) FROM meter_readings));

-- 8. Hóa đơn (gắn với user_id = 1)
INSERT INTO invoices (id, user_id, contract_id, month, year, total_amount, status, due_date) VALUES
(1, 1, 1, 10, 2024, 3742500, 'PARTIAL', '2024-11-05'),
(2, 1, 2, 10, 2024, 4195000, 'UNPAID', '2024-11-05')
ON CONFLICT (id) DO NOTHING;
SELECT setval('invoices_id_seq', (SELECT COALESCE(MAX(id), 1) FROM invoices));

-- 9. Chi tiết hóa đơn
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
INSERT INTO payments (id, invoice_id, amount, method, paid_at) VALUES
(1, 1, 2000000, 'TRANSFER', '2024-10-31 09:30:00+07')
ON CONFLICT (id) DO NOTHING;
SELECT setval('payments_id_seq', (SELECT COALESCE(MAX(id), 1) FROM payments));
