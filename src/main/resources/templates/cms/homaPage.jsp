<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="UTF-8">
    <title>Dashboard</title>

    <link rel="stylesheet" th:href="@{/css/dashboard-user.css}">
</head>

<body>

    <!-- Background -->
    <div class="bg-image"></div>

    <div th:replace="~{cms/layouts/header :: header}"></div>

    <div class="layout">


        <main class="section">

            <div class="container-xl">

                <div class="section-header">
                    <div class="header-left">
                        <div class="section-eyebrow">
                            SMART ATTENDANCE
                        </div>

                        <div class="section-title">
                            SYSTEM
                        </div>

                        <div class="section-divider"></div>

                        <p class="section-subtitle">
                            Giải pháp điểm danh Face ID thông minh cho doanh nghiệp và cơ sở giáo dục.
                            Quản lý hiệu quả, chính xác và an toàn
                        </p>

                        <a href="" class="btn btn-primary">Bắt đầu ngay</a>
                        <a href="" class="btn btn-secondery">
                            Xem Demo
                        </a>
                    </div>

                    <div class="header-right">
                        <div class="right-box">
                            <div class="grid grid-cols-2 gap-4">
                                <div class="header-form">
                                    <i data-lucide="users" class="form-icon"></i>
                                    <div class="form-line">5000+</div>
                                    <div class="form-text">Người dùng</div>
                                </div>
                                <div class="header-form">
                                    <i data-lucide="target" class="form-icon"></i>
                                    <div class="form-line">99.8%</div>
                                    <div class="form-text">Độ chính xác</div>
                                </div>
                                <div class="header-form">
                                    <i data-lucide="building-2" class="form-icon"></i>
                                    <div class="form-line">50+</div>
                                    <div class="form-text">Đơn vị</div>
                                </div>
                                <div class="header-form">
                                    <i data-lucide="clock" class="form-icon"></i>
                                    <div class="form-line">24/7</div>
                                    <div class="form-text">Hoạt động</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Home body -->
                <div class="section-body">
                    <div class="body-bg">
                        <div class="body-header">
                            <h2 class="header-title">Tính năng nổi bật</h2>
                            <p class="section-subtitle">Giải pháp toàn diện cho quản lý điểm danh</p>
                        </div>
                        <div class="body-container">
                            <div class="container-box">
                                <div class="container-icon">
                                    <i data-lucide="camera" class="icon"></i>
                                </div>
                                <h3 class="container-title">Face Recognition</h3>
                                <p class="container-text">Nhận diện khuôn mặt chính xác với AI, chống
                                    điểm danh hộ</p>
                            </div>
                            <div class="container-box">
                                <div class="container-icon">
                                    <i data-lucide="bar-chart-3" class="icon"></i>
                                </div>
                                <h3 class="container-title">Analytics Dashboard</h3>
                                <p class="container-text">Thống kê realtime, biểu đồ trực quan, báo cáo
                                    chi tiết</p>
                            </div>
                            <div class="container-box">
                                <div class="container-icon">
                                    <i data-lucide="file-text" class="icon"></i>
                                </div>
                                <h3 class="container-title">Report Management</h3>
                                <p class="container-text">Quản lý báo cáo công việc hằng ngày và cuối
                                    tuần</p>
                            </div>
                            <div class="container-box">
                                <div class="container-icon">
                                    <i data-lucide="calendar" class="icon"></i>
                                </div>
                                <h3 class="container-title">Leave Management</h3>
                                <p class="container-text">Xin nghỉ phép, duyệt đơn, theo dõi lịch sử
                                    nghỉ</p>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </main>
    </div>
    <div th:replace="~{cms/layouts/footer :: footer}"></div>

</body>

</html>