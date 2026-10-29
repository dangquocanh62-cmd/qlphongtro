const db = require('../config/db');

exports.getInvoices = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { status, contractId, month, year } = req.query;

    if (db.isConnected) {
      let query = `
        SELECT i.*, 
               c.room_id, r.room_code,
               t.full_name as tenant_name, t.phone as tenant_phone,
               COALESCE((SELECT SUM(amount) FROM payments p WHERE p.invoice_id = i.id), 0) as paid_amount
        FROM invoices i
        JOIN contracts c ON i.contract_id = c.id
        JOIN rooms r ON c.room_id = r.id
        JOIN tenants t ON c.tenant_id = t.id
        WHERE i.user_id = $1
      `;
      const params = [userId];
      if (status) {
        params.push(status);
        query += ` AND i.status = $${params.length}`;
      }
      if (contractId) {
        params.push(contractId);
        query += ` AND i.contract_id = $${params.length}`;
      }
      if (month) {
        params.push(month);
        query += ` AND i.month = $${params.length}`;
      }
      if (year) {
        params.push(year);
        query += ` AND i.year = $${params.length}`;
      }
      query += ' ORDER BY i.year DESC, i.month DESC, i.id DESC';
      const result = await db.query(query, params);
      return res.json({ success: true, data: result.rows });
    } else {
      let invoices = db.memoryStore.invoices.filter(i => (i.user_id || 1) === userId);
      if (status) invoices = invoices.filter(i => i.status === status);
      if (contractId) invoices = invoices.filter(i => i.contract_id === parseInt(contractId, 10));
      if (month) invoices = invoices.filter(i => i.month === parseInt(month, 10));
      if (year) invoices = invoices.filter(i => i.year === parseInt(year, 10));

      const enriched = invoices.map(i => {
        const contract = db.memoryStore.contracts.find(c => c.id === i.contract_id) || {};
        const room = db.memoryStore.rooms.find(r => r.id === contract.room_id) || {};
        const tenant = db.memoryStore.tenants.find(t => t.id === contract.tenant_id) || {};
        const payments = db.memoryStore.payments.filter(p => p.invoice_id === i.id);
        const paidAmount = payments.reduce((sum, p) => sum + Number(p.amount), 0);

        return {
          ...i,
          room_id: contract.room_id || null,
          room_code: room.room_code || '',
          tenant_name: tenant.full_name || '',
          tenant_phone: tenant.phone || '',
          paid_amount: paidAmount,
          remaining_amount: Math.max(0, Number(i.total_amount) - paidAmount)
        };
      });

      return res.json({ success: true, data: enriched });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tải danh sách hóa đơn', error: error.message });
  }
};

exports.getInvoiceById = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);

    if (db.isConnected) {
      const invoiceRes = await db.query(`
        SELECT i.*, 
               c.room_id, r.room_code,
               t.full_name as tenant_name, t.phone as tenant_phone, t.id_card as tenant_id_card
        FROM invoices i
        JOIN contracts c ON i.contract_id = c.id
        JOIN rooms r ON c.room_id = r.id
        JOIN tenants t ON c.tenant_id = t.id
        WHERE i.id = $1 AND i.user_id = $2
      `, [id, userId]);
      if (invoiceRes.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hóa đơn' });
      }

      const invoice = invoiceRes.rows[0];
      const itemsRes = await db.query('SELECT * FROM invoice_items WHERE invoice_id = $1 ORDER BY id ASC', [id]);
      const paymentsRes = await db.query('SELECT * FROM payments WHERE invoice_id = $1 ORDER BY paid_at DESC', [id]);

      invoice.items = itemsRes.rows;
      invoice.payments = paymentsRes.rows;
      invoice.paid_amount = invoice.payments.reduce((sum, p) => sum + Number(p.amount), 0);
      invoice.remaining_amount = Math.max(0, Number(invoice.total_amount) - invoice.paid_amount);

      return res.json({ success: true, data: invoice });
    } else {
      const invoice = db.memoryStore.invoices.find(i => i.id === id && (i.user_id || 1) === userId);
      if (!invoice) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hóa đơn' });
      }
      const contract = db.memoryStore.contracts.find(c => c.id === invoice.contract_id) || {};
      const room = db.memoryStore.rooms.find(r => r.id === contract.room_id) || {};
      const tenant = db.memoryStore.tenants.find(t => t.id === contract.tenant_id) || {};
      const items = db.memoryStore.invoice_items.filter(it => it.invoice_id === id);
      const payments = db.memoryStore.payments.filter(p => p.invoice_id === id);
      const paidAmount = payments.reduce((sum, p) => sum + Number(p.amount), 0);

      return res.json({
        success: true,
        data: {
          ...invoice,
          room_id: contract.room_id,
          room_code: room.room_code || '',
          tenant_name: tenant.full_name || '',
          tenant_phone: tenant.phone || '',
          tenant_id_card: tenant.id_card || '',
          items,
          payments,
          paid_amount: paidAmount,
          remaining_amount: Math.max(0, Number(invoice.total_amount) - paidAmount)
        }
      });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tra cứu chi tiết hóa đơn', error: error.message });
  }
};

