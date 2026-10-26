const jwt = require('jsonwebtoken');
const db = require('../config/db');

const JWT_SECRET = process.env.JWT_SECRET || 'phongtro_secret_key_2024';

exports.login = async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ success: false, message: 'Vui lòng cung cấp email và mật khẩu' });
    }

    let user = null;
    if (db.isConnected) {
      const result = await db.query('SELECT * FROM users WHERE email = $1', [email]);
      if (result.rows.length > 0) user = result.rows[0];
    } else {
      user = db.memoryStore.users.find(u => u.email === email);
    }

    if (!user) {
      return res.status(401).json({ success: false, message: 'Email hoặc mật khẩu không chính xác' });
    }

    // Demo password check (accepts '123456' or matching hash)
    const isPasswordValid = (password === '123456' || password === user.password_hash);
    if (!isPasswordValid) {
      return res.status(401).json({ success: false, message: 'Email hoặc mật khẩu không chính xác' });
    }

    const token = jwt.sign(
      { id: user.id, email: user.email, role: user.role, full_name: user.full_name },
      JWT_SECRET,
      { expiresIn: '7d' }
    );

    const { password_hash, ...userProfile } = user;
    return res.json({
      success: true,
      message: 'Đăng nhập thành công',
      data: {
        token,
        user: userProfile
      }
    });
  } catch (error) {
    console.error('Login error:', error);
    return res.status(500).json({ success: false, message: 'Lỗi máy chủ khi đăng nhập', error: error.message });
  }
};

exports.register = async (req, res) => {
  try {
    const { email, password, full_name, phone, role } = req.body;
    if (!email || !password || !full_name) {
      return res.status(400).json({ success: false, message: 'Vui lòng điền đủ email, mật khẩu và họ tên' });
    }

    const userRole = role === 'MANAGER' ? 'MANAGER' : 'OWNER';

    const defaultServices = [
      { name: 'Điện', unit: 'kWh', unit_price: 3500, is_metered: true },
      { name: 'Nước', unit: 'm³', unit_price: 25000, is_metered: true },
      { name: 'Internet cáp quang', unit: 'phòng', unit_price: 100000, is_metered: false },
      { name: 'Rác & Vệ sinh chung', unit: 'phòng', unit_price: 50000, is_metered: false },
      { name: 'Gửi xe máy', unit: 'người', unit_price: 80000, is_metered: false }
    ];

    let createdUser = null;

    if (db.isConnected) {
      const existing = await db.query('SELECT id FROM users WHERE email = $1', [email]);
      if (existing.rows.length > 0) {
        return res.status(400).json({ success: false, message: 'Email đã được sử dụng' });
      }
      const insert = await db.query(
        `INSERT INTO users (email, password_hash, full_name, phone, role) 
         VALUES ($1, $2, $3, $4, $5) RETURNING id, email, full_name, phone, role, created_at`,
        [email, password, full_name, phone || null, userRole]
      );
      createdUser = insert.rows[0];

      // Auto-seed default services for this new user
      for (const s of defaultServices) {
        await db.query(
          `INSERT INTO services (user_id, name, unit, unit_price, is_metered)
           VALUES ($1, $2, $3, $4, $5)`,
          [createdUser.id, s.name, s.unit, s.unit_price, s.is_metered]
        );
      }
    } else {
      const existing = db.memoryStore.users.find(u => u.email === email);
      if (existing) {
        return res.status(400).json({ success: false, message: 'Email đã được sử dụng' });
      }
      const newUserId = db.memoryStore.users.length > 0 ? Math.max(...db.memoryStore.users.map(u => u.id)) + 1 : 1;
      const newUser = {
        id: newUserId,
        email,
        password_hash: password,
        full_name,
        phone: phone || '',
        role: userRole,
        created_at: new Date().toISOString()
      };
      db.memoryStore.users.push(newUser);

      // Auto-seed default services in memoryStore
      let nextServiceId = db.memoryStore.services.length > 0 ? Math.max(...db.memoryStore.services.map(s => s.id)) + 1 : 1;
      for (const s of defaultServices) {
        db.memoryStore.services.push({
          id: nextServiceId++,
          user_id: newUserId,
          ...s
        });
      }

      const { password_hash, ...profile } = newUser;
      createdUser = profile;
    }

    const token = jwt.sign(
      { id: createdUser.id, email: createdUser.email, role: createdUser.role, full_name: createdUser.full_name },
      JWT_SECRET,
      { expiresIn: '7d' }
    );

    return res.status(201).json({
      success: true,
      message: 'Tạo tài khoản thành công',
      data: {
        token,
        user: createdUser
      }
    });
  } catch (error) {
    console.error('Register error:', error);
    return res.status(500).json({ success: false, message: 'Lỗi khi đăng ký', error: error.message });
  }
};

exports.getProfile = (req, res) => {
  return res.json({ success: true, data: req.user });
};
