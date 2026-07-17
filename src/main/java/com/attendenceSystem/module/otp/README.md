# OTP Module

## Overview
Module OTP là module hỗ trợ (support module) xử lý sinh, gửi và xác minh mã OTP (One-Time Password) cho các mục đích:
- Quên mật khẩu (FORGOT_PASSWORD)
- Đăng ký tài khoản (REGISTER)
- Thay đổi email (CHANGE_EMAIL)

Module này được các module khác (ví dụ: user module) gọi thông qua REST API.

## Cấu trúc

### DTOs

#### Request (Class)
- `SendOtpRequest` - Yêu cầu gửi OTP
  - destination: email/số điện thoại
  - purpose: mục đích sử dụng OTP

- `VerifyOtpRequest` - Yêu cầu xác minh OTP
  - destination: email/số điện thoại
  - code: mã OTP
  - purpose: mục đích sử dụng OTP

#### Response (Record)
- `OtpResponse` - Thông tin OTP đã tạo
  - id: định danh OTP
  - destination: email/số điện thoại
  - purpose: mục đích
  - expiredAt: thời gian hết hạn
  - createdAt: thời gian tạo
  - used: đã sử dụng hay chưa

### Entities
- `Otp` - Entity lưu trữ OTP trong database
- `OtpPurpose` - Enum định nghĩa các mục đích sử dụng OTP

### Services
- `OptService` - Interface chính
- `OtpSender` - Interface gửi OTP
- `OtpServiceImpl` - Implementation chính
- `OtpSenderImpl` - Implementation gửi OTP (stub)

### Repositories
- `OtpRepository` - JPA Repository với custom methods

### Controllers
- `OtpApiController` - REST API endpoints (chỉ có REST API, không có MVC controller)

### Mappers
- `SendOtpRequestMapper` - Chuyển SendOtpRequest → Otp entity
- `OtpResponseMapper` - Chuyển Otp entity → OtpResponse

## Endpoints

### REST API
- `POST /api/otp/send` - Gửi OTP
- `POST /api/otp/verify` - Xác minh OTP

## Cấu hình
- OTP có độ dài 6 số
- Thời gian hết hạn: 5 phút
- Mã OTP được tạo ngẫu nhiên

## Luồng hoạt động

### Gửi OTP
1. Nhận request với destination và purpose
2. Tạo mã OTP ngẫu nhiên 6 số
3. Lưu OTP vào database (status: unused)
4. Gửi OTP qua channel phù hợp (email/SMS)
5. Trả về thông tin OTP

### Xác minh OTP
1. Tìm OTP mới nhất cho destination và purpose (chưa sử dụng)
2. Kiểm tra OTP có hết hạn không
3. So sánh mã OTP
4. Đánh dấu OTP đã sử dụng
5. Trả về thông tin OTP đã xác minh

## Patterns
- Request DTOs: Class
- Response DTOs: Record
- Mapper: Static methods
- Service: Constructor injection với Lombok @RequiredArgsConstructor