exports.createInvoice = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { contract_id, month, year, due_date, items } = req.body;
    if (!contract_id || !month || !year || !Array.isArray(items) || items.length === 0) {
      return res.status(400).json({ success: false, message: 'Vui lòng cung cấp hợp đồng, tháng, năm và danh sách các mục chi phí' });
    }

    const totalAmount = items.reduce((sum, item) => sum + (Number(item.quantity || 1) * Number(item.unit_price || 0)), 0);

    if (db.isConnected) {
      // Check contract belongs to user
      const cCheck = await db.query('SELECT id FROM contracts WHERE id = $1 AND user_id = $2', [contract_id, userId]);
      if (cCheck.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng thuộc quyền quản lý của bạn' });
      }

      // Check existing invoice for this contract and month/year
      const existing = await db.query(
        'SELECT id FROM invoices WHERE contract_id = $1 AND month = $2 AND year = $3 AND user_id = $4',
        [contract_id, month, year, userId]
      );
      if (existing.rows.length > 0) {
        return res.status(400).json({ success: false, message: `Hóa đơn tháng ${month}/${year} cho hợp đồng này đã tồn tại` });
      }

      const invRes = await db.query(
        `INSERT INTO invoices (user_id, contract_id, month, year, total_amount, status, due_date)
         VALUES ($1, $2, $3, $4, $5, 'UNPAID', $6) RETURNING *`,
        [userId, contract_id, month, year, totalAmount, due_date || null]
      );
      const invoice = invRes.rows[0];

      for (const item of items) {
        const amt = Number(item.quantity || 1) * Number(item.unit_price || 0);
        await db.query(
          `INSERT INTO invoice_items (invoice_id, description, quantity, unit_price, amount)
           VALUES ($1, $2, $3, $4, $5)`,
          [invoice.id, item.description, item.quantity || 1, item.unit_price, amt]
        );
      }

      return res.status(201).json({ success: true, message: 'Tạo hóa đơn thành công', data: invoice });
    } else {
      const contract = db.memoryStore.contracts.find(c => c.id === parseInt(contract_id, 10) && (c.user_id || 1) === userId);
      if (!contract) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng thuộc quyền quản lý của bạn' });
      }

      const existing = db.memoryStore.invoices.find(
        i => (i.user_id || 1) === userId && i.contract_id === parseInt(contract_id, 10) && i.month === parseInt(month, 10) && i.year === parseInt(year, 10)
      );
      if (existing) {
        return res.status(400).json({ success: false, message: `Hóa đơn tháng ${month}/${year} cho hợp đồng này đã tồn tại` });
      }

      const newId = db.memoryStore.invoices.length > 0 ? Math.max(...db.memoryStore.invoices.map(i => i.id)) + 1 : 1;
      const invoice = {
        id: newId,
        user_id: userId,
        contract_id: parseInt(contract_id, 10),
        month: parseInt(month, 10),
        year: parseInt(year, 10),
        total_amount: totalAmount,
        status: 'UNPAID',
        due_date: due_date || null,
        created_at: new Date().toISOString()
      };
      db.memoryStore.invoices.push(invoice);

      items.forEach((item, idx) => {
        const amt = Number(item.quantity || 1) * Number(item.unit_price || 0);
        db.memoryStore.invoice_items.push({
          id: db.memoryStore.invoice_items.length + 1 + idx,
          invoice_id: newId,
          description: item.description,
          quantity: Number(item.quantity || 1),
          unit_price: Number(item.unit_price),
          amount: amt
        });
      });

      return res.status(201).json({ success: true, message: 'Tạo hóa đơn thành công', data: invoice });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tạo hóa đơn', error: error.message });
  }
};

