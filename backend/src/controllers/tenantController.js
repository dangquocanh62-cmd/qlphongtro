const db = require('../config/db');

exports.getTenants = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { q } = req.query;

    if (db.isConnected) {
      let query = 'SELECT * FROM tenants WHERE user_id = $1';
      const params = [userId];
      if (q) {
        params.push(`%${q}%`);
        query += ` AND (full_name ILIKE $${params.length} OR phone ILIKE $${params.length} OR id_card ILIKE $${params.length} OR hometown ILIKE $${params.length})`;
      }
      query += ' ORDER BY created_at DESC';
      const result = await db.query(query, params);
      return res.json({ success: true, data: result.rows });
    } else {
      let tenants = db.memoryStore.tenants.filter(t => (t.user_id || 1) === userId);
      if (q) {
        const queryLower = q.toLowerCase();
        tenants = tenants.filter(t => 
          t.full_name.toLowerCase().includes(queryLower) ||
          (t.phone && t.phone.includes(queryLower)) ||
          (t.id_card && t.id_card.includes(queryLower)) ||
          (t.hometown && t.hometown.toLowerCase().includes(queryLower))
        );
      }
      return res.json({ success: true, data: tenants });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tải danh sách khách thuê', error: error.message });
  }
};

exports.getTenantById = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    if (db.isConnected) {
      const result = await db.query('SELECT * FROM tenants WHERE id = $1 AND user_id = $2', [id, userId]);
      if (result.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy khách thuê' });
      }
      return res.json({ success: true, data: result.rows[0] });
    } else {
      const tenant = db.memoryStore.tenants.find(t => t.id === id && (t.user_id || 1) === userId);
      if (!tenant) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy khách thuê' });
      }
      return res.json({ success: true, data: tenant });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tra cứu khách thuê', error: error.message });
  }
};

exports.createTenant = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { full_name, phone, id_card, birth_date, hometown } = req.body;
    if (!full_name) {
      return res.status(400).json({ success: false, message: 'Họ tên khách thuê là bắt buộc' });
    }

    if (db.isConnected) {
      if (id_card) {
        const existing = await db.query('SELECT id FROM tenants WHERE id_card = $1 AND user_id = $2', [id_card, userId]);
        if (existing.rows.length > 0) {
          return res.status(400).json({ success: false, message: `Số CCCD ${id_card} đã tồn tại trong danh sách của bạn` });
        }
      }
      const insert = await db.query(
        `INSERT INTO tenants (user_id, full_name, phone, id_card, birth_date, hometown)
         VALUES ($1, $2, $3, $4, $5, $6) RETURNING *`,
        [userId, full_name, phone || null, id_card || null, birth_date || null, hometown || null]
      );
      return res.status(201).json({ success: true, message: 'Thêm khách thuê thành công', data: insert.rows[0] });
    } else {
      if (id_card && db.memoryStore.tenants.some(t => (t.user_id || 1) === userId && t.id_card === id_card)) {
        return res.status(400).json({ success: false, message: `Số CCCD ${id_card} đã tồn tại trong danh sách của bạn` });
      }
      const newTenant = {
        id: db.memoryStore.tenants.length > 0 ? Math.max(...db.memoryStore.tenants.map(t => t.id)) + 1 : 1,
        user_id: userId,
        full_name,
        phone: phone || '',
        id_card: id_card || '',
        birth_date: birth_date || null,
        hometown: hometown || '',
        created_at: new Date().toISOString()
      };
      db.memoryStore.tenants.push(newTenant);
      return res.status(201).json({ success: true, message: 'Thêm khách thuê thành công', data: newTenant });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi thêm khách thuê', error: error.message });
  }
};

exports.updateTenant = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    const { full_name, phone, id_card, birth_date, hometown } = req.body;

    if (db.isConnected) {
      const update = await db.query(
        `UPDATE tenants
         SET full_name = COALESCE($1, full_name),
             phone = COALESCE($2, phone),
             id_card = COALESCE($3, id_card),
             birth_date = COALESCE($4, birth_date),
             hometown = COALESCE($5, hometown)
         WHERE id = $6 AND user_id = $7 RETURNING *`,
        [full_name, phone, id_card, birth_date, hometown, id, userId]
      );
      if (update.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy khách thuê' });
      }
      return res.json({ success: true, message: 'Cập nhật khách thuê thành công', data: update.rows[0] });
    } else {
      const tenant = db.memoryStore.tenants.find(t => t.id === id && (t.user_id || 1) === userId);
      if (!tenant) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy khách thuê' });
      }
      if (full_name !== undefined) tenant.full_name = full_name;
      if (phone !== undefined) tenant.phone = phone;
      if (id_card !== undefined) tenant.id_card = id_card;
      if (birth_date !== undefined) tenant.birth_date = birth_date;
      if (hometown !== undefined) tenant.hometown = hometown;
      return res.json({ success: true, message: 'Cập nhật khách thuê thành công', data: tenant });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi cập nhật khách thuê', error: error.message });
  }
};

exports.deleteTenant = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    if (db.isConnected) {
      const result = await db.query('DELETE FROM tenants WHERE id = $1 AND user_id = $2', [id, userId]);
      if (result.rowCount === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy khách thuê' });
      }
      return res.json({ success: true, message: 'Xóa khách thuê thành công' });
    } else {
      const index = db.memoryStore.tenants.findIndex(t => t.id === id && (t.user_id || 1) === userId);
      if (index === -1) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy khách thuê' });
      }
      db.memoryStore.tenants.splice(index, 1);
      return res.json({ success: true, message: 'Xóa khách thuê thành công' });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Không thể xóa khách đang có hợp đồng', error: error.message });
  }
};
