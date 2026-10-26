const express = require('express');
const cors = require('cors');
require('dotenv').config();

const db = require('./config/db');
const apiRoutes = require('./routes/api');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Request logger
app.use((req, res, next) => {
  console.log(`[${new Date().toLocaleTimeString()}] ${req.method} ${req.url}`);
  next();
});

// Root welcome route
app.get('/', (req, res) => {
  res.json({
    message: 'Chào mừng đến với API Quản lý nhà trọ (QLPhongTro RESTful API)',
    version: '1.0.0',
    database_status: db.isConnected ? 'Connected to PostgreSQL' : 'Running In-Memory Mode with Seed Data',
    endpoints: {
      auth: '/api/auth/login',
      dashboard: '/api/dashboard/stats',
      rooms: '/api/rooms',
      tenants: '/api/tenants',
      contracts: '/api/contracts',
      services: '/api/services',
      meter_readings: '/api/meter-readings',
      invoices: '/api/invoices',
      payments: '/api/payments'
    }
  });
});

// Mount API routes
app.use('/api', apiRoutes);

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Unhandled Server Error:', err);
  res.status(500).json({
    success: false,
    message: 'Đã xảy ra lỗi nội bộ trên máy chủ',
    error: process.env.NODE_ENV === 'development' ? err.message : undefined
  });
});

// Start server and initialize DB connection
async function start() {
  await db.initDb();
  app.listen(PORT, '0.0.0.0', () => {
    console.log(`=======================================================`);
    console.log(`🚀 RESTful API Server đang chạy tại: http://localhost:${PORT}`);
    console.log(`📱 Địa chỉ kết nối từ Android Emulator: http://10.0.2.2:${PORT}`);
    console.log(`⚙️  Chế độ: ${process.env.NODE_ENV || 'development'}`);
    console.log(`=======================================================`);
  });
}

start();
