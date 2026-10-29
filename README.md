# Ứng Dụng Android Quản Lý Nhà Trọ (QLPhongTro)

Dự án ứng dụng di động hỗ trợ **Quản lý nhà trọ chuyên nghiệp** được xây dựng trên nền tảng **Android (Java)** theo kiến trúc chuẩn **MVVM (Model - View - ViewModel)**, tích hợp **RESTful API (Node.js/Express)** và cơ sở dữ liệu quan hệ **PostgreSQL** hỗ trợ đồng bộ đám mây **Neon Cloud**.

---

## 1. Kiến Trúc Hệ Thống (Architecture)

```
[Android Client (Java)]              [RESTful API Backend]               [PostgreSQL Database]
  (MVVM Pattern)                      (Express / Node.js)                   (10 Tables - Neon Cloud)
  ├── View (Activities/Fragments)  <-> ├── Routes & Controllers         <-> ├── users, rooms, tenants
  ├── ViewModel & LiveData         <-> ├── Auth Middleware (JWT)        <-> ├── contracts, contract_tenants
  ├── Repository Pattern           <-> ├── Automatic Billing Engine     <-> ├── services, meter_readings
  └── Retrofit 2 + OkHttp          <-> └── PostgreSQL Client Pool (pg)  <-> └── invoices, items, payments
```

### 1.1. Các tầng trong ứng dụng Android (`com.nhatro.app`):
- **Model (`data/model`)**: Khai báo các thực thể POJO ánh xạ trực tiếp từ 10 bảng CSDL: `User`, `Room`, `Tenant`, `Contract`, `ServiceItem`, `MeterReading`, `Invoice`, `InvoiceItem`, `Payment`, `DashboardStats`, `ChangePasswordRequest`.
- **API (`data/api`)**: `ApiClient` cấu hình Retrofit, Gson Converter, Logging Interceptor, tự động fallback IP (`10.0.2.2` <-> `127.0.0.1`) và `AuthInterceptor` (tự động gắn JWT Bearer Token vào header).
- **Repository (`data/repository`)**: Tầng trung gian quản lý dữ liệu, trả về `LiveData<Resource<T>>` với các trạng thái `LOADING`, `SUCCESS`, `ERROR`.
- **ViewModel (`viewmodel`)**: Lưu trữ và quản lý dữ liệu giao diện có vòng đời độc lập (`AndroidViewModel`), tương tác phản ứng (reactive) với UI.
- **UI & Adapters (`ui/`)**: Các màn hình được thiết kế theo chuẩn **Material Design 3**, tối ưu trải nghiệm người dùng với Card, Chip trạng thái, SwipeRefreshLayout và RecyclerView.

---

## 2. Chi Tiết 10 Bảng Cơ Sở Dữ Liệu PostgreSQL

1. **`users`** (Tài khoản người dùng): `id`, `email`, `password_hash`, `full_name`, `phone`, `role` (OWNER/MANAGER), `created_at`.
2. **`rooms`** (Phòng trọ): `id`, `user_id`, `room_code`, `floor`, `area`, `price` (Check >= 0), `status` (AVAILABLE, OCCUPIED, MAINTENANCE), `note`, `UNIQUE(user_id, room_code)`.
3. **`tenants`** (Khách thuê): `id`, `user_id`, `full_name`, `phone`, `id_card` (Unique CCCD), `birth_date`, `hometown`, `created_at`.
4. **`contracts`** (Hợp đồng thuê): `id`, `user_id`, `room_id`, `tenant_id`, `start_date`, `end_date`, `deposit`, `rent_price`, `status` (ACTIVE, ENDED, CANCELLED).
5. **`contract_tenants`** (Người ở cùng): `contract_id`, `tenant_id` (Composite PK, FKs).
6. **`services`** (Dịch vụ): `id`, `user_id`, `name`, `unit` (kWh, m³, phòng, người), `unit_price`, `is_metered` (boolean).
7. **`meter_readings`** (Chỉ số điện nước): `id`, `user_id`, `room_id`, `service_id`, `month`, `year`, `old_value`, `new_value` (Check new >= old), `UNIQUE(room_id, service_id, month, year)`.
8. **`invoices`** (Hóa đơn hàng tháng): `id`, `user_id`, `contract_id`, `month`, `year`, `total_amount`, `status` (UNPAID, PARTIAL, PAID), `due_date`, `created_at`, `UNIQUE(contract_id, month, year)`.
9. **`invoice_items`** (Chi tiết hóa đơn): `id`, `invoice_id`, `description`, `quantity`, `unit_price`, `amount`.
10. **`payments`** (Lịch sử thanh toán): `id`, `invoice_id`, `amount`, `method` (CASH, TRANSFER), `paid_at`.