// Tự động tạo hóa đơn theo hợp đồng và chỉ số tháng hiện tại
exports.generateMonthlyInvoice = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { contract_id, month, year, due_date } = req.body;
    if (!contract_id || !month || !year) {
      return res.status(400).json({ success: false, message: 'Vui lòng chọn hợp đồng, tháng và năm tính tiền' });
    }

    const m = parseInt(month, 10);
    const y = parseInt(year, 10);

    let contract = null;
    let room = null;
    let services = [];
    let meterReadings = [];

    if (db.isConnected) {
      const cRes = await db.query('SELECT * FROM contracts WHERE id = $1 AND user_id = $2', [contract_id, userId]);
      if (cRes.rows.length === 0) return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng' });
      contract = cRes.rows[0];

      const rRes = await db.query('SELECT * FROM rooms WHERE id = $1 AND user_id = $2', [contract.room_id, userId]);
      room = rRes.rows[0];

      const sRes = await db.query('SELECT * FROM services WHERE user_id = $1', [userId]);
      services = sRes.rows;

      const mrRes = await db.query('SELECT * FROM meter_readings WHERE room_id = $1 AND month = $2 AND year = $3 AND user_id = $4', [contract.room_id, m, y, userId]);
      meterReadings = mrRes.rows;
    } else {
      contract = db.memoryStore.contracts.find(c => c.id === parseInt(contract_id, 10) && (c.user_id || 1) === userId);
      if (!contract) return res.status(404).json({ success: false, message: 'Không tìm thấy hợp đồng' });
      room = db.memoryStore.rooms.find(r => r.id === contract.room_id && (r.user_id || 1) === userId);
      services = db.memoryStore.services.filter(s => (s.user_id || 1) === userId);
      meterReadings = db.memoryStore.meter_readings.filter(mr => (mr.user_id || 1) === userId && mr.room_id === contract.room_id && mr.month === m && mr.year === y);
    }

    // Xây dựng danh sách các mục chi phí (invoice_items)
    const items = [];

    // 1. Tiền phòng
    items.push({
      description: `Tiền thuê phòng ${room ? room.room_code : ''} tháng ${m}/${y}`,
      quantity: 1,
      unit_price: Number(contract.rent_price),
      amount: Number(contract.rent_price)
    });

    // 2. Dịch vụ theo công tơ (Điện, Nước)
    for (const s of services.filter(sv => sv.is_metered)) {
      const reading = meterReadings.find(r => r.service_id === s.id);
      if (reading) {
        const usage = Number(reading.new_value) - Number(reading.old_value);
        items.push({
          description: `${s.name} (${reading.old_value} -> ${reading.new_value} ${s.unit})`,
          quantity: usage,
          unit_price: Number(s.unit_price),
          amount: usage * Number(s.unit_price)
        });
      }
    }

    // 3. Dịch vụ cố định (Internet, Rác...)
    for (const s of services.filter(sv => !sv.is_metered)) {
      items.push({
        description: `${s.name}`,
        quantity: 1,
        unit_price: Number(s.unit_price),
        amount: Number(s.unit_price)
      });
    }

    // Lưu hóa đơn
    const reqWrapper = {
      user: req.user,
      body: {
        contract_id,
        month: m,
        year: y,
        due_date: due_date || `${y}-${String(m).padStart(2, '0')}-05`,
        items
      }
    };
    return exports.createInvoice(reqWrapper, res);
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tự động tạo hóa đơn', error: error.message });
  }
};

