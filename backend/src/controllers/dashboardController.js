const db = require('../config/db');

exports.getStats = async (req, res) => {
  try {
    if (db.isConnected) {
      const roomCountsRes = await db.query(`
        SELECT 
          COUNT(*) as total_rooms,
          COUNT(*) FILTER (WHERE status = 'AVAILABLE') as available_rooms,
          COUNT(*) FILTER (WHERE status = 'OCCUPIED') as occupied_rooms,
          COUNT(*) FILTER (WHERE status = 'MAINTENANCE') as maintenance_rooms
        FROM rooms
      `);

      const tenantCountRes = await db.query('SELECT COUNT(*) as total_tenants FROM tenants');
      const activeContractsRes = await db.query("SELECT COUNT(*) as active_contracts FROM contracts WHERE status = 'ACTIVE'");

      const invoicesRes = await db.query(`
        SELECT 
          COUNT(*) as total_invoices,
          COUNT(*) FILTER (WHERE status = 'PAID') as paid_invoices,
          COUNT(*) FILTER (WHERE status = 'UNPAID') as unpaid_invoices,
          COUNT(*) FILTER (WHERE status = 'PARTIAL') as partial_invoices,
          COALESCE(SUM(total_amount), 0) as total_invoiced_amount
        FROM invoices
      `);

      const paymentsRes = await db.query('SELECT COALESCE(SUM(amount), 0) as total_revenue FROM payments');

      const roomCounts = roomCountsRes.rows[0];
      const invCounts = invoicesRes.rows[0];
      const totalRevenue = Number(paymentsRes.rows[0].total_revenue);
      const totalInvoiced = Number(invCounts.total_invoiced_amount);
      const unpaidDebt = Math.max(0, totalInvoiced - totalRevenue);

      return res.json({
        success: true,
        data: {
          total_rooms: Number(roomCounts.total_rooms),
          available_rooms: Number(roomCounts.available_rooms),
          occupied_rooms: Number(roomCounts.occupied_rooms),
          maintenance_rooms: Number(roomCounts.maintenance_rooms),
          total_tenants: Number(tenantCountRes.rows[0].total_tenants),
          active_contracts: Number(activeContractsRes.rows[0].active_contracts),
          total_invoices: Number(invCounts.total_invoices),
          paid_invoices: Number(invCounts.paid_invoices),
          unpaid_invoices: Number(invCounts.unpaid_invoices),
          partial_invoices: Number(invCounts.partial_invoices),
          total_revenue: totalRevenue,
          unpaid_debt: unpaidDebt
        }
      });
    } else {
      const rooms = db.memoryStore.rooms;
      const tenants = db.memoryStore.tenants;
      const contracts = db.memoryStore.contracts;
      const invoices = db.memoryStore.invoices;
      const payments = db.memoryStore.payments;

      const totalRevenue = payments.reduce((sum, p) => sum + Number(p.amount), 0);
      const totalInvoiced = invoices.reduce((sum, i) => sum + Number(i.total_amount), 0);
      const unpaidDebt = Math.max(0, totalInvoiced - totalRevenue);

      return res.json({
        success: true,
        data: {
          total_rooms: rooms.length,
          available_rooms: rooms.filter(r => r.status === 'AVAILABLE').length,
          occupied_rooms: rooms.filter(r => r.status === 'OCCUPIED').length,
          maintenance_rooms: rooms.filter(r => r.status === 'MAINTENANCE').length,
          total_tenants: tenants.length,
          active_contracts: contracts.filter(c => c.status === 'ACTIVE').length,
          total_invoices: invoices.length,
          paid_invoices: invoices.filter(i => i.status === 'PAID').length,
          unpaid_invoices: invoices.filter(i => i.status === 'UNPAID').length,
          partial_invoices: invoices.filter(i => i.status === 'PARTIAL').length,
          total_revenue: totalRevenue,
          unpaid_debt: unpaidDebt
        }
      });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi lấy dữ liệu thống kê', error: error.message });
  }
};