---

## 3. Cấu Trúc Thư Mục Dự Án

```
d:\QLPhongtroj\
  ├── database/
  │   ├── schema.sql                     # Script DDL tạo toàn bộ 10 bảng, ràng buộc & Trigger
  │   └── seed.sql                       # Dữ liệu mẫu ban đầu
  ├── backend/                           # RESTful API Server Node.js / Express kết nối PostgreSQL
  │   ├── src/
  │   │   ├── config/db.js               # Kết nối PostgreSQL (Hỗ trợ Neon Cloud SSL + In-Memory fallback)
  │   │   ├── controllers/               # Xử lý logic 10 nghiệp vụ + Đổi mật khẩu
  │   │   ├── middleware/auth.js         # Xác thực JWT Token & phân quyền dữ liệu
  │   │   ├── routes/api.js              # Toàn bộ REST endpoints
  │   │   └── server.js                  # Entry point máy chủ (Tự động khởi tạo .env nếu thiếu)
  │   ├── scripts/
  │   │   ├── setupCloudDb.js            # Khởi tạo bảng và nạp seed data lên Cloud
  │   │   └── migrateLocalToCloud.js     # Đồng bộ toàn bộ dữ liệu máy tính lên Neon Cloud
  │   ├── .env.example                   # Mẫu cấu hình biến môi trường kết nối Cloud
  │   └── package.json
  ├── app/                               # Ứng dụng Android Java (MVVM)
  │   ├── src/main/java/com/nhatro/app/
  │   │   ├── data/
  │   │   │   ├── api/                   # ApiClient, ApiService, AuthInterceptor
  │   │   │   ├── model/                 # Các POJO Models (Room, Tenant, Contract, Invoice...)
  │   │   │   └── repository/            # 9 Repositories + Resource<T>
  │   │   ├── viewmodel/                 # 9 ViewModels quản lý trạng thái LiveData
  │   │   ├── ui/
  │   │   │   ├── auth/                  # LoginActivity, RegisterActivity
  │   │   │   ├── main/                  # MainActivity điều hướng BottomNavigation
  │   │   │   ├── dashboard/             # DashboardFragment (KPI doanh thu, phòng, nợ)
  │   │   │   ├── rooms/                 # RoomListFragment, FloorPlanFragment, AddEditRoom, RoomDetail
  │   │   │   ├── tenants/               # TenantListFragment, AddEditTenant
  │   │   │   ├── contracts/             # ContractListFragment, AddEditContract, Detail
  │   │   │   ├── services/              # ServiceListActivity, AddEditService
  │   │   │   ├── meter/                 # MeterReadingListActivity, AddMeterReading
  │   │   │   ├── invoices/              # InvoiceListFragment, CreateInvoice, Detail
  │   │   │   ├── settings/              # SettingsActivity (Cài đặt, Tài khoản VietQR, Đổi mật khẩu)
  │   │   │   └── adapters/              # RecyclerView Adapters + FloorPlanAdapter
  │   │   └── utils/                     # CurrencyUtils (VND), DateUtils, Constants, SharedPrefs
  │   └── src/main/res/                  # XML Layouts, Material 3 Colors, Vector Icons, Styles
  ├── build.gradle                       # Root Gradle
  ├── settings.gradle
  └── gradlew / gradlew.bat
```

---

## 4. Các Tính Năng Nổi Bật Đã Cài Đặt Hoàn Thiện

- 📊 **Dashboard Thống Kê Thông Minh**: Đo lường thời gian thực số lượng phòng trống/đang thuê/bảo trì, doanh thu đã thu và tổng dư nợ chưa thanh toán theo tháng.
- 🏢 **Sơ Đồ Phòng Theo Tầng (Floor Plan Grid)**:
  - Hiển thị trực quan lưới ô vuông phân tầng (Tầng 1, Tầng 2, Tầng 3...).
  - Tô màu trực quan theo trạng thái: **Xanh lá** (Còn trống), **Xanh dương** (Đang thuê), **Cam** (Bảo trì).
  - Chạm vào từng ô để mở nhanh xem chi tiết phòng.
  - Hỗ trợ chuyển đổi mượt mà giữa chế độ **Sơ đồ tầng** và **Danh sách phòng**.
