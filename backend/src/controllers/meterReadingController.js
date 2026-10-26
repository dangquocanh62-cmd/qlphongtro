const db = require('../config/db');

exports.getMeterReadings = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { roomId, serviceId, month, year } = req.query;

    if (db.isConnected) {
      let query = `
        SELECT mr.*, 
               r.room_code,
               s.name as service_name, s.unit as service_unit, s.unit_price as service_unit_price,
               (mr.new_value - mr.old_value) as usage_amount,
               ((mr.new_value - mr.old_value) * s.unit_price) as total_cost
        FROM meter_readings mr
        JOIN rooms r ON mr.room_id = r.id
        JOIN services s ON mr.service_id = s.id
        WHERE mr.user_id = $1
      `;
      const params = [userId];
      if (roomId) {
        params.push(roomId);
        query += ` AND mr.room_id = $${params.length}`;
      }
      if (serviceId) {
        params.push(serviceId);
        query += ` AND mr.service_id = $${params.length}`;
      }
      if (month) {
        params.push(month);
        query += ` AND mr.month = $${params.length}`;
      }
      if (year) {
        params.push(year);
        query += ` AND mr.year = $${params.length}`;
      }
      query += ' ORDER BY mr.year DESC, mr.month DESC, mr.room_id ASC';
      const result = await db.query(query, params);
      return res.json({ success: true, data: result.rows });
    } else {
      let readings = db.memoryStore.meter_readings.filter(m => (m.user_id || 1) === userId);
      if (roomId) readings = readings.filter(m => m.room_id === parseInt(roomId, 10));
      if (serviceId) readings = readings.filter(m => m.service_id === parseInt(serviceId, 10));
      if (month) readings = readings.filter(m => m.month === parseInt(month, 10));
      if (year) readings = readings.filter(m => m.year === parseInt(year, 10));

      const enriched = readings.map(m => {
        const room = db.memoryStore.rooms.find(r => r.id === m.room_id) || {};
        const service = db.memoryStore.services.find(s => s.id === m.service_id) || {};
        const usage = Number(m.new_value) - Number(m.old_value);
        const cost = usage * (service.unit_price || 0);
        return {
          ...m,
          room_code: room.room_code || '',
          service_name: service.name || '',
          service_unit: service.unit || '',
          service_unit_price: service.unit_price || 0,
          usage_amount: usage,
          total_cost: cost
        };
      });

      return res.json({ success: true, data: enriched });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tải chỉ số điện nước', error: error.message });
  }
};

exports.getLatestReading = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { roomId, serviceId } = req.query;
    if (!roomId || !serviceId) {
      return res.status(400).json({ success: false, message: 'Vui lòng cung cấp roomId và serviceId' });
    }

    if (db.isConnected) {
      const result = await db.query(
        `SELECT * FROM meter_readings 
         WHERE room_id = $1 AND service_id = $2 AND user_id = $3
         ORDER BY year DESC, month DESC LIMIT 1`,
        [roomId, serviceId, userId]
      );
      return res.json({ success: true, data: result.rows[0] || null });
    } else {
      const list = db.memoryStore.meter_readings
        .filter(m => (m.user_id || 1) === userId && m.room_id === parseInt(roomId, 10) && m.service_id === parseInt(serviceId, 10))
        .sort((a, b) => (b.year * 12 + b.month) - (a.year * 12 + a.month));
      return res.json({ success: true, data: list[0] || null });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tra cứu chỉ số gần nhất', error: error.message });
  }
};

