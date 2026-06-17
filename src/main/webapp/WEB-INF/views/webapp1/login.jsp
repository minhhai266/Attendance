<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html lang="vi">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng Nhập - Face ID Attendance</title>
        <link rel="stylesheet" href="css/theme.css">
        <link rel="stylesheet" href="css/components.css">
    </head>

    <body>
        <div class="min-h-screen flex items-center justify-center"
            style="background-color: var(--background); padding: 1.5rem;">
            <div class="card" style="max-width: 450px; width: 100%;">
                <div class="card-header text-center">
                    <h1 class="card-title" style="font-size: 2rem;">Đăng Nhập</h1>
                    <p class="card-description">Nhập thông tin để truy cập hệ thống</p>
                </div>

                <div class="card-body">
                    <% String error=request.getParameter("error"); if (error !=null) { %>
                        <div class="alert alert-destructive mb-4">
                            Sai email hoặc mật khẩu!
                        </div>
                        <% } %>

                            <form action="login" method="post" id="loginForm">
                                <div class="form-group">
                                    <label class="form-label">Vai trò</label>
                                    <select name="role" class="form-control" required>
                                        <option value="EMPLOYEE">Nhân viên (Employee)</option>
                                        <option value="TEACHER">Giáo viên / Quản lý (Teacher)</option>
                                        <option value="ADMIN">Quản trị viên (Admin)</option>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label class="form-label">Email</label>
                                    <input type="email" name="email" class="form-control"
                                        placeholder="your.email@company.com" required>
                                </div>

                                <div class="form-group">
                                    <label class="form-label">Mật khẩu</label>
                                    <div style="position: relative;">
                                        <input type="password" name="password" id="password" class="form-control"
                                            placeholder="••••••••" required>
                                        <button type="button" onclick="AttendanceSystem.togglePassword('password')"
                                            style="position: absolute; right: 10px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer;">
                                            <svg width="20" height="20" fill="none" stroke="currentColor"
                                                stroke-width="2">
                                                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                                                <circle cx="12" cy="12" r="3"></circle>
                                            </svg>
                                        </button>
                                    </div>
                                </div>

                                <button type="submit" class="btn btn-primary w-full"
                                    style="width: 100%; margin-top: 1.5rem;">
                                    <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"
                                        stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                                        <polyline points="10 17 15 12 10 7"></polyline>
                                        <line x1="15" y1="12" x2="3" y2="12"></line>
                                    </svg>
                                    Đăng nhập
                                </button>
                            </form>
                </div>

                <div class="card-footer text-center">
                    <p class="text-sm text-muted">
                        Chưa có tài khoản?
                        <a href="register.jsp" class="text-primary font-semibold">Đăng ký ngay</a>
                    </p>
                    <p class="text-sm text-muted" style="margin-top: 0.5rem;">
                        <a href="index.jsp" class="text-muted">← Về trang chủ</a>
                    </p>
                </div>

                <div style="text-align: center; margin-top: 1rem;">
                    <button id="theme-toggle" onclick="AttendanceSystem.toggleTheme()" class="btn btn-icon btn-outline">
                        <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"
                            stroke-linecap="round" stroke-linejoin="round">
                            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
                        </svg>
                    </button>
                </div>
            </div>
        </div>

        <script src="js/main.js"></script>
    </body>

    </html>