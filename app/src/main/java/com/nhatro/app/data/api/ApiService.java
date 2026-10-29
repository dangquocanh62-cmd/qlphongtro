package com.nhatro.app.data.api;

import com.nhatro.app.data.model.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // 1. Authentication
    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<ApiResponse<LoginResponse>> register(@Body RegisterRequest request);

    @GET("auth/profile")
    Call<ApiResponse<User>> getProfile();

    // 2. Dashboard Stats & Reports
    @GET("dashboard/stats")
    Call<ApiResponse<DashboardStats>> getDashboardStats(@Query("month") Integer month, @Query("year") Integer year);

    @GET("reports/revenue")
    Call<ApiResponse<List<MonthlyRevenue>>> getMonthlyRevenue(@Query("months") int months);

    // 3. Rooms
    @GET("rooms")
    Call<ApiResponse<List<Room>>> getRooms(@Query("status") String status, @Query("q") String query);

    @GET("rooms/{id}")
    Call<ApiResponse<Room>> getRoomById(@Path("id") Long id);

    @POST("rooms")
    Call<ApiResponse<Room>> createRoom(@Body Room room);

    @PUT("rooms/{id}")
    Call<ApiResponse<Room>> updateRoom(@Path("id") Long id, @Body Room room);

    @DELETE("rooms/{id}")
    Call<ApiResponse<Void>> deleteRoom(@Path("id") Long id);

    // 4. Tenants
    @GET("tenants")
    Call<ApiResponse<List<Tenant>>> getTenants(@Query("q") String query);

    @GET("tenants/{id}")
    Call<ApiResponse<Tenant>> getTenantById(@Path("id") Long id);

    @POST("tenants")
    Call<ApiResponse<Tenant>> createTenant(@Body Tenant tenant);

    @PUT("tenants/{id}")
    Call<ApiResponse<Tenant>> updateTenant(@Path("id") Long id, @Body Tenant tenant);

    @DELETE("tenants/{id}")
    Call<ApiResponse<Void>> deleteTenant(@Path("id") Long id);

    // 5. Contracts
    @GET("contracts")
    Call<ApiResponse<List<Contract>>> getContracts(
            @Query("status") String status,
            @Query("roomId") Long roomId,
            @Query("tenantId") Long tenantId,
            @Query("expiring") Boolean expiring
    );

    @GET("contracts/{id}")
    Call<ApiResponse<Contract>> getContractById(@Path("id") Long id);

    @POST("contracts")
    Call<ApiResponse<Contract>> createContract(@Body Map<String, Object> body);

    @PUT("contracts/{id}")
    Call<ApiResponse<Contract>> updateContract(@Path("id") Long id, @Body Contract contract);

    @PUT("contracts/{id}/end")
    Call<ApiResponse<Contract>> endContract(@Path("id") Long id);

    @PUT("contracts/{id}/extend")
    Call<ApiResponse<Contract>> extendContract(@Path("id") Long id, @Body Map<String, Object> body);

    @POST("contracts/{id}/roommates")
    Call<ApiResponse<Void>> addRoommate(@Path("id") Long id, @Body Map<String, Object> body);

    @DELETE("contracts/{id}/roommates/{tenantId}")
    Call<ApiResponse<Void>> removeRoommate(@Path("id") Long id, @Path("tenantId") Long tenantId);

    // 6. Services
    @GET("services")
    Call<ApiResponse<List<ServiceItem>>> getServices();

    @POST("services")
    Call<ApiResponse<ServiceItem>> createService(@Body ServiceItem service);

    @PUT("services/{id}")
    Call<ApiResponse<ServiceItem>> updateService(@Path("id") Long id, @Body ServiceItem service);

    @DELETE("services/{id}")
    Call<ApiResponse<Void>> deleteService(@Path("id") Long id);

    // 7. Meter Readings
    @GET("meter-readings")
    Call<ApiResponse<List<MeterReading>>> getMeterReadings(
            @Query("roomId") Long roomId,
            @Query("serviceId") Long serviceId,
            @Query("month") Integer month,
            @Query("year") Integer year
    );

    @GET("meter-readings/latest")
    Call<ApiResponse<MeterReading>> getLatestReading(
            @Query("roomId") Long roomId,
            @Query("serviceId") Long serviceId
    );

    @POST("meter-readings")
    Call<ApiResponse<MeterReading>> createMeterReading(@Body MeterReading reading);

    @PUT("meter-readings/{id}")
    Call<ApiResponse<MeterReading>> updateMeterReading(@Path("id") Long id, @Body MeterReading reading);

    @DELETE("meter-readings/{id}")
    Call<ApiResponse<Void>> deleteMeterReading(@Path("id") Long id);

    // 8. Invoices
    @GET("invoices")
    Call<ApiResponse<List<Invoice>>> getInvoices(
            @Query("status") String status,
            @Query("contractId") Long contractId,
            @Query("month") Integer month,
            @Query("year") Integer year
    );

    @GET("invoices/{id}")
    Call<ApiResponse<Invoice>> getInvoiceById(@Path("id") Long id);

    @POST("invoices")
    Call<ApiResponse<Invoice>> createInvoice(@Body CreateInvoiceRequest request);

    @POST("invoices/generate-monthly")
    Call<ApiResponse<Invoice>> generateMonthlyInvoice(@Body Map<String, Object> body);

    @POST("invoices/bulk-generate")
    Call<ApiResponse<Map<String, Object>>> bulkGenerateInvoices(@Body Map<String, Object> body);

    @POST("invoices/{id}/items")
    Call<ApiResponse<InvoiceItem>> addInvoiceItem(@Path("id") Long invoiceId, @Body Map<String, Object> body);

    @DELETE("invoices/{id}/items/{itemId}")
    Call<ApiResponse<Void>> removeInvoiceItem(@Path("id") Long invoiceId, @Path("itemId") Long itemId);

    @DELETE("invoices/{id}")
    Call<ApiResponse<Void>> deleteInvoice(@Path("id") Long id);

    // 9. Payments
    @GET("payments")
    Call<ApiResponse<List<Payment>>> getPayments(@Query("invoiceId") Long invoiceId);

    @POST("payments")
    Call<ApiResponse<Map<String, Object>>> createPayment(@Body Payment payment);
}
