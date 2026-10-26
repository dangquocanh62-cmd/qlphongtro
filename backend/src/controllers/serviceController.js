const db = require('../config/db');

exports.getServices = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    if (db.isConnected) {
      const result = await db.query('SELECT * FROM services WHERE user_id = $1 ORDER BY id ASC', [userId]);
      return res.json({ success: true, data: result.rows });
    } else {
      const services = db.memoryStore.services.filter(s => (s.user_id || 1) === userId);
      return res.json({ success: true, data: services });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tải danh mục dịch vụ', error: error.message });
  }
};

exports.createService = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { name, unit, unit_price, is_metered } = req.body;
    if (!name || !unit || unit_price === undefined || unit_price < 0) {
      return res.status(400).json({ success: false, message: 'Tên dịch vụ, đơn vị và đơn giá (>= 0) là bắt buộc' });
    }

    const metered = Boolean(is_metered);

    if (db.isConnected) {
      const insert = await db.query(
        `INSERT INTO services (user_id, name, unit, unit_price, is_metered)
         VALUES ($1, $2, $3, $4, $5) RETURNING *`,
        [userId, name, unit, unit_price, metered]
      );
      return res.status(201).json({ success: true, message: 'Thêm dịch vụ thành công', data: insert.rows[0] });
    } else {
      const newService = {
        id: db.memoryStore.services.length > 0 ? Math.max(...db.memoryStore.services.map(s => s.id)) + 1 : 1,
        user_id: userId,
        name,
        unit,
        unit_price: Number(unit_price),
        is_metered: metered
      };
      db.memoryStore.services.push(newService);
      return res.status(201).json({ success: true, message: 'Thêm dịch vụ thành công', data: newService });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tạo dịch vụ', error: error.message });
  }
};

exports.updateService = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    const { name, unit, unit_price, is_metered } = req.body;

    if (db.isConnected) {
      const update = await db.query(
        `UPDATE services
         SET name = COALESCE($1, name),
             unit = COALESCE($2, unit),
             unit_price = COALESCE($3, unit_price),
             is_metered = COALESCE($4, is_metered)
         WHERE id = $5 AND user_id = $6 RETURNING *`,
        [name, unit, unit_price, is_metered, id, userId]
      );
      if (update.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy dịch vụ' });
      }
      return res.json({ success: true, message: 'Cập nhật dịch vụ thành công', data: update.rows[0] });
    } else {
      const service = db.memoryStore.services.find(s => s.id === id && (s.user_id || 1) === userId);
      if (!service) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy dịch vụ' });
      }
      if (name !== undefined) service.name = name;
      if (unit !== undefined) service.unit = unit;
      if (unit_price !== undefined) service.unit_price = Number(unit_price);
      if (is_metered !== undefined) service.is_metered = Boolean(is_metered);
      return res.json({ success: true, message: 'Cập nhật dịch vụ thành công', data: service });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi cập nhật dịch vụ', error: error.message });
  }
};

exports.deleteService = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    if (db.isConnected) {
      const result = await db.query('DELETE FROM services WHERE id = $1 AND user_id = $2', [id, userId]);
      if (result.rowCount === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy dịch vụ' });
      }
      return res.json({ success: true, message: 'Xóa dịch vụ thành công' });
    } else {
      const index = db.memoryStore.services.findIndex(s => s.id === id && (s.user_id || 1) === userId);
      if (index === -1) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy dịch vụ' });
      }
      db.memoryStore.services.splice(index, 1);
      return res.json({ success: true, message: 'Xóa dịch vụ thành công' });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Không thể xóa dịch vụ đã được ghi nhận chỉ số', error: error.message });
  }
};
