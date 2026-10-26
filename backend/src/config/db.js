const { Pool } = require('pg');
require('dotenv').config();

let isPgConnected = false;
let pool = null;

const pgConfig = process.env.DATABASE_URL
  ? {
      connectionString: process.env.DATABASE_URL,
      ssl: { rejectUnauthorized: false },
      connectionTimeoutMillis: 10000
    }
  : {
      host: process.env.DB_HOST || '127.0.0.1',
      port: parseInt(process.env.DB_PORT || '5432', 10),
      database: process.env.DB_NAME || 'qlphongtro',
      user: process.env.DB_USER || 'postgres',
      password: process.env.DB_PASSWORD || 'postgres',
      ssl: process.env.DB_SSL === 'true' ? { rejectUnauthorized: false } : false,
      connectionTimeoutMillis: 3000
    };

// In-memory data store for standalone/mock mode when PostgreSQL is offline
const memoryStore = {
  users: [
    { id: 1, email: 'admin@nhatro.vn', password_hash: '123456', full_name: 'Nguyễn Văn Chủ', phone: '0901234567', role: 'OWNER', created_at: new Date().toISOString() },
    { id: 2, email: 'quanly@nhatro.vn', password_hash: '123456', full_name: 'Trần Thị Quản Lý', phone: '0912345678', role: 'MANAGER', created_at: new Date().toISOString() }
  ],
  rooms: [
    { id: 1, room_code: 'P101', floor: 1, area: 22.5, price: 3000000, status: 'OCCUPIED', note: 'Phòng tầng 1, có gác lửng, tủ lạnh' },
    { id: 2, room_code: 'P102', floor: 1, area: 20.0, price: 2800000, status: 'AVAILABLE', note: 'Phòng tầng 1, thoáng mát, gần cửa sổ' },
    { id: 3, room_code: 'P201', floor: 2, area: 25.0, price: 3500000, status: 'OCCUPIED', note: 'Phòng tầng 2, ban công riêng, máy lạnh' },
    { id: 4, room_code: 'P202', floor: 2, area: 25.0, price: 3500000, status: 'OCCUPIED', note: 'Phòng tầng 2, máy lạnh, máy giặt riêng' },
    { id: 5, room_code: 'P301', floor: 3, area: 28.0, price: 3800000, status: 'AVAILABLE', note: 'Phòng tầng 3, ban công rộng, view đẹp' },
    { id: 6, room_code: 'P302', floor: 3, area: 24.0, price: 3200000, status: 'MAINTENANCE', note: 'Đang bảo dưỡng điều hòa' }
  ],
  tenants: [
    { id: 1, full_name: 'Lê Hoàng Long', phone: '0987654321', id_card: '001200001234', birth_date: '2000-05-15', hometown: 'Hà Nội', created_at: new Date().toISOString() },
    { id: 2, full_name: 'Trần Mai Anh', phone: '0978123456', id_card: '001201004567', birth_date: '2001-09-20', hometown: 'Hải Phòng', created_at: new Date().toISOString() },
    { id: 3, full_name: 'Phạm Quốc Đạt', phone: '0934567890', id_card: '036200007890', birth_date: '1999-11-10', hometown: 'Nam Định', created_at: new Date().toISOString() },
    { id: 4, full_name: 'Đỗ Thị Hoa', phone: '0965432109', id_card: '037201009876', birth_date: '2002-03-25', hometown: 'Ninh Bình', created_at: new Date().toISOString() },
    { id: 5, full_name: 'Vũ Minh Trí', phone: '0943210987', id_card: '040200003456', birth_date: '1998-07-08', hometown: 'Nghệ An', created_at: new Date().toISOString() }
  ],
  contracts: [
    { id: 1, room_id: 1, tenant_id: 1, start_date: '2024-01-01', end_date: '2025-01-01', deposit: 3000000, rent_price: 3000000, status: 'ACTIVE' },
    { id: 2, room_id: 3, tenant_id: 3, start_date: '2024-03-01', end_date: '2025-03-01', deposit: 3500000, rent_price: 3500000, status: 'ACTIVE' },
    { id: 3, room_id: 4, tenant_id: 4, start_date: '2024-02-15', end_date: '2025-02-15', deposit: 3500000, rent_price: 3500000, status: 'ACTIVE' }
  ],
  contract_tenants: [
    { contract_id: 1, tenant_id: 2 }
  ],
  services: [
    { id: 1, name: 'Điện', unit: 'kWh', unit_price: 3500, is_metered: true },
    { id: 2, name: 'Nước', unit: 'm³', unit_price: 25000, is_metered: true },
    { id: 3, name: 'Internet cáp quang', unit: 'phòng', unit_price: 100000, is_metered: false },
    { id: 4, name: 'Rác & Vệ sinh chung', unit: 'phòng', unit_price: 50000, is_metered: false },
    { id: 5, name: 'Gửi xe máy', unit: 'người', unit_price: 80000, is_metered: false }
  ],
  meter_readings: [
    { id: 1, room_id: 1, service_id: 1, month: 9, year: 2024, old_value: 120.0, new_value: 215.0 },
    { id: 2, room_id: 1, service_id: 2, month: 9, year: 2024, old_value: 45.0, new_value: 53.0 },
    { id: 3, room_id: 1, service_id: 1, month: 10, year: 2024, old_value: 215.0, new_value: 320.0 },
    { id: 4, room_id: 1, service_id: 2, month: 10, year: 2024, old_value: 53.0, new_value: 62.0 },
    { id: 5, room_id: 3, service_id: 1, month: 10, year: 2024, old_value: 300.0, new_value: 420.0 },
    { id: 6, room_id: 3, service_id: 2, month: 10, year: 2024, old_value: 60.0, new_value: 71.0 }
  ],
  invoices: [
    { id: 1, contract_id: 1, month: 10, year: 2024, total_amount: 3742500, status: 'PARTIAL', due_date: '2024-11-05', created_at: new Date().toISOString() },
    { id: 2, contract_id: 2, month: 10, year: 2024, total_amount: 4195000, status: 'UNPAID', due_date: '2024-11-05', created_at: new Date().toISOString() }
  ],
  invoice_items: [
    { id: 1, invoice_id: 1, description: 'Tiền thuê phòng tháng 10/2024', quantity: 1, unit_price: 3000000, amount: 3000000 },
    { id: 2, invoice_id: 1, description: 'Tiền điện (Chỉ số: 215 -> 320: 105 kWh)', quantity: 105, unit_price: 3500, amount: 367500 },
    { id: 3, invoice_id: 1, description: 'Tiền nước (Chỉ số: 53 -> 62: 9 m³)', quantity: 9, unit_price: 25000, amount: 225000 },
    { id: 4, invoice_id: 1, description: 'Internet cáp quang', quantity: 1, unit_price: 100000, amount: 100000 },
    { id: 5, invoice_id: 1, description: 'Rác & Vệ sinh môi trường', quantity: 1, unit_price: 50000, amount: 50000 },
    { id: 6, invoice_id: 2, description: 'Tiền thuê phòng tháng 10/2024', quantity: 1, unit_price: 3500000, amount: 3500000 },
    { id: 7, invoice_id: 2, description: 'Tiền điện (Chỉ số: 300 -> 420: 120 kWh)', quantity: 120, unit_price: 3500, amount: 420000 },
    { id: 8, invoice_id: 2, description: 'Tiền nước (Chỉ số: 60 -> 71: 11 m³)', quantity: 11, unit_price: 25000, amount: 275000 }
  ],
  payments: [
    { id: 1, invoice_id: 1, amount: 2000000, method: 'TRANSFER', paid_at: '2024-10-31T09:30:00.000Z' }
  ]
};

