# Ứng Dụng Android Quản Lý Nhà Trọ (QLPhongTro)

Dự án ứng dụng di động hỗ trợ **Quản lý nhà trọ** được xây dựng trên nền tảng **Android (Java)** theo kiến trúc chuẩn **MVVM (Model - View - ViewModel)**, tích hợp **RESTful API** và cơ sở dữ liệu quan hệ **PostgreSQL**.

---

## 1. Kiến Trúc Hệ Thống (Architecture)

```
[Android Client (Java)]              [RESTful API Backend]               [PostgreSQL Database]
  (MVVM Pattern)                      (Express / Node.js)                   (10 Tables)
  ├── View (Activities/Fragments)  <-> ├── Routes & Controllers         <-> ├── users, rooms, tenants
  ├── ViewModel & LiveData         <-> ├── Auth Middleware (JWT)        <-> ├── contracts, contract_tenants
  ├── Repository Pattern           <-> ├── Automatic Billing Engine     <-> ├── services, meter_readings
  └── Retrofit 2 + OkHttp          <-> └── PostgreSQL Client Pool (pg)  <-> └── invoices, items, payments
```

### 1.1. Các tầng trong ứng dụng Android (com.nhatro.app):
- **Model (`data/model`)**: Khai báo các thực thể POJO ánh xạ trực tiếp từ 10 bảng CSDL: `User`, `Room`, `Tenant`, `Contract`, `ServiceItem`, `MeterReading`, `Invoice`, `InvoiceItem`, `Payment`, `DashboardStats`.
- **API (`data/api`)**: `ApiClient` cấu hình Retrofit, Gson Converter, Logging Interceptor và `AuthInterceptor` (tự động gắn JWT Bearer Token vào header).
- **Repository (`data/repository`)**: Tầng trung gian quản lý dữ liệu, trả về `LiveData<Resource<T>>` với các trạng thái `LOADING`, `SUCCESS`, `ERROR`.
- **ViewModel (`viewmodel`)**: Lưu trữ và quản lý dữ liệu giao diện có vòng đời độc lập (`AndroidViewModel`), tương tác phản ứng (reactive) với UI.
- **UI & Adapters (`ui/`)**: Các màn hình được thiết kế theo chuẩn **Material Design 3**, tối ưu trải nghiệm người dùng với Card, Chip trạng thái, SwipeRefreshLayout và RecyclerView.

---

## 2. Chi Tiết 10 Bảng Cơ Sở Dữ Liệu PostgreSQL

1. **`users`** (Tài khoản người dùng): `id`, `email`, `password_hash`, `full_name`, `phone`, `role` (OWNER/MANAGER), `created_at`.
2. **`rooms`** (Phòng trọ): `id`, `room_code` (Unique), `floor`, `area`, `price` (Check >= 0), `status` (AVAILABLE, OCCUPIED, MAINTENANCE), `note`.
3. **`tenants`** (Khách thuê): `id`, `full_name`, `phone`, `id_card` (Unique CCCD), `birth_date`, `hometown`, `created_at`.
4. **`contracts`** (Hợp đồng thuê): `id`, `room_id`, `tenant_id`, `start_date`, `end_date`, `deposit`, `rent_price`, `status` (ACTIVE, ENDED, CANCELLED).
5. **`contract_tenants`** (Người ở cùng): `contract_id`, `tenant_id` (Composite PK, FKs).
6. **`services`** (Dịch vụ): `id`, `name`, `unit` (kWh, m³, phòng, người), `unit_price`, `is_metered` (boolean).
7. **`meter_readings`** (Chỉ số điện nước): `id`, `room_id`, `service_id`, `month`, `year`, `old_value`, `new_value` (Check new >= old), `UNIQUE(room_id, service_id, month, year)`.
8. **`invoices`** (Hóa đơn hàng tháng): `id`, `contract_id`, `month`, `year`, `total_amount`, `status` (UNPAID, PARTIAL, PAID), `due_date`, `created_at`, `UNIQUE(contract_id, month, year)`.
9. **`invoice_items`** (Chi tiết hóa đơn): `id`, `invoice_id`, `description`, `quantity`, `unit_price`, `amount`.
10. **`payments`** (Lịch sử thanh toán): `id`, `invoice_id`, `amount`, `method` (CASH, TRANSFER), `paid_at`.

---

## 3. Cấu Trúc Thư Mục Dự Án