exports.deleteInvoice = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const id = parseInt(req.params.id, 10);
    if (db.isConnected) {
      const result = await db.query('DELETE FROM invoices WHERE id = $1 AND user_id = $2', [id, userId]);
      if (result.rowCount === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hóa đơn' });
      }
      return res.json({ success: true, message: 'Xóa hóa đơn thành công' });
    } else {
      const idx = db.memoryStore.invoices.findIndex(i => i.id === id && (i.user_id || 1) === userId);
      if (idx === -1) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hóa đơn' });
      }
      db.memoryStore.invoices.splice(idx, 1);
      // Delete child items
      db.memoryStore.invoice_items = db.memoryStore.invoice_items.filter(it => it.invoice_id !== id);
      db.memoryStore.payments = db.memoryStore.payments.filter(p => p.invoice_id !== id);
      return res.json({ success: true, message: 'Xóa hóa đơn thành công' });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi xóa hóa đơn', error: error.message });
  }
};

// Lập hóa đơn hàng loạt cho tất cả các phòng đang có hợp đồng hiệu lực
exports.bulkGenerateInvoices = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const { month, year, due_date } = req.body;

    if (!month || !year) {
      return res.status(400).json({ success: false, message: 'Vui lòng chọn tháng và năm lập hóa đơn hàng loạt' });
    }

    const m = parseInt(month, 10);
    const y = parseInt(year, 10);
    const dueDate = due_date || `${y}-${String(m).padStart(2, '0')}-05`;

    let activeContracts = [];
    let services = [];
    let meterReadings = [];
    let existingInvoices = [];

    if (db.isConnected) {
      // 1. Get active contracts
      const cRes = await db.query(`
        SELECT c.*, r.room_code 
        FROM contracts c 
        JOIN rooms r ON c.room_id = r.id 
        WHERE c.user_id = $1 AND c.status = 'ACTIVE'
        ORDER BY r.room_code ASC
      `, [userId]);
      activeContracts = cRes.rows;

      // 2. Get services
      const sRes = await db.query('SELECT * FROM services WHERE user_id = $1', [userId]);
      services = sRes.rows;

      // 3. Get meter readings for this month/year
      const mrRes = await db.query('SELECT * FROM meter_readings WHERE month = $1 AND year = $2 AND user_id = $3', [m, y, userId]);
      meterReadings = mrRes.rows;

      // 4. Get existing invoices for this month/year
      const invRes = await db.query('SELECT contract_id FROM invoices WHERE month = $1 AND year = $2 AND user_id = $3', [m, y, userId]);
      existingInvoices = invRes.rows;
    } else {
      activeContracts = db.memoryStore.contracts
        .filter(c => (c.user_id || 1) === userId && c.status === 'ACTIVE')
        .map(c => {
          const room = db.memoryStore.rooms.find(r => r.id === c.room_id) || {};
          return { ...c, room_code: room.room_code || '' };
        });
      services = db.memoryStore.services.filter(s => (s.user_id || 1) === userId);
      meterReadings = db.memoryStore.meter_readings.filter(mr => (mr.user_id || 1) === userId && mr.month === m && mr.year === y);
      existingInvoices = db.memoryStore.invoices.filter(i => (i.user_id || 1) === userId && i.month === m && i.year === y);
    }

    if (activeContracts.length === 0) {
      return res.json({
        success: true,
        message: 'Không có phòng nào đang có hợp đồng hiệu lực để lập hóa đơn.',
        data: { created_count: 0, skipped_count: 0, details: [] }
      });
    }

    const existingContractIds = new Set(existingInvoices.map(i => Number(i.contract_id)));
    const details = [];
    let createdCount = 0;
    let skippedCount = 0;

    for (const contract of activeContracts) {
      if (existingContractIds.has(contract.id)) {
        skippedCount++;
        details.push({
          room_code: contract.room_code,
          contract_id: contract.id,
          status: 'SKIPPED',
          message: 'Đã có hóa đơn cho tháng này'
        });
        continue;
      }

      // Build invoice items
      const items = [];

      // 1. Tiền phòng
      items.push({
        description: `Tiền thuê phòng ${contract.room_code} tháng ${m}/${y}`,
        quantity: 1,
        unit_price: Number(contract.rent_price),
        amount: Number(contract.rent_price)
      });

      // 2. Dịch vụ theo công tơ (Điện, Nước)
      for (const s of services.filter(sv => sv.is_metered)) {
        const reading = meterReadings.find(r => r.room_id === contract.room_id && r.service_id === s.id);
        if (reading) {
          const usage = Math.max(0, Number(reading.new_value) - Number(reading.old_value));
          items.push({
            description: `${s.name} (${reading.old_value} -> ${reading.new_value} ${s.unit})`,
            quantity: usage,
            unit_price: Number(s.unit_price),
            amount: usage * Number(s.unit_price)
          });
        }
      }

      // 3. Dịch vụ cố định (Internet, Rác...)
      for (const s of services.filter(sv => !sv.is_metered)) {
        items.push({
          description: `${s.name}`,
          quantity: 1,
          unit_price: Number(s.unit_price),
          amount: Number(s.unit_price)
        });
      }

      const totalAmount = items.reduce((sum, it) => sum + (Number(it.quantity || 1) * Number(it.unit_price || 0)), 0);

      if (db.isConnected) {
        const invRes = await db.query(
          `INSERT INTO invoices (user_id, contract_id, month, year, total_amount, status, due_date)
           VALUES ($1, $2, $3, $4, $5, 'UNPAID', $6) RETURNING *`,
          [userId, contract.id, m, y, totalAmount, dueDate]
        );
        const newInvoice = invRes.rows[0];

        for (const item of items) {
          const amt = Number(item.quantity || 1) * Number(item.unit_price || 0);
          await db.query(
            `INSERT INTO invoice_items (invoice_id, description, quantity, unit_price, amount)
             VALUES ($1, $2, $3, $4, $5)`,
            [newInvoice.id, item.description, item.quantity || 1, item.unit_price, amt]
          );
        }
      } else {
        const newId = db.memoryStore.invoices.length > 0 ? Math.max(...db.memoryStore.invoices.map(i => i.id)) + 1 : 1;
        const newInvoice = {
          id: newId,
          user_id: userId,
          contract_id: contract.id,
          month: m,
          year: y,
          total_amount: totalAmount,
          status: 'UNPAID',
          due_date: dueDate,
          created_at: new Date().toISOString()
        };
        db.memoryStore.invoices.push(newInvoice);

        items.forEach((item, idx) => {
          const amt = Number(item.quantity || 1) * Number(item.unit_price || 0);
          db.memoryStore.invoice_items.push({
            id: db.memoryStore.invoice_items.length + 1 + idx,
            invoice_id: newId,
            description: item.description,
            quantity: Number(item.quantity || 1),
            unit_price: Number(item.unit_price),
            amount: amt
          });
        });
      }

      createdCount++;
      details.push({
        room_code: contract.room_code,
        contract_id: contract.id,
        status: 'CREATED',
        total_amount: totalAmount
      });
    }

    return res.json({
      success: true,
      message: `Đã lập ${createdCount} hóa đơn thành công! (Bỏ qua ${skippedCount} phòng đã có hóa đơn)`,
      data: {
        created_count: createdCount,
        skipped_count: skippedCount,
        details
      }
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi lập hóa đơn hàng loạt', error: error.message });
  }
};