async function initDb() {
  try {
    pool = new Pool(pgConfig);
    const client = await pool.connect();
    const res = await client.query('SELECT NOW()');
    client.release();
    isPgConnected = true;
    console.log('[Database] Kết nối PostgreSQL THÀNH CÔNG tại:', res.rows[0].now);
  } catch (err) {
    if (!process.env.DATABASE_URL) {
      // Tự động thử kích hoạt dịch vụ PostgreSQL trong WSL nếu đang tắt trên máy local
      try {
        const { execSync } = require('child_process');
        execSync('wsl -d Ubuntu -u root service postgresql start', { stdio: 'ignore', timeout: 5000 });
        pool = new Pool(pgConfig);
        const retryClient = await pool.connect();
        const res = await retryClient.query('SELECT NOW()');
        retryClient.release();
        isPgConnected = true;
        console.log('[Database] Đã tự động kích hoạt PostgreSQL trên WSL và kết nối THÀNH CÔNG tại:', res.rows[0].now);
        return;
      } catch (_) {
        // Nếu không bật được thì chuyển sang In-Memory mode
      }
    }

    isPgConnected = false;
    const errorDetails = (err.errors && err.errors.length)
      ? err.errors.map(e => e.message || e.code).join(', ')
      : (err.message || err.code || 'ECONNREFUSED');
    console.warn(`[Database] Không thể kết nối trực tiếp đến PostgreSQL (${errorDetails}).`);
    console.log('[Database] Chuyển sang chế độ In-Memory Database đã nạp sẵn Seed Data đầy đủ 10 bảng!');
  }
}

async function query(text, params) {
  if (isPgConnected && pool) {
    return pool.query(text, params);
  }
  return null;
}

module.exports = {
  initDb,
  query,
  get isConnected() { return isPgConnected; },
  getPool: () => pool,
  memoryStore
};
