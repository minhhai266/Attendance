<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Điểm Danh Face ID - Face ID Attendance</title>
    <link rel="stylesheet" href="css/theme.css">
    <link rel="stylesheet" href="css/components.css">
    <style>
        .camera-frame {
            position: relative;
            width: 400px;
            height: 500px;
            background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
            border-radius: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
        }
        .face-icon {
            font-size: 10rem;
            opacity: 0.3;
        }
        .corner-marker {
            position: absolute;
            width: 30px;
            height: 30px;
            border: 3px solid #60a5fa;
        }
        .corner-marker.top-left { top: 20px; left: 20px; border-right: none; border-bottom: none; }
        .corner-marker.top-right { top: 20px; right: 20px; border-left: none; border-bottom: none; }
        .corner-marker.bottom-left { bottom: 20px; left: 20px; border-right: none; border-top: none; }
        .corner-marker.bottom-right { bottom: 20px; right: 20px; border-left: none; border-top: none; }
        .scan-line {
            position: absolute;
            width: 90%;
            height: 2px;
            background: linear-gradient(to right, transparent, #60a5fa, transparent);
            animation: scan 2s linear infinite;
        }
        @keyframes scan {
            0% { top: 0%; }
            100% { top: 100%; }
        }
        .log-item {
            padding: 1rem;
            background: var(--card);
            border: 1px solid var(--border);
            border-radius: 0.5rem;
            margin-bottom: 0.5rem;
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="nav">
            <div class="nav-brand">
                <svg width="32" height="32" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                </svg>
                Điểm Danh Face ID
            </div>
            <div class="flex items-center gap-4">
                <div class="clock text-right">
                    <div class="clock-time font-bold text-xl">--:--:--</div>
                    <div class="clock-date text-sm text-muted"></div>
                </div>
                <button id="theme-toggle" onclick="AttendanceSystem.toggleTheme()" class="btn btn-icon btn-outline">
                    <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
                    </svg>
                </button>
            </div>
        </div>
    </div>

    <div class="container" style="padding: 2rem 1.5rem;">
        <div class="grid grid-cols-3 gap-6">
            <!-- Recent Logs -->
            <div>
                <h3 class="font-bold mb-4">Lịch sử gần đây</h3>
                <div style="max-height: 600px; overflow-y: auto;">
                    <div class="log-item animate-slide-up">
                        <div class="flex items-center gap-3">
                            <div class="badge badge-success">✓</div>
                            <div>
                                <div class="font-semibold">Nguyễn Văn A</div>
                                <div class="text-xs text-muted">Check In: 07:58</div>
                            </div>
                        </div>
                    </div>
                    <div class="log-item animate-slide-up">
                        <div class="flex items-center gap-3">
                            <div class="badge badge-warning">!</div>
                            <div>
                                <div class="font-semibold">Trần Thị B</div>
                                <div class="text-xs text-muted">Check In: 08:12 (Muộn)</div>
                            </div>
                        </div>
                    </div>
                    <div class="log-item animate-slide-up">
                        <div class="flex items-center gap-3">
                            <div class="badge badge-success">✓</div>
                            <div>
                                <div class="font-semibold">Lê Văn C</div>
                                <div class="text-xs text-muted">Check In: 08:00</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Camera View -->
            <div class="text-center">
                <h3 class="font-bold mb-4">Quét khuôn mặt</h3>
                <div class="camera-frame mx-auto">
                    <div class="face-icon">👤</div>
                    <div class="corner-marker top-left"></div>
                    <div class="corner-marker top-right"></div>
                    <div class="corner-marker bottom-left"></div>
                    <div class="corner-marker bottom-right"></div>
                    <div class="scan-line"></div>
                </div>
                <p class="text-muted mt-4">Vui lòng nhìn vào camera để điểm danh</p>
                <p class="text-sm text-muted">Hệ thống đang quét...</p>
            </div>

            <!-- Stats -->
            <div>
                <h3 class="font-bold mb-4">Thống kê hôm nay</h3>
                <div class="stat-card stat-card-success mb-4">
                    <div class="text-muted text-sm mb-1">Đã điểm danh</div>
                    <div class="text-3xl font-bold text-success">42</div>
                </div>
                <div class="stat-card stat-card-warning mb-4">
                    <div class="text-muted text-sm mb-1">Đi muộn</div>
                    <div class="text-3xl font-bold text-warning">8</div>
                </div>
                <div class="stat-card stat-card-destructive mb-4">
                    <div class="text-muted text-sm mb-1">Chưa điểm danh</div>
                    <div class="text-3xl font-bold text-destructive">12</div>
                </div>
                <div class="card mt-6">
                    <div class="text-sm text-muted mb-2">Tổng nhân viên</div>
                    <div class="text-2xl font-bold">50</div>
                    <div class="text-xs text-muted mt-2">Tỷ lệ chuyên cần: 84%</div>
                </div>
            </div>
        </div>
    </div>

    <script src="js/main.js"></script>
</body>
</html>
