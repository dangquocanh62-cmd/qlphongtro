const { Client } = require('pg');
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '../.env') });

async function syncLocalToCloud() {
  const localClient = new Client({
    host: '127.0.0.1',
    port: 5432,
    database: 'qlphongtro',
    user: 'postgres',
    password: 'postgres'
  });

  const cloudClient = new Client({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: false }
  });

  try {
    console.log('[Sync] Đang kết nối tới Local PostgreSQL...');
    await localClient.connect();
    console.log('[Sync] Kết nối Local PostgreSQL thành công!');

    console.log('[Sync] Đang kết nối tới Neon Cloud PostgreSQL...');
    await cloudClient.connect();
    console.log('[Sync] Kết nối Cloud PostgreSQL thành công!');

    const tables = [
      'users',
      'rooms',
      'tenants',
      'services',
      'contracts',
      'contract_tenants',
      'meter_readings',
      'invoices',
      'invoice_items',
      'payments'
    ];

    // 1. Dọn dẹp dữ liệu cũ trên Cloud để chuẩn bị nạp bản sao đầy đủ
    console.log('[Sync] Đang dọn dẹp các bảng trên Cloud...');
    await cloudClient.query('TRUNCATE TABLE payments, invoice_items, invoices, meter_readings, contract_tenants, contracts, services, tenants, rooms, users CASCADE;');

    // 2. Chuyển từng bảng từ Local sang Cloud
    for (const table of tables) {
      const localData = await localClient.query(`SELECT * FROM ${table}`);
      console.log(`[Sync] Đang chuyển bảng ${table}: ${localData.rows.length} bản ghi...`);

      if (localData.rows.length > 0) {
        const columns = Object.keys(localData.rows[0]);
        const colNames = columns.join(', ');

        for (const row of localData.rows) {
          const values = columns.map(c => row[c]);
          const placeholders = columns.map((_, idx) => `$${idx + 1}`).join(', ');
          const queryText = `INSERT INTO ${table} (${colNames}) VALUES (${placeholders}) ON CONFLICT DO NOTHING;`;
          await cloudClient.query(queryText, values);
        }
      }

      // Cập nhật sequence nếu bảng có trường id
      if (table !== 'contract_tenants') {
        try {
          await cloudClient.query(`SELECT setval('${table}_id_seq', COALESCE((SELECT MAX(id) FROM ${table}), 1));`);
        } catch (_) {}
      }
    }

    console.log('==================================================');
    console.log('🎉 ĐỒNG BỘ TOÀN BỘ DỮ LIỆU TỪ MÁY NÀY LÊN CLOUD THÀNH CÔNG!');
    console.log('==================================================');
    
    // Thống kê kết quả
    for (const t of ['users', 'rooms', 'tenants', 'contracts', 'invoices', 'payments']) {
      const r = await cloudClient.query(`SELECT count(*) FROM ${t}`);
      console.log(`- ${t}: ${r.rows[0].count} bản ghi`);
    }

    await localClient.end();
    await cloudClient.end();
  } catch (error) {
    console.error('❌ Lỗi đồng bộ dữ liệu:', error);
    try { await localClient.end(); } catch (_) {}
    try { await cloudClient.end(); } catch (_) {}
    process.exit(1);
  }
}

syncLocalToCloud();