- ⚙️ **Trang Cài Đặt & Tài Khoản Chuyên Nghiệp**:
  - Thông tin chủ nhà, ảnh đại diện, vai trò quản trị.
  - **Tài khoản nhận tiền (VietQR)**: Cấu hình tên ngân hàng, số tài khoản, tên chủ thẻ để tạo mã QR chuyển khoản nhanh trên hóa đơn gửi khách.
  - Quản lý bảng giá dịch vụ và truy cập nhanh ghi số điện nước.
  - **Đổi mật khẩu tài khoản**: Hỗ trợ đổi mật khẩu bảo mật trực tiếp trên ứng dụng kèm kiểm tra tính hợp lệ tức thời.
  - Cấu hình linh hoạt URL máy chủ API và đăng xuất tài khoản an toàn.
- 🏠 **Quản Lý Phòng Trọ (rooms)**: Lọc theo trạng thái, xem chi tiết khách đang ở, tạo hợp đồng trực tiếp từ phòng trống.
- 👥 **Quản Lý Khách Thuê (tenants)**: Tìm kiếm tức thời theo tên, số điện thoại, CCCD.
- 📑 **Hợp Đồng & Người Ở Cùng (contracts & contract_tenants)**: Quản lý thời hạn, tiền cọc, giá thuê, người ở cùng, thanh lý hợp đồng tự động giải phóng phòng.
- ⚡ **Quản Lý Dịch Vụ & Đo Chỉ Số Điện Nước (services & meter_readings)**: Tự động tải chỉ số cũ từ tháng trước, kiểm tra tính hợp lệ `Chỉ số mới >= Chỉ số cũ`, tính tiền điện nước tự động.
- 💵 **Hóa Đơn & Thu Chi Tự Động (invoices & invoice_items)**: Tính tiền tự động với 1 chạm (gom Tiền phòng + Điện + Nước theo chỉ số tiêu thụ + Dịch vụ cố định), tính nợ còn lại.
- 💳 **Ghi Nhận Thanh Toán (payments)**: Hỗ trợ thanh toán Tiền mặt (CASH) hoặc Chuyển khoản (TRANSFER), tự động cập nhật trạng thái hóa đơn (*UNPAID -> PARTIAL -> PAID*).
- ☁️ **Đồng Bộ Đám Mây Neon Cloud (PostgreSQL Singapore)**: Mọi dữ liệu phòng, khách thuê, hóa đơn được lưu trữ tập trung trên đám mây, giúp **đồng bộ 100% thời gian thực giữa nhiều máy tính và thiết bị di động**.

---

## 5. Hướng Dẫn Cài Đặt & Chạy Hệ Thống

### Bước 1: Khởi động RESTful API Backend
- **Chạy bằng file batch (1-Click)**: Nhấp đúp chuột vào file `KHOI_DONG_SERVER.bat` tại thư mục gốc. File sẽ tự động liên kết cổng và khởi động API Server.
- **Hoặc chạy thủ công qua Terminal**:
  ```bash
  cd backend
  npm install
  npm start
  ```
  *(Backend đã được tích hợp sẵn kết nối Cloud Database, khi mang sang máy khác chạy sẽ **tự động kết nối và có sẵn dữ liệu** mà không cần cài đặt PostgreSQL).*

### Bước 2: Chạy Ứng Dụng Android
1. **Mở dự án trong Android Studio**:
   - Khởi động **Android Studio**, chọn **Open** và duyệt đến thư mục dự án `QLPhongtroj`.
   - Chờ Gradle đồng bộ các thư viện.
2. **Chạy ứng dụng**:
   - Chọn thiết bị máy ảo (Android Emulator) hoặc điện thoại thật.
   - Nhấn **Run 'app'** (hoặc phím tắt `Shift + F10`).
   - File APK bản Debug được xuất tại: `app/build/outputs/apk/debug/app-debug.apk`.
3. **Cấu hình máy chủ API trên ứng dụng**:
   - Mặc định kết nối: `http://10.0.2.2:3000/api/` (Android Emulator) hoặc `http://127.0.0.1:3000/api/`.
   - Nếu chạy trên điện thoại thật qua mạng Wi-Fi: Vào trang **Cài đặt & Tài khoản** -> chọn **Địa chỉ máy chủ (Server API)** -> nhập IP máy tính chạy backend (VD: `http://192.168.1.15:3000/api/`).

---

## 6. Tài Khoản Đăng Nhập Mặc Định

| Vai trò | Email đăng nhập | Mật khẩu mặc định | Quyền hạn |
| :--- | :--- | :---: | :--- |
| **Chủ trọ / Quản trị (OWNER)** | `admin@nhatro.vn` | `123456` | Toàn quyền quản lý phòng, khách thuê, giá dịch vụ, hóa đơn, tài khoản |
| **Quản lý (MANAGER)** | `quanly@nhatro.vn` | `123456` | Ghi chỉ số điện nước, lập hóa đơn, thu tiền, xem báo cáo |