exports.createMeterReading = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { room_id, service_id, month, year, old_value, new_value } = req.body;
    if (!room_id || !service_id || !month || !year || old_value === undefined || new_value === undefined) {
      return res.status(400).json({ success: false, message: 'Vui lòng điền đủ phòng, dịch vụ, tháng, năm và các chỉ số' });
    }

    const m = parseInt(month, 10);
    const y = parseInt(year, 10);
    const oldVal = parseFloat(old_value);
    const newVal = parseFloat(new_value);

    if (m < 1 || m > 12) {
      return res.status(400).json({ success: false, message: 'Tháng phải từ 1 đến 12' });
    }
    if (newVal < oldVal) {
      return res.status(400).json({ success: false, message: 'Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ' });
    }

    if (db.isConnected) {
      const existing = await db.query(
        `SELECT id FROM meter_readings WHERE room_id = $1 AND service_id = $2 AND month = $3 AND year = $4 AND user_id = $5`,
        [room_id, service_id, m, y, userId]
      );
      if (existing.rows.length > 0) {
        return res.status(400).json({ success: false, message: `Chỉ số của phòng cho dịch vụ này trong tháng ${m}/${y} đã được ghi` });
      }

      const insert = await db.query(
        `INSERT INTO meter_readings (user_id, room_id, service_id, month, year, old_value, new_value)
         VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *`,
        [userId, room_id, service_id, m, y, oldVal, newVal]
      );
      return res.status(201).json({ success: true, message: 'Ghi chỉ số thành công', data: insert.rows[0] });
    } else {
      const existing = db.memoryStore.meter_readings.find(
        r => (r.user_id || 1) === userId && r.room_id === parseInt(room_id, 10) && r.service_id === parseInt(service_id, 10) && r.month === m && r.year === y
      );
      if (existing) {
        return res.status(400).json({ success: false, message: `Chỉ số của phòng cho dịch vụ này trong tháng ${m}/${y} đã được ghi` });
      }

      const newId = db.memoryStore.meter_readings.length > 0 ? Math.max(...db.memoryStore.meter_readings.map(r => r.id)) + 1 : 1;
      const reading = {
        id: newId,
        user_id: userId,
        room_id: parseInt(room_id, 10),
        service_id: parseInt(service_id, 10),
        month: m,
        year: y,
        old_value: oldVal,
        new_value: newVal
      };
      db.memoryStore.meter_readings.push(reading);
      return res.status(201).json({ success: true, message: 'Ghi chỉ số thành công', data: reading });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi ghi chỉ số điện nước', error: error.message });
  }
};

exports.updateMeterReading = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    const { old_value, new_value } = req.body;

    if (new_value !== undefined && old_value !== undefined && parseFloat(new_value) < parseFloat(old_value)) {
      return res.status(400).json({ success: false, message: 'Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ' });
    }

    if (db.isConnected) {
      const update = await db.query(
        `UPDATE meter_readings 
         SET old_value = COALESCE($1, old_value),
             new_value = COALESCE($2, new_value)
         WHERE id = $3 AND user_id = $4 RETURNING *`,
        [old_value, new_value, id, userId]
      );
      if (update.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy bản ghi chỉ số' });
      }
      return res.json({ success: true, message: 'Cập nhật chỉ số thành công', data: update.rows[0] });
    } else {
      const reading = db.memoryStore.meter_readings.find(m => m.id === id && (m.user_id || 1) === userId);
      if (!reading) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy bản ghi chỉ số' });
      }
      if (old_value !== undefined) reading.old_value = parseFloat(old_value);
      if (new_value !== undefined) reading.new_value = parseFloat(new_value);
      return res.json({ success: true, message: 'Cập nhật chỉ số thành công', data: reading });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi cập nhật chỉ số', error: error.message });
  }
};

exports.deleteMeterReading = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    if (db.isConnected) {
      const result = await db.query('DELETE FROM meter_readings WHERE id = $1 AND user_id = $2', [id, userId]);
      if (result.rowCount === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy bản ghi chỉ số' });
      }
      return res.json({ success: true, message: 'Xóa chỉ số thành công' });
    } else {
      const idx = db.memoryStore.meter_readings.findIndex(m => m.id === id && (m.user_id || 1) === userId);
      if (idx === -1) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy bản ghi chỉ số' });
      }
      db.memoryStore.meter_readings.splice(idx, 1);
      return res.json({ success: true, message: 'Xóa chỉ số thành công' });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi xóa chỉ số', error: error.message });
  }
};
