const { Client } = require('pg');
const fs = require('fs');
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '../.env') });

async function setup() {
  const dbUrl = process.env.DATABASE_URL;
  if (!dbUrl) {
    console.error('Không tìm thấy DATABASE_URL trong .env');
    process.exit(1);
  }

  console.log('[Setup] Đang kết nối tới Neon Cloud PostgreSQL...');
  const client = new Client({
    connectionString: dbUrl,
    ssl: { rejectUnauthorized: false }
  });

  try {
    await client.connect();
    console.log('[Setup] Kết nối Cloud Database THÀNH CÔNG!');

    // 1. Chạy schema.sql
    const schemaPath = path.join(__dirname, '../../database/schema.sql');
    if (fs.existsSync(schemaPath)) {
      console.log('[Setup] Đang khởi tạo bảng cấu trúc từ schema.sql...');
      const schemaSql = fs.readFileSync(schemaPath, 'utf8');
      await client.query(schemaSql);
      console.log('[Setup] Khởi tạo cấu trúc bảng THÀNH CÔNG!');
    }

    // 2. Chạy seed.sql
    const seedPath = path.join(__dirname, '../database/seed.sql');
    if (fs.existsSync(seedPath)) {
      console.log('[Setup] Đang nạp dữ liệu mẫu ban đầu từ seed.sql...');
      const seedSql = fs.readFileSync(seedPath, 'utf8');
      await client.query(seedSql);
      console.log('[Setup] Nạp dữ liệu mẫu THÀNH CÔNG!');
    }

    // 3. Kiểm tra số lượng bản ghi
    const userRes = await client.query('SELECT count(*) FROM users');
    const roomRes = await client.query('SELECT count(*) FROM rooms');
    const tenantRes = await client.query('SELECT count(*) FROM tenants');
    const invoiceRes = await client.query('SELECT count(*) FROM invoices');

    console.log('==============================================');
    console.log('📊 THỐNG KÊ DỮ LIỆU CLOUD DATABASE:');
    console.log(`- Tài khoản (users): ${userRes.rows[0].count}`);
    console.log(`- Phòng trọ (rooms): ${roomRes.rows[0].count}`);
    console.log(`- Khách thuê (tenants): ${tenantRes.rows[0].count}`);
    console.log(`- Hóa đơn (invoices): ${invoiceRes.rows[0].count}`);
    console.log('==============================================');

    await client.end();
    console.log('✅ HOÀN TẤT CẤU HÌNH CLOUD DATABASE!');
  } catch (err) {
    console.error('❌ Lỗi thiết lập Cloud Database:', err);
    try { await client.end(); } catch (_) {}
    process.exit(1);
  }
}

setup();