```
d:\QLPhongtroj\
  ├── database/
  │   ├── schema.sql                     # Script DDL tạo toàn bộ 10 bảng, ràng buộc & Trigger
  │   └── seed.sql                       # Dữ liệu mẫu ban đầu (phòng, khách, hợp đồng, hóa đơn...)
  ├── backend/                           # RESTful API Server Node.js / Express kết nối PostgreSQL
  │   ├── src/
  │   │   ├── config/db.js               # Kết nối PostgreSQL + In-Memory fallback mode
  │   │   ├── controllers/               # Xử lý logic 10 nghiệp vụ
  │   │   ├── middleware/auth.js         # Xác thực JWT Token
  │   │   ├── routes/api.js              # REST endpoints
  │   │   ├── scripts/migrate.js         # Tự động chạy schema.sql và seed.sql lên PostgreSQL
  │   │   └── server.js                  # Entry point máy chủ (Port 3000)
  │   ├── .env                           # Cấu hình cổng, DB PostgreSQL và Secret key
  │   └── package.json
  ├── app/                               # Ứng dụng Android Java (MVVM)
  │   ├── src/main/java/com/nhatro/app/
  │   │   ├── data/
  │   │   │   ├── api/                   # ApiClient, ApiService, AuthInterceptor
  │   │   │   ├── model/                 # 15 POJO Models (Room, Tenant, Contract, Invoice...)
  │   │   │   └── repository/            # 9 Repositories + Resource<T>
  │   │   ├── viewmodel/                 # 9 ViewModels quản lý trạng thái LiveData
  │   │   ├── ui/
  │   │   │   ├── auth/                  # LoginActivity, cấu hình URL máy chủ
  │   │   │   ├── main/                  # MainActivity điều hướng BottomNavigation
  │   │   │   ├── dashboard/             # DashboardFragment (KPI doanh thu, phòng, nợ)
  │   │   │   ├── rooms/                 # RoomListFragment, AddEditRoom, RoomDetail
  │   │   │   ├── tenants/               # TenantListFragment, AddEditTenant
  │   │   │   ├── contracts/             # ContractListFragment, AddEditContract, Detail
  │   │   │   ├── services/              # ServiceListActivity, AddEditService
  │   │   │   ├── meter/                 # MeterReadingListActivity, AddMeterReading
  │   │   │   ├── invoices/              # InvoiceListFragment, CreateInvoice, Detail
  │   │   │   └── adapters/              # 8 RecyclerView Adapters
  │   │   └── utils/                     # CurrencyUtils (VND), DateUtils, Constants, SharedPrefs
  │   └── src/main/res/                  # XML Layouts, Material 3 Colors, Menu, Styles
  ├── build.gradle                       # Root Gradle
  ├── settings.gradle
  └── gradlew / gradlew.bat
```

---

## 4. Hướng Dẫn Cài Đặt & Chạy Hệ Thống

### Bước 1: Khởi động RESTful API Backend
- **Cách nhanh nhất (1-Click)**: Nhấp đúp chuột vào file `KHOI_DONG_SERVER.bat` tại thư mục gốc `d:\QLPhongtroj`. File này sẽ tự động:
  1. Khởi động PostgreSQL 16 trong WSL.
  2. Kết nối cổng 3000 cho máy ảo Android Emulator (`adb reverse`).
  3. Khởi động API Server Node.js kết nối trực tiếp đến PostgreSQL.

- **Hoặc chạy thủ công qua Terminal**:
  ```bash
  cd d:\QLPhongtroj\backend
  npm start
  ```

### Bước 2: Chạy Ứng Dụng Android
1. **Mở dự án**:
   - Khởi động **Android Studio**.
   - Chọn **Open** và duyệt đến thư mục `d:\QLPhongtroj`.
   - Android Studio sẽ tự động đồng bộ Gradle và nhận diện module `:app`.

2. **Chạy ứng dụng**:
   - Chọn thiết bị máy ảo (Android Emulator) hoặc điện thoại thật.
   - Nhấn nút **Run 'app'** (hoặc `Shift + F10`).
   - File APK đã được biên dịch sẵn tại: `app/build/outputs/apk/debug/app-debug.apk`.

3. **Cấu hình địa chỉ API trên ứng dụng**:
   - Mặc định ứng dụng kết nối tới: `http://10.0.2.2:3000/api/` (Dành cho Android Emulator).
   - Nếu chạy trên điện thoại thật qua mạng Wi-Fi: Tại màn hình Đăng nhập (hoặc menu trên góc phải màn hình chính), chọn **Cấu hình máy chủ API** và nhập IP máy tính, ví dụ: `http://192.168.1.15:3000/api/`.

### Bước 3: Tài khoản đăng nhập thử nghiệm
- **Chủ nhà trọ (OWNER)**:
  - Email: `admin@nhatro.vn`
  - Mật khẩu: `123456`
- **Quản lý (MANAGER)**:
  - Email: `quanly@nhatro.vn`
  - Mật khẩu: `123456`

---

## 5. Các Tính Năng Nổi Bật Đã Cài Đặt Hoàn Thiện

- 📊 **Dashboard Thống Kê**: Đo lường thời gian thực số lượng phòng trống/đang thuê/bảo trì, doanh thu đã thu và tổng dư nợ chưa thanh toán.
- 🏠 **Quản Lý Phòng Trọ (rooms)**: Lọc theo trạng thái (*Còn trống, Đang thuê, Bảo trì*), xem chi tiết khách đang ở, tạo hợp đồng trực tiếp từ phòng trống.
- 👥 **Quản Lý Khách Thuê (tenants)**: Tìm kiếm tức thời theo tên, số điện thoại, CCCD.
- 📑 **Hợp Đồng & Người Ở Cùng (contracts & contract_tenants)**: Quản lý ngày bắt đầu/kết thúc, tiền cọc, tiền thuê, thêm/xóa người ở cùng, thanh lý hợp đồng (tự động chuyển phòng về trạng thái trống).
- ⚡ **Quản Lý Dịch Vụ & Đo Chỉ Số Điện Nước (services & meter_readings)**: Tự động tải chỉ số cũ từ tháng trước, kiểm tra tính hợp lệ `Chỉ số mới >= Chỉ số cũ`, tính tiền tức thời.
- 💵 **Hóa Đơn & Thu Chi Tự Động (invoices & invoice_items)**: Tính tiền tự động chỉ với 1 chạm (gom Tiền phòng + Điện + Nước theo chỉ số tiêu thụ + Dịch vụ cố định), tính nợ còn lại.
- 💳 **Ghi Nhận Thanh Toán (payments)**: Hỗ trợ thanh toán Tiền mặt (CASH) hoặc Chuyển khoản (TRANSFER), tự động cập nhật trạng thái hóa đơn (*UNPAID -> PARTIAL -> PAID*).
