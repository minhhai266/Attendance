<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng Ký - Face ID Attendance</title>
    <link rel="stylesheet" href="css/theme.css">
    <link rel="stylesheet" href="css/components.css">
</head>
<body>
    <div class="min-h-screen flex items-center justify-center" style="background-color: var(--background); padding: 1.5rem;">
        <div class="card" style="max-width: 500px; width: 100%;">
            <div class="card-header text-center">
                <h1 class="card-title" style="font-size: 2rem;">Đăng Ký Tài Khoản</h1>
                <p class="card-description">Tạo tài khoản mới để sử dụng hệ thống</p>
            </div>

            <div class="card-body">
                <form action="register" method="post" id="registerForm">
                    <div class="form-group">
                        <label class="form-label">Họ và tên *</label>
                        <input type="text" name="name" class="form-control" placeholder="Nguyễn Văn A" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Email *</label>
                        <input type="email" name="email" class="form-control" placeholder="your.email@company.com" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Số điện thoại *</label>
                        <input type="tel" name="phone" class="form-control" placeholder="0901234567" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Phòng ban *</label>
                        <select name="department" class="form-control" required>
                            <option value="">Chọn phòng ban</option>
                            <option value="IT">IT</option>
                            <option value="HR">HR</option>
                            <option value="Marketing">Marketing</option>
                            <option value="Finance">Finance</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Mật khẩu *</label>
                        <input type="password" name="password" id="password" class="form-control" placeholder="••••••••" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Xác nhận mật khẩu *</label>
                        <input type="password" name="confirmPassword" class="form-control" placeholder="••••••••" required>
                    </div>

                    <div class="alert alert-info" style="font-size: 0.875rem; margin-top: 1rem;">
                        <strong>Lưu ý:</strong> Tài khoản của bạn sẽ cần được Admin phê duyệt trước khi có thể đăng nhập.
                    </div>

                    <button type="submit" class="btn btn-primary w-full" style="width: 100%; margin-top: 1.5rem;">
                        <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                            <circle cx="8.5" cy="7" r="4"></circle>
                            <line x1="20" y1="8" x2="20" y2="14"></line>
                            <line x1="23" y1="11" x2="17" y2="11"></line>
                        </svg>
                        Đăng ký
                    </button>
                </form>
            </div>

            <div class="card-footer text-center">
                <p class="text-sm text-muted">
                    Đã có tài khoản? 
                    <a href="login.jsp" class="text-primary font-semibold">Đăng nhập</a>
                </p>
                <p class="text-sm text-muted" style="margin-top: 0.5rem;">
                    <a href="index.jsp" class="text-muted">← Về trang chủ</a>
                </p>
            </div>
        </div>
    </div>

    <script src="js/main.js"></script>
</body>
</html>
