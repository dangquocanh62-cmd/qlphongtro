const db = require('../config/db');

exports.getRooms = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { status, q } = req.query;

    if (db.isConnected) {
      let query = `
        SELECT r.*, 
               c.id as contract_id, c.start_date as contract_start, c.end_date as contract_end,
               t.id as tenant_id, t.full_name as tenant_name, t.phone as tenant_phone
        FROM rooms r
        LEFT JOIN contracts c ON r.id = c.room_id AND c.status = 'ACTIVE'
        LEFT JOIN tenants t ON c.tenant_id = t.id
        WHERE r.user_id = $1
      `;
      const params = [userId];
      if (status) {
        params.push(status);
        query += ` AND r.status = $${params.length}`;
      }
      if (q) {
        params.push(`%${q}%`);
        query += ` AND (r.room_code ILIKE $${params.length} OR r.note ILIKE $${params.length})`;
      }
      query += ' ORDER BY r.room_code ASC';
      const result = await db.query(query, params);
      return res.json({ success: true, data: result.rows });
    } else {
      let rooms = db.memoryStore.rooms.filter(r => (r.user_id || 1) === userId);
      if (status) {
        rooms = rooms.filter(r => r.status.toUpperCase() === status.toUpperCase());
      }
      if (q) {
        const queryLower = q.toLowerCase();
        rooms = rooms.filter(r => 
          r.room_code.toLowerCase().includes(queryLower) || 
          (r.note && r.note.toLowerCase().includes(queryLower))
        );
      }

      // Attach active contract and tenant information if occupied
      const enriched = rooms.map(room => {
        const contract = db.memoryStore.contracts.find(c => c.room_id === room.id && c.status === 'ACTIVE' && (c.user_id || 1) === userId);
        let tenant = null;
        if (contract) {
          tenant = db.memoryStore.tenants.find(t => t.id === contract.tenant_id);
        }
        return {
          ...room,
          contract_id: contract ? contract.id : null,
          contract_start: contract ? contract.start_date : null,
          contract_end: contract ? contract.end_date : null,
          tenant_id: tenant ? tenant.id : null,
          tenant_name: tenant ? tenant.full_name : null,
          tenant_phone: tenant ? tenant.phone : null
        };
      });

      return res.json({ success: true, data: enriched });
    }
  } catch (error) {
    console.error('getRooms error:', error);
    return res.status(500).json({ success: false, message: 'Lỗi tải danh sách phòng', error: error.message });
  }
};

exports.getRoomById = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    if (db.isConnected) {
      const result = await db.query(`
        SELECT r.*, 
               c.id as contract_id, c.start_date as contract_start, c.end_date as contract_end, c.rent_price as contract_rent_price,
               t.id as tenant_id, t.full_name as tenant_name, t.phone as tenant_phone
        FROM rooms r
        LEFT JOIN contracts c ON r.id = c.room_id AND c.status = 'ACTIVE'
        LEFT JOIN tenants t ON c.tenant_id = t.id
        WHERE r.id = $1 AND r.user_id = $2
      `, [id, userId]);
      if (result.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy phòng' });
      }
      return res.json({ success: true, data: result.rows[0] });
    } else {
      const room = db.memoryStore.rooms.find(r => r.id === id && (r.user_id || 1) === userId);
      if (!room) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy phòng' });
      }
      const contract = db.memoryStore.contracts.find(c => c.room_id === room.id && c.status === 'ACTIVE' && (c.user_id || 1) === userId);
      let tenant = null;
      if (contract) {
        tenant = db.memoryStore.tenants.find(t => t.id === contract.tenant_id);
      }
      return res.json({
        success: true,
        data: {
          ...room,
          contract_id: contract ? contract.id : null,
          contract_start: contract ? contract.start_date : null,
          contract_end: contract ? contract.end_date : null,
          contract_rent_price: contract ? contract.rent_price : null,
          tenant_id: tenant ? tenant.id : null,
          tenant_name: tenant ? tenant.full_name : null,
          tenant_phone: tenant ? tenant.phone : null
        }
      });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tra cứu phòng', error: error.message });
  }
};

