const db = require('../config/db');

exports.getServices = async (req, res) => {
  try {
    if (db.isConnected) {
      const result = await db.query('SELECT * FROM services ORDER BY id ASC');
      return res.json({ success: true, data: result.rows });
    } else {
      return res.json({ success: true, data: db.memoryStore.services });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tải danh mục dịch vụ', error: error.message });
  }
};

exports.createService = async (req, res) => {
  try {
    const { name, unit, unit_price, is_metered } = req.body;
    if (!name || !unit || unit_price === undefined || unit_price < 0) {
      return res.status(400).json({ success: false, message: 'Tên dịch vụ, đơn vị và đơn giá (>= 0) là bắt buộc' });
    }

    const metered = Boolean(is_metered);

    if (db.isConnected) {
      const insert = await db.query(
        `INSERT INTO services (name, unit, unit_price, is_metered)
         VALUES ($1, $2, $3, $4) RETURNING *`,
        [name, unit, unit_price, metered]
      );
      return res.status(201).json({ success: true, message: 'Thêm dịch vụ thành công', data: insert.rows[0] });
    } else {
      const newService = {
        id: db.memoryStore.services.length > 0 ? Math.max(...db.memoryStore.services.map(s => s.id)) + 1 : 1,
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
    const id = parseInt(req.params.id, 10);
    const { name, unit, unit_price, is_metered } = req.body;

    if (db.isConnected) {
      const update = await db.query(
        `UPDATE services
         SET name = COALESCE($1, name),
             unit = COALESCE($2, unit),
             unit_price = COALESCE($3, unit_price),
             is_metered = COALESCE($4, is_metered)
         WHERE id = $5 RETURNING *`,
        [name, unit, unit_price, is_metered, id]
      );
      if (update.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy dịch vụ' });
      }
      return res.json({ success: true, message: 'Cập nhật dịch vụ thành công', data: update.rows[0] });
    } else {
      const service = db.memoryStore.services.find(s => s.id === id);
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
    const id = parseInt(req.params.id, 10);
    if (db.isConnected) {
      await db.query('DELETE FROM services WHERE id = $1', [id]);
      return res.json({ success: true, message: 'Xóa dịch vụ thành công' });
    } else {
      const index = db.memoryStore.services.findIndex(s => s.id === id);
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
