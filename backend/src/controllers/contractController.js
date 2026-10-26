const db = require('../config/db');

exports.getContracts = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { status, roomId, tenantId } = req.query;

    if (db.isConnected) {
      let query = `
        SELECT c.*, 
               r.room_code, r.floor, r.area,
               t.full_name as tenant_name, t.phone as tenant_phone, t.id_card as tenant_id_card,
               (SELECT COUNT(*) FROM contract_tenants ct WHERE ct.contract_id = c.id) as roommate_count
        FROM contracts c
        JOIN rooms r ON c.room_id = r.id
        JOIN tenants t ON c.tenant_id = t.id
        WHERE c.user_id = $1
      `;
      const params = [userId];
      if (status) {
        params.push(status);
        query += ` AND c.status = $${params.length}`;
      }
      if (roomId) {
        params.push(roomId);
        query += ` AND c.room_id = $${params.length}`;
      }
      if (tenantId) {
        params.push(tenantId);
        query += ` AND c.tenant_id = $${params.length}`;
      }
      query += ' ORDER BY c.id DESC';
      const result = await db.query(query, params);
      return res.json({ success: true, data: result.rows });
    } else {
      let contracts = db.memoryStore.contracts.filter(c => (c.user_id || 1) === userId);
      if (status) {
        contracts = contracts.filter(c => c.status === status);
      }
      if (roomId) {
        contracts = contracts.filter(c => c.room_id === parseInt(roomId, 10));
      }
      if (tenantId) {
        contracts = contracts.filter(c => c.tenant_id === parseInt(tenantId, 10));
      }

      const enriched = contracts.map(c => {
        const room = db.memoryStore.rooms.find(r => r.id === c.room_id) || {};
        const tenant = db.memoryStore.tenants.find(t => t.id === c.tenant_id) || {};
        const roommates = db.memoryStore.contract_tenants.filter(ct => ct.contract_id === c.id);
        return {
          ...c,
          room_code: room.room_code || '',
          floor: room.floor || 1,
          area: room.area || 0,
          tenant_name: tenant.full_name || '',
          tenant_phone: tenant.phone || '',
          tenant_id_card: tenant.id_card || '',
          roommate_count: roommates.length
        };
      });

      return res.json({ success: true, data: enriched });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tải danh sách hợp đồng', error: error.message });
  }
};

exports.getContractById = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    if (db.isConnected) {
      const contractRes = await db.query(`
        SELECT c.*, 
               r.room_code, r.floor, r.area, r.price as standard_price,
               t.full_name as tenant_name, t.phone as tenant_phone, t.id_card as tenant_id_card, t.hometown as tenant_hometown
        FROM contracts c
        JOIN rooms r ON c.room_id = r.id
        JOIN tenants t ON c.tenant_id = t.id
        WHERE c.id = $1 AND c.user_id = $2
      `, [id, userId]);
      if (contractRes.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng' });
      }

      const contract = contractRes.rows[0];
      const roommatesRes = await db.query(`
        SELECT t.* 
        FROM contract_tenants ct
        JOIN tenants t ON ct.tenant_id = t.id
        WHERE ct.contract_id = $1
      `, [id]);
      contract.roommates = roommatesRes.rows;

      return res.json({ success: true, data: contract });
    } else {
      const contract = db.memoryStore.contracts.find(c => c.id === id && (c.user_id || 1) === userId);
      if (!contract) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng' });
      }
      const room = db.memoryStore.rooms.find(r => r.id === contract.room_id) || {};
      const tenant = db.memoryStore.tenants.find(t => t.id === contract.tenant_id) || {};
      const roommateIds = db.memoryStore.contract_tenants
        .filter(ct => ct.contract_id === id)
        .map(ct => ct.tenant_id);
      const roommates = db.memoryStore.tenants.filter(t => roommateIds.includes(t.id));

      return res.json({
        success: true,
        data: {
          ...contract,
          room_code: room.room_code || '',
          floor: room.floor || 1,
          area: room.area || 0,
          standard_price: room.price || 0,
          tenant_name: tenant.full_name || '',
          tenant_phone: tenant.phone || '',
          tenant_id_card: tenant.id_card || '',
          tenant_hometown: tenant.hometown || '',
          roommates
        }
      });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tra cứu hợp đồng', error: error.message });
  }
};

