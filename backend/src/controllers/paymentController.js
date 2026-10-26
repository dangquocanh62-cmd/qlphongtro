const db = require('../config/db');

exports.getPayments = async (req, res) => {
  try {
    const { invoiceId } = req.query;

    if (db.isConnected) {
      let query = `
        SELECT p.*, i.month, i.year, i.total_amount,
               r.room_code, t.full_name as tenant_name
        FROM payments p
        JOIN invoices i ON p.invoice_id = i.id
        JOIN contracts c ON i.contract_id = c.id
        JOIN rooms r ON c.room_id = r.id
        JOIN tenants t ON c.tenant_id = t.id
        WHERE 1=1
      `;
      const params = [];
      if (invoiceId) {
        params.push(invoiceId);
        query += ` AND p.invoice_id = $${params.length}`;
      }
      query += ' ORDER BY p.paid_at DESC';
      const result = await db.query(query, params);
      return res.json({ success: true, data: result.rows });
    } else {
      let list = [...db.memoryStore.payments];
      if (invoiceId) {
        list = list.filter(p => p.invoice_id === parseInt(invoiceId, 10));
      }
      const enriched = list.map(p => {
        const inv = db.memoryStore.invoices.find(i => i.id === p.invoice_id) || {};
        const contract = db.memoryStore.contracts.find(c => c.id === inv.contract_id) || {};
        const room = db.memoryStore.rooms.find(r => r.id === contract.room_id) || {};
        const tenant = db.memoryStore.tenants.find(t => t.id === contract.tenant_id) || {};
        return {
          ...p,
          month: inv.month,
          year: inv.year,
          total_amount: inv.total_amount,
          room_code: room.room_code || '',
          tenant_name: tenant.full_name || ''
        };
      });
      return res.json({ success: true, data: enriched });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi tải lịch sử thanh toán', error: error.message });
  }
};

exports.createPayment = async (req, res) => {
  try {
    const { invoice_id, amount, method, paid_at } = req.body;
    if (!invoice_id || !amount || Number(amount) <= 0) {
      return res.status(400).json({ success: false, message: 'Mã hóa đơn và số tiền thanh toán (> 0) là bắt buộc' });
    }

    const payMethod = method === 'TRANSFER' ? 'TRANSFER' : 'CASH';
    const payTime = paid_at || new Date().toISOString();
    const payAmount = Number(amount);

    if (db.isConnected) {
      // Get invoice details
      const invRes = await db.query('SELECT total_amount, status FROM invoices WHERE id = $1', [invoice_id]);
      if (invRes.rows.length === 0) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hóa đơn' });
      }
      const invoice = invRes.rows[0];

      // Insert payment
      const pRes = await db.query(
        `INSERT INTO payments (invoice_id, amount, method, paid_at)
         VALUES ($1, $2, $3, $4) RETURNING *`,
        [invoice_id, payAmount, payMethod, payTime]
      );
      const newPayment = pRes.rows[0];

      // Calculate total paid so far
      const totalPaidRes = await db.query(
        'SELECT SUM(amount) as total_paid FROM payments WHERE invoice_id = $1',
        [invoice_id]
      );
      const totalPaid = Number(totalPaidRes.rows[0].total_paid || 0);

      let newStatus = 'PARTIAL';
      if (totalPaid >= Number(invoice.total_amount)) {
        newStatus = 'PAID';
      }

      await db.query('UPDATE invoices SET status = $1 WHERE id = $2', [newStatus, invoice_id]);

      return res.status(201).json({
        success: true,
        message: 'Ghi nhận thanh toán thành công',
        data: {
          payment: newPayment,
          invoice_id,
          total_paid: totalPaid,
          invoice_status: newStatus
        }
      });
    } else {
      const invoice = db.memoryStore.invoices.find(i => i.id === parseInt(invoice_id, 10));
      if (!invoice) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy hóa đơn' });
      }

      const newId = db.memoryStore.payments.length > 0 ? Math.max(...db.memoryStore.payments.map(p => p.id)) + 1 : 1;
      const payment = {
        id: newId,
        invoice_id: parseInt(invoice_id, 10),
        amount: payAmount,
        method: payMethod,
        paid_at: payTime
      };
      db.memoryStore.payments.push(payment);

      const allPayments = db.memoryStore.payments.filter(p => p.invoice_id === parseInt(invoice_id, 10));
      const totalPaid = allPayments.reduce((sum, p) => sum + Number(p.amount), 0);

      let newStatus = 'PARTIAL';
      if (totalPaid >= Number(invoice.total_amount)) {
        newStatus = 'PAID';
      }
      invoice.status = newStatus;

      return res.status(201).json({
        success: true,
        message: 'Ghi nhận thanh toán thành công',
        data: {
          payment,
          invoice_id,
          total_paid: totalPaid,
          invoice_status: newStatus
        }
      });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi ghi nhận thanh toán', error: error.message });
  }
};
