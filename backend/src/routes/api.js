const express = require('express');
const router = express.Router();
const auth = require('../middleware/auth');

const authController = require('../controllers/authController');
const roomController = require('../controllers/roomController');
const tenantController = require('../controllers/tenantController');
const contractController = require('../controllers/contractController');
const serviceController = require('../controllers/serviceController');
const meterReadingController = require('../controllers/meterReadingController');
const invoiceController = require('../controllers/invoiceController');
const paymentController = require('../controllers/paymentController');
const dashboardController = require('../controllers/dashboardController');

// 1. Auth routes
router.post('/auth/login', authController.login);
router.post('/auth/register', authController.register);
router.get('/auth/profile', auth, authController.getProfile);
router.put('/auth/change-password', auth, authController.changePassword);

// 2. Dashboard & Reports routes
router.get('/dashboard/stats', auth, dashboardController.getStats);
router.get('/dashboard/summary', auth, dashboardController.getStats);
router.get('/reports/revenue', auth, dashboardController.getMonthlyRevenue);
router.get('/dashboard/revenue', auth, dashboardController.getMonthlyRevenue);

// 3. Rooms routes
router.get('/rooms', auth, roomController.getRooms);
router.get('/rooms/:id', auth, roomController.getRoomById);
router.post('/rooms', auth, roomController.createRoom);
router.put('/rooms/:id', auth, roomController.updateRoom);
router.delete('/rooms/:id', auth, roomController.deleteRoom);

// 4. Tenants routes
router.get('/tenants', auth, tenantController.getTenants);
router.get('/tenants/:id', auth, tenantController.getTenantById);
router.post('/tenants', auth, tenantController.createTenant);
router.put('/tenants/:id', auth, tenantController.updateTenant);
router.delete('/tenants/:id', auth, tenantController.deleteTenant);

// 5. Contracts routes
router.get('/contracts', auth, contractController.getContracts);
router.get('/contracts/:id', auth, contractController.getContractById);
router.post('/contracts', auth, contractController.createContract);
router.put('/contracts/:id', auth, contractController.updateContract);
router.put('/contracts/:id/end', auth, contractController.endContract);
router.put('/contracts/:id/extend', auth, contractController.extendContract);
router.post('/contracts/:id/roommates', auth, contractController.addRoommate);
router.delete('/contracts/:id/roommates/:tenantId', auth, contractController.removeRoommate);

// 6. Services routes
router.get('/services', auth, serviceController.getServices);
router.post('/services', auth, serviceController.createService);
router.put('/services/:id', auth, serviceController.updateService);
router.delete('/services/:id', auth, serviceController.deleteService);

// 7. Meter readings routes (Điện/Nước)
router.get('/meter-readings', auth, meterReadingController.getMeterReadings);
router.get('/meter-readings/latest', auth, meterReadingController.getLatestReading);
router.post('/meter-readings', auth, meterReadingController.createMeterReading);
router.put('/meter-readings/:id', auth, meterReadingController.updateMeterReading);
router.delete('/meter-readings/:id', auth, meterReadingController.deleteMeterReading);

// 8. Invoices routes
router.get('/invoices', auth, invoiceController.getInvoices);
router.get('/invoices/:id', auth, invoiceController.getInvoiceById);
router.post('/invoices', auth, invoiceController.createInvoice);
router.post('/invoices/generate-monthly', auth, invoiceController.generateMonthlyInvoice);
router.post('/invoices/bulk-generate', auth, invoiceController.bulkGenerateInvoices);
router.post('/invoices/:id/items', auth, invoiceController.addInvoiceItem);
router.delete('/invoices/:id/items/:itemId', auth, invoiceController.removeInvoiceItem);
router.delete('/invoices/:id', auth, invoiceController.deleteInvoice);

// 9. Payments routes
router.get('/payments', auth, paymentController.getPayments);
router.post('/payments', auth, paymentController.createPayment);

module.exports = router;
