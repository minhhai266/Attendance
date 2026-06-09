<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hệ Thống Điểm Danh Face ID</title>
    <link rel="stylesheet" href="css/theme.css">
    <link rel="stylesheet" href="css/components.css">
</head>
<body>
    <div class="min-h-screen flex flex-col items-center justify-center" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
        <div class="text-center" style="color: white; max-width: 600px; padding: 2rem;">
            <h1 style="font-size: 3rem; margin-bottom: 1rem;">🎯 Hệ Thống Điểm Danh</h1>
            <h2 style="font-size: 2rem; margin-bottom: 2rem;">Face ID Recognition</h2>
            <p style="font-size: 1.25rem; margin-bottom: 3rem; opacity: 0.9;">
                Hệ thống điểm danh tự động bằng nhận diện khuôn mặt, 
                quản lý báo cáo và nghỉ phép thông minh
            </p>

            <div class="grid grid-cols-1 gap-4" style="max-width: 400px; margin: 0 auto;">
                <a href="face-attendance.jsp" class="btn btn-lg" style="background: white; color: #667eea; font-size: 1.25rem; padding: 1rem 2rem;">
                    <svg width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display: inline-block; margin-right: 0.5rem;">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    Điểm Danh Face ID
                </a>

                <a href="login.jsp" class="btn btn-lg btn-outline" style="background: rgba(255,255,255,0.2); color: white; border: 2px solid white; font-size: 1.25rem; padding: 1rem 2rem;">
                    <svg width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display: inline-block; margin-right: 0.5rem;">
                        <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                        <polyline points="10 17 15 12 10 7"></polyline>
                        <line x1="15" y1="12" x2="3" y2="12"></line>
                    </svg>
                    Đăng Nhập Hệ Thống
                </a>

                <a href="register.jsp" class="btn btn-lg" style="background: rgba(255,255,255,0.1); color: white; border: 1px solid rgba(255,255,255,0.3); font-size: 1rem; padding: 0.75rem 1.5rem;">
                    Đăng ký tài khoản mới →
                </a>
            </div>

            <div class="mt-4" style="margin-top: 3rem; opacity: 0.8;">
                <p style="font-size: 0.875rem;">© 2026 Face ID Attendance System. All rights reserved.</p>
            </div>
        </div>
    </div>

    <script src="js/main.js"></script>
</body>
</html>