exports.addInvoiceItem = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const invoiceId = parseInt(req.params.id, 10);
    const { description, quantity, unit_price } = req.body;

    if (!description || unit_price === undefined) {
      return res.status(400).json({ success: false, message: 'Vui lòng cung cấp tên khoản thu và đơn giá' });
    }

    const qty = Number(quantity) || 1;
    const unitPrice = Number(unit_price);
    const amount = qty * unitPrice;

    if (db.isConnected) {
      const invCheck = await db.query('SELECT * FROM invoices WHERE id = $1 AND user_id = $2', [invoiceId, userId]);
      if (invCheck.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hóa đơn' });
      }

      const itemRes = await db.query(
        `INSERT INTO invoice_items (invoice_id, description, quantity, unit_price, amount)
         VALUES ($1, $2, $3, $4, $5) RETURNING *`,
        [invoiceId, description, qty, unitPrice, amount]
      );

      // Cập nhật lại total_amount của hóa đơn
      await db.query(
        `UPDATE invoices 
         SET total_amount = total_amount + $1
         WHERE id = $2 AND user_id = $3`,
        [amount, invoiceId, userId]
      );

      return res.status(201).json({
        success: true,
        message: 'Thêm mục dịch vụ thành công',
        data: itemRes.rows[0]
      });
    } else {
      const invoice = db.memoryStore.invoices.find(i => i.id === invoiceId && (i.user_id || 1) === userId);
      if (!invoice) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hóa đơn' });
      }

      const newItem = {
        id: db.memoryStore.invoice_items.length + 1,
        invoice_id: invoiceId,
        description,
        quantity: qty,
        unit_price: unitPrice,
        amount
      };
      db.memoryStore.invoice_items.push(newItem);
      invoice.total_amount = Number(invoice.total_amount) + amount;

      return res.status(201).json({
        success: true,
        message: 'Thêm mục dịch vụ thành công',
        data: newItem
      });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi thêm mục vào hóa đơn', error: error.message });
  }
};