exports.createContract = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { room_id, tenant_id, start_date, end_date, deposit, rent_price, roommate_ids } = req.body;
    if (!room_id || !tenant_id || !start_date || rent_price === undefined) {
      return res.status(400).json({ success: false, message: 'Vui lòng cung cấp phòng, khách đại diện, ngày bắt đầu và giá thuê' });
    }

    if (end_date && new Date(end_date) <= new Date(start_date)) {
      return res.status(400).json({ success: false, message: 'Ngày kết thúc phải sau ngày bắt đầu' });
    }

    if (db.isConnected) {
      // Check room availability for this user
      const roomCheck = await db.query('SELECT status FROM rooms WHERE id = $1 AND user_id = $2', [room_id, userId]);
      if (roomCheck.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy phòng thuộc quyền quản lý của bạn' });
      }

      // Check tenant belongs to this user
      const tenantCheck = await db.query('SELECT id FROM tenants WHERE id = $1 AND user_id = $2', [tenant_id, userId]);
      if (tenantCheck.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy khách thuê thuộc quyền quản lý của bạn' });
      }

      const insert = await db.query(
        `INSERT INTO contracts (user_id, room_id, tenant_id, start_date, end_date, deposit, rent_price, status)
         VALUES ($1, $2, $3, $4, $5, $6, $7, 'ACTIVE') RETURNING *`,
        [userId, room_id, tenant_id, start_date, end_date || null, deposit || 0, rent_price]
      );
      const contract = insert.rows[0];

      // Update room to OCCUPIED
      await db.query(`UPDATE rooms SET status = 'OCCUPIED' WHERE id = $1 AND user_id = $2`, [room_id, userId]);

      // Add roommates if any
      if (Array.isArray(roommate_ids) && roommate_ids.length > 0) {
        for (const rId of roommate_ids) {
          await db.query(
            `INSERT INTO contract_tenants (contract_id, tenant_id) VALUES ($1, $2) ON CONFLICT DO NOTHING`,
            [contract.id, rId]
          );
        }
      }

      return res.status(201).json({ success: true, message: 'Tạo hợp đồng thành công', data: contract });
    } else {
      const room = db.memoryStore.rooms.find(r => r.id === parseInt(room_id, 10) && (r.user_id || 1) === userId);
      if (!room) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy phòng thuộc quyền quản lý của bạn' });
      }

      const newId = db.memoryStore.contracts.length > 0 ? Math.max(...db.memoryStore.contracts.map(c => c.id)) + 1 : 1;
      const newContract = {
        id: newId,
        user_id: userId,
        room_id: parseInt(room_id, 10),
        tenant_id: parseInt(tenant_id, 10),
        start_date,
        end_date: end_date || null,
        deposit: Number(deposit) || 0,
        rent_price: Number(rent_price),
        status: 'ACTIVE'
      };
      db.memoryStore.contracts.push(newContract);
      room.status = 'OCCUPIED';

      if (Array.isArray(roommate_ids) && roommate_ids.length > 0) {
        for (const rId of roommate_ids) {
          db.memoryStore.contract_tenants.push({ contract_id: newId, tenant_id: parseInt(rId, 10) });
        }
      }

      return res.status(201).json({ success: true, message: 'Tạo hợp đồng thành công', data: newContract });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tạo hợp đồng', error: error.message });
  }
};

exports.updateContract = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    const { start_date, end_date, deposit, rent_price, status } = req.body;

    if (db.isConnected) {
      const update = await db.query(
        `UPDATE contracts 
         SET start_date = COALESCE($1, start_date),
             end_date = COALESCE($2, end_date),
             deposit = COALESCE($3, deposit),
             rent_price = COALESCE($4, rent_price),
             status = COALESCE($5, status)
         WHERE id = $6 AND user_id = $7 RETURNING *`,
        [start_date, end_date, deposit, rent_price, status, id, userId]
      );
      if (update.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng' });
      }
      return res.json({ success: true, message: 'Cập nhật hợp đồng thành công', data: update.rows[0] });
    } else {
      const contract = db.memoryStore.contracts.find(c => c.id === id && (c.user_id || 1) === userId);
      if (!contract) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng' });
      }
      if (start_date !== undefined) contract.start_date = start_date;
      if (end_date !== undefined) contract.end_date = end_date;
      if (deposit !== undefined) contract.deposit = Number(deposit);
      if (rent_price !== undefined) contract.rent_price = Number(rent_price);
      if (status !== undefined) contract.status = status;
      return res.json({ success: true, message: 'Cập nhật hợp đồng thành công', data: contract });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi cập nhật hợp đồng', error: error.message });
  }
};

