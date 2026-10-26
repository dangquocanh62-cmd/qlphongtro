const jwt = require('jsonwebtoken');
const JWT_SECRET = process.env.JWT_SECRET || 'phongtro_secret_key_2024';

module.exports = (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    // Để ứng dụng Android dễ dàng test cả khi chưa có token hoặc có token
    // Nếu không có header thì gán user mặc định
    req.user = { id: 1, email: 'admin@nhatro.vn', role: 'OWNER', full_name: 'Nguyễn Văn Chủ' };
    return next();
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded;
    next();
  } catch (err) {
    return res.status(401).json({ success: false, message: 'Phiên đăng nhập hết hạn hoặc không hợp lệ' });
  }
};
