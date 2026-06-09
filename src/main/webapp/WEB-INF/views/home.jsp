<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/layouts/header.jsp" %>

<div class="min-h-screen bg-gradient-to-br from-slate-950 via-indigo-950 to-slate-950">
    <!-- Navigation -->
    <nav class="border-b border-white/10 bg-slate-950/50 backdrop-blur-xl">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <div class="flex items-center gap-2">
                    <div class="w-10 h-10 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center">
                        <i data-lucide="camera" class="w-6 h-6 text-white"></i>
                    </div>
                    <span class="text-xl font-bold bg-gradient-to-r from-indigo-400 to-purple-400 bg-clip-text text-transparent">
                        Smart Attendance
                    </span>
                </div>
                <div class="flex gap-4">
                    <a href="${pageContext.request.contextPath}/login" class="px-4 py-2 text-slate-300 hover:text-white transition">Đăng nhập</a>
                    <a href="${pageContext.request.contextPath}/register" class="px-4 py-2 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 rounded-lg transition">Bắt đầu ngay</a>
                </div>
            </div>
        </div>
    </nav>

    <!-- Hero Section -->
    <section class="relative overflow-hidden py-20 lg:py-32">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="grid lg:grid-cols-2 gap-12 items-center">
                <div>
                    <h1 class="text-5xl lg:text-7xl font-bold mb-6">
                        <span class="bg-gradient-to-r from-indigo-400 via-purple-400 to-pink-400 bg-clip-text text-transparent">
                            SMART ATTENDANCE
                        </span>
                        <br />
                        <span class="text-white">SYSTEM</span>
                    </h1>
                    <p class="text-xl text-slate-300 mb-8 leading-relaxed">
                        Giải pháp điểm danh Face ID thông minh cho doanh nghiệp và cơ sở giáo dục.
                        Quản lý hiệu quả, chính xác và an toàn.
                    </p>
                    <div class="flex gap-4">
                        <a href="${pageContext.request.contextPath}/register" class="px-6 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 rounded-lg font-medium transition">
                            Bắt đầu ngay
                        </a>
                        <a href="${pageContext.request.contextPath}/login" class="px-6 py-3 border border-white/20 text-white hover:bg-white/10 rounded-lg font-medium transition">
                            Xem Demo
                        </a>
                    </div>
                </div>

                <div class="relative">
                    <div class="bg-gradient-to-br from-indigo-600/20 to-purple-600/20 backdrop-blur-xl border border-white/10 rounded-3xl p-8">
                        <div class="grid grid-cols-2 gap-4">
                            <div class="bg-slate-900/50 backdrop-blur-sm border border-white/10 rounded-2xl p-6 text-center">
                                <i data-lucide="users" class="w-8 h-8 mx-auto mb-3 text-indigo-400"></i>
                                <div class="text-3xl font-bold text-white mb-1">5000+</div>
                                <div class="text-sm text-slate-400">Người dùng</div>
                            </div>
                            <div class="bg-slate-900/50 backdrop-blur-sm border border-white/10 rounded-2xl p-6 text-center">
                                <i data-lucide="target" class="w-8 h-8 mx-auto mb-3 text-indigo-400"></i>
                                <div class="text-3xl font-bold text-white mb-1">99.8%</div>
                                <div class="text-sm text-slate-400">Độ chính xác</div>
                            </div>
                            <div class="bg-slate-900/50 backdrop-blur-sm border border-white/10 rounded-2xl p-6 text-center">
                                <i data-lucide="building-2" class="w-8 h-8 mx-auto mb-3 text-indigo-400"></i>
                                <div class="text-3xl font-bold text-white mb-1">50+</div>
                                <div class="text-sm text-slate-400">Đơn vị</div>
                            </div>
                            <div class="bg-slate-900/50 backdrop-blur-sm border border-white/10 rounded-2xl p-6 text-center">
                                <i data-lucide="clock" class="w-8 h-8 mx-auto mb-3 text-indigo-400"></i>
                                <div class="text-3xl font-bold text-white mb-1">24/7</div>
                                <div class="text-sm text-slate-400">Hoạt động</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Features Section -->
    <section class="py-20 bg-slate-900/50">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="text-center mb-16">
                <h2 class="text-4xl font-bold text-white mb-4">Tính năng nổi bật</h2>
                <p class="text-xl text-slate-400">Giải pháp toàn diện cho quản lý điểm danh</p>
            </div>
            <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
                <div class="bg-slate-900/50 backdrop-blur-sm border border-white/10 hover:border-white/20 rounded-2xl p-6 transition-all group">
                    <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-indigo-600 to-indigo-800 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                        <i data-lucide="camera" class="w-7 h-7 text-white"></i>
                    </div>
                    <h3 class="text-xl font-bold text-white mb-2">Face Recognition</h3>
                    <p class="text-slate-400 leading-relaxed">Nhận diện khuôn mặt chính xác với AI, chống điểm danh hộ</p>
                </div>
                <div class="bg-slate-900/50 backdrop-blur-sm border border-white/10 hover:border-white/20 rounded-2xl p-6 transition-all group">
                    <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-purple-600 to-purple-800 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                        <i data-lucide="bar-chart-3" class="w-7 h-7 text-white"></i>
                    </div>
                    <h3 class="text-xl font-bold text-white mb-2">Analytics Dashboard</h3>
                    <p class="text-slate-400 leading-relaxed">Thống kê realtime, biểu đồ trực quan, báo cáo chi tiết</p>
                </div>
                <div class="bg-slate-900/50 backdrop-blur-sm border border-white/10 hover:border-white/20 rounded-2xl p-6 transition-all group">
                    <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-pink-600 to-pink-800 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                        <i data-lucide="file-text" class="w-7 h-7 text-white"></i>
                    </div>
                    <h3 class="text-xl font-bold text-white mb-2">Report Management</h3>
                    <p class="text-slate-400 leading-relaxed">Quản lý báo cáo công việc hằng ngày và cuối tuần</p>
                </div>
                <div class="bg-slate-900/50 backdrop-blur-sm border border-white/10 hover:border-white/20 rounded-2xl p-6 transition-all group">
                    <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-orange-600 to-orange-800 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                        <i data-lucide="calendar" class="w-7 h-7 text-white"></i>
                    </div>
                    <h3 class="text-xl font-bold text-white mb-2">Leave Management</h3>
                    <p class="text-slate-400 leading-relaxed">Xin nghỉ phép, duyệt đơn, theo dõi lịch sử nghỉ</p>
                </div>
            </div>
        </div>
    </section>

    <!-- Footer -->
    <footer class="border-t border-white/10 bg-slate-950/50 backdrop-blur-xl py-12">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-slate-400">
            <p>&copy; 2026 Smart Attendance System. All rights reserved.</p>
        </div>
    </footer>
</div>

<%@ include file="/WEB-INF/views/layouts/footer.jsp" %>