exports.createRoom = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { room_code, floor, area, price, status, note } = req.body;
    if (!room_code || price === undefined || price < 0) {
      return res.status(400).json({ success: false, message: 'Mã phòng và giá thuê (>= 0) là bắt buộc' });
    }

    const roomStatus = status || 'AVAILABLE';

    if (db.isConnected) {
      const existing = await db.query('SELECT id FROM rooms WHERE room_code = $1 AND user_id = $2', [room_code, userId]);
      if (existing.rows.length > 0) {
        return res.status(400).json({ success: false, message: `Mã phòng ${room_code} đã tồn tại trong nhà trọ của bạn` });
      }
      const insert = await db.query(
        `INSERT INTO rooms (user_id, room_code, floor, area, price, status, note) 
         VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *`,
        [userId, room_code, floor || 1, area || 0, price, roomStatus, note || null]
      );
      return res.status(201).json({ success: true, message: 'Thêm phòng thành công', data: insert.rows[0] });
    } else {
      const existing = db.memoryStore.rooms.find(r => (r.user_id || 1) === userId && r.room_code.toLowerCase() === room_code.toLowerCase());
      if (existing) {
        return res.status(400).json({ success: false, message: `Mã phòng ${room_code} đã tồn tại trong nhà trọ của bạn` });
      }
      const newRoom = {
        id: db.memoryStore.rooms.length > 0 ? Math.max(...db.memoryStore.rooms.map(r => r.id)) + 1 : 1,
        user_id: userId,
        room_code,
        floor: Number(floor) || 1,
        area: Number(area) || 0,
        price: Number(price),
        status: roomStatus,
        note: note || ''
      };
      db.memoryStore.rooms.push(newRoom);
      return res.status(201).json({ success: true, message: 'Thêm phòng thành công', data: newRoom });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi thêm phòng mới', error: error.message });
  }
};

exports.updateRoom = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    const { room_code, floor, area, price, status, note } = req.body;

    if (db.isConnected) {
      const existing = await db.query('SELECT * FROM rooms WHERE id = $1 AND user_id = $2', [id, userId]);
      if (existing.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy phòng' });
      }
      const update = await db.query(
        `UPDATE rooms 
         SET room_code = COALESCE($1, room_code),
             floor = COALESCE($2, floor),
             area = COALESCE($3, area),
             price = COALESCE($4, price),
             status = COALESCE($5, status),
             note = COALESCE($6, note)
         WHERE id = $7 AND user_id = $8 RETURNING *`,
        [room_code, floor, area, price, status, note, id, userId]
      );
      return res.json({ success: true, message: 'Cập nhật phòng thành công', data: update.rows[0] });
    } else {
      const room = db.memoryStore.rooms.find(r => r.id === id && (r.user_id || 1) === userId);
      if (!room) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy phòng' });
      }
      if (room_code !== undefined) room.room_code = room_code;
      if (floor !== undefined) room.floor = Number(floor);
      if (area !== undefined) room.area = Number(area);
      if (price !== undefined) room.price = Number(price);
      if (status !== undefined) room.status = status;
      if (note !== undefined) room.note = note;
      return res.json({ success: true, message: 'Cập nhật phòng thành công', data: room });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi cập nhật phòng', error: error.message });
  }
};

exports.deleteRoom = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    if (db.isConnected) {
      const result = await db.query('DELETE FROM rooms WHERE id = $1 AND user_id = $2', [id, userId]);
      if (result.rowCount === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy phòng' });
      }
      return res.json({ success: true, message: 'Xóa phòng thành công' });
    } else {
      const index = db.memoryStore.rooms.findIndex(r => r.id === id && (r.user_id || 1) === userId);
      if (index === -1) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy phòng' });
      }
      db.memoryStore.rooms.splice(index, 1);
      return res.json({ success: true, message: 'Xóa phòng thành công' });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Không thể xóa phòng đang có hợp đồng hoặc hóa đơn', error: error.message });
  }
};