exports.endContract = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    if (db.isConnected) {
      const result = await db.query(
        `UPDATE contracts SET status = 'ENDED' WHERE id = $1 AND user_id = $2 RETURNING *`,
        [id, userId]
      );
      if (result.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng' });
      }
      const contract = result.rows[0];
      // Check if any other active contracts for this room
      const otherActive = await db.query(
        `SELECT id FROM contracts WHERE room_id = $1 AND status = 'ACTIVE' AND user_id = $2`,
        [contract.room_id, userId]
      );
      if (otherActive.rows.length === 0) {
        await db.query(`UPDATE rooms SET status = 'AVAILABLE' WHERE id = $1 AND user_id = $2`, [contract.room_id, userId]);
      }
      return res.json({ success: true, message: 'Kết thúc hợp đồng thành công', data: contract });
    } else {
      const contract = db.memoryStore.contracts.find(c => c.id === id && (c.user_id || 1) === userId);
      if (!contract) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng' });
      }
      contract.status = 'ENDED';
      const hasOtherActive = db.memoryStore.contracts.some(c => c.room_id === contract.room_id && c.status === 'ACTIVE' && (c.user_id || 1) === userId);
      if (!hasOtherActive) {
        const room = db.memoryStore.rooms.find(r => r.id === contract.room_id && (r.user_id || 1) === userId);
        if (room) room.status = 'AVAILABLE';
      }
      return res.json({ success: true, message: 'Kết thúc hợp đồng thành công', data: contract });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi kết thúc hợp đồng', error: error.message });
  }
};

exports.addRoommate = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const contractId = parseInt(req.params.id, 10);
    const { tenant_id } = req.body;
    if (!tenant_id) {
      return res.status(400).json({ success: false, message: 'Mã khách ở cùng là bắt buộc' });
    }

    if (db.isConnected) {
      const cCheck = await db.query('SELECT id FROM contracts WHERE id = $1 AND user_id = $2', [contractId, userId]);
      if (cCheck.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng' });
      }
      await db.query(
        `INSERT INTO contract_tenants (contract_id, tenant_id) VALUES ($1, $2) ON CONFLICT DO NOTHING`,
        [contractId, tenant_id]
      );
      return res.json({ success: true, message: 'Đã thêm người ở cùng vào hợp đồng' });
    } else {
      const contract = db.memoryStore.contracts.find(c => c.id === contractId && (c.user_id || 1) === userId);
      if (!contract) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng' });
      }
      const exists = db.memoryStore.contract_tenants.some(ct => ct.contract_id === contractId && ct.tenant_id === parseInt(tenant_id, 10));
      if (!exists) {
        db.memoryStore.contract_tenants.push({ contract_id: contractId, tenant_id: parseInt(tenant_id, 10) });
      }
      return res.json({ success: true, message: 'Đã thêm người ở cùng vào hợp đồng' });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi thêm người ở cùng', error: error.message });
  }
};

exports.removeRoommate = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const contractId = parseInt(req.params.id, 10);
    const tenantId = parseInt(req.params.tenantId, 10);

    if (db.isConnected) {
      const cCheck = await db.query('SELECT id FROM contracts WHERE id = $1 AND user_id = $2', [contractId, userId]);
      if (cCheck.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng' });
      }
      await db.query(
        `DELETE FROM contract_tenants WHERE contract_id = $1 AND tenant_id = $2`,
        [contractId, tenantId]
      );
      return res.json({ success: true, message: 'Đã xóa người ở cùng khỏi hợp đồng' });
    } else {
      const contract = db.memoryStore.contracts.find(c => c.id === contractId && (c.user_id || 1) === userId);
      if (!contract) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng' });
      }
      const idx = db.memoryStore.contract_tenants.findIndex(ct => ct.contract_id === contractId && ct.tenant_id === tenantId);
      if (idx !== -1) {
        db.memoryStore.contract_tenants.splice(idx, 1);
      }
      return res.json({ success: true, message: 'Đã xóa người ở cùng khỏi hợp đồng' });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi xóa người ở cùng', error: error.message });
  }
};
