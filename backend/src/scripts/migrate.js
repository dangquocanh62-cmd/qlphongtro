const fs = require('fs');
const path = require('path');
const { Pool } = require('pg');
require('dotenv').config({ path: path.join(__dirname, '../../.env') });

async function runMigration() {
  const pool = new Pool(
    process.env.DATABASE_URL
      ? {
          connectionString: process.env.DATABASE_URL,
          ssl: { rejectUnauthorized: false }
        }
      : {
          host: process.env.DB_HOST || '127.0.0.1',
          port: parseInt(process.env.DB_PORT || '5432', 10),
          database: process.env.DB_NAME || 'qlphongtro',
          user: process.env.DB_USER || 'postgres',
          password: process.env.DB_PASSWORD || 'postgres',
          ssl: process.env.DB_SSL === 'true' ? { rejectUnauthorized: false } : false
        }
  );

  try {
    const dbTarget = process.env.DATABASE_URL ? 'Cloud PostgreSQL' : (process.env.DB_NAME || 'qlphongtro');
    console.log('[Migration] Đang kết nối tới PostgreSQL database:', dbTarget);
    let client;
    try {
      client = await pool.connect();
    } catch (connErr) {
      if (!process.env.DATABASE_URL) {
        try {
          const { execSync } = require('child_process');
          execSync('wsl -d Ubuntu -u root service postgresql start', { stdio: 'ignore', timeout: 5000 });
          client = await pool.connect();
        } catch (_) {
          throw connErr;
        }
      } else {
        throw connErr;
      }
    }
    console.log('[Migration] Kết nối thành công! Đang thực thi schema.sql...');

    const localSchemaPath = path.join(__dirname, '../../database/schema.sql');
    const rootSchemaPath = path.join(__dirname, '../../../database/schema.sql');
    const schemaPath = fs.existsSync(localSchemaPath) ? localSchemaPath : rootSchemaPath;
    const schemaSql = fs.readFileSync(schemaPath, 'utf8');
    await client.query(schemaSql);
    console.log('[Migration] Tạo bảng & cấu trúc dữ liệu THÀNH CÔNG!');

    const localSeedPath = path.join(__dirname, '../../database/seed.sql');
    const rootSeedPath = path.join(__dirname, '../../../database/seed.sql');
    const seedPath = fs.existsSync(localSeedPath) ? localSeedPath : rootSeedPath;
    if (fs.existsSync(seedPath)) {
      console.log('[Migration] Đang nạp dữ liệu mẫu từ seed.sql...');
      const seedSql = fs.readFileSync(seedPath, 'utf8');
      await client.query(seedSql);
      console.log('[Migration] Nạp dữ liệu mẫu THÀNH CÔNG!');
    }

    client.release();
    await pool.end();
    console.log('[Migration] Quá trình khởi tạo cơ sở dữ liệu hoàn tất 100%!');
  } catch (err) {
    const errorDetails = (err.errors && err.errors.length)
      ? err.errors.map(e => e.message || e.code).join(', ')
      : (err.message || err.code || err);
    console.error(`[Migration] Lỗi khi chạy migration: ${errorDetails}`);
    if (err.code === 'ECONNREFUSED' || (err.errors && err.errors.some(e => e.code === 'ECONNREFUSED'))) {
      console.error(`[Migration] ⚠️  Nguyên nhân: Không tìm thấy PostgreSQL đang chạy tại ${process.env.DB_HOST || 'localhost'}:${process.env.DB_PORT || 5432} (ECONNREFUSED).`);
      console.error(`[Migration] 💡 Cách khắc phục:`);
      console.error(`   1. Khởi động PostgreSQL trong Windows Services (services.msc) hoặc khởi chạy PostgreSQL container nếu dùng Docker.`);
      console.error(`   2. Đảm bảo thông tin DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD trong file .env chính xác.`);
      console.error(`   3. Nếu bạn chỉ muốn test app hoặc chưa cài PostgreSQL: Bạn KHÔNG CẦN chạy migrate. Hãy chạy 'npm start' trực tiếp - backend sẽ tự động chuyển sang chế độ In-Memory Database có sẵn dữ liệu mẫu!`);
    }
    process.exit(1);
  }
}

runMigration();