exports.removeInvoiceItem = async (req, res) => {
  try {
    const userId = req.user?.id || 1;
    const invoiceId = parseInt(req.params.id, 10);
    const itemId = parseInt(req.params.itemId, 10);

    if (db.isConnected) {
      const invCheck = await db.query('SELECT * FROM invoices WHERE id = $1 AND user_id = $2', [invoiceId, userId]);
      if (invCheck.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hóa đơn' });
      }

      const itemCheck = await db.query('SELECT * FROM invoice_items WHERE id = $1 AND invoice_id = $2', [itemId, invoiceId]);
      if (itemCheck.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy mục chi phí trong hóa đơn' });
      }

      const itemAmount = Number(itemCheck.rows[0].amount);

      await db.query('DELETE FROM invoice_items WHERE id = $1', [itemId]);
      await db.query(
        `UPDATE invoices 
         SET total_amount = GREATEST(0, total_amount - $1)
         WHERE id = $2 AND user_id = $3`,
        [itemAmount, invoiceId, userId]
      );

      return res.json({ success: true, message: 'Xóa mục chi phí thành công' });
    } else {
      const invoice = db.memoryStore.invoices.find(i => i.id === invoiceId && (i.user_id || 1) === userId);
      if (!invoice) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hóa đơn' });
      }

      const idx = db.memoryStore.invoice_items.findIndex(it => it.id === itemId && it.invoice_id === invoiceId);
      if (idx === -1) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy mục chi phí trong hóa đơn' });
      }

      const itemAmount = Number(db.memoryStore.invoice_items[idx].amount);
      db.memoryStore.invoice_items.splice(idx, 1);
      invoice.total_amount = Math.max(0, Number(invoice.total_amount) - itemAmount);

      return res.json({ success: true, message: 'Xóa mục chi phí thành công' });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi xóa mục chi phí', error: error.message });
  }
};

