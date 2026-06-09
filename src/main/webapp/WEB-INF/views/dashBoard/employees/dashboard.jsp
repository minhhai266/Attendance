<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Set page variables --%>
<c:set var="pageTitle" value="Dashboard" scope="request"/>
<c:set var="roleColor" value="from-indigo-600 to-purple-600" scope="request"/>
<c:set var="roleLabel" value="Nhân viên" scope="request"/>
<c:set var="userName" value="Nguyễn Văn A" scope="request"/>
<c:set var="currentPage" value="/employee/dashboard" scope="request"/>

<%-- Define menu items --%>
<jsp:useBean id="menuItems" class="java.util.ArrayList" scope="request"/>
<%-- In real app, this would be populated from backend --%>

<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="min-h-screen bg-slate-950 flex">
    <%@ include file="/WEB-INF/views/layout/sidebar.jsp" %>

    <div class="flex-1 flex flex-col">
        <header class="h-16 bg-slate-900/50 backdrop-blur-xl border-b border-white/10 flex items-center justify-between px-6">
            <h1 class="text-xl font-bold text-white">Dashboard</h1>
            <button class="relative text-slate-400 hover:text-white">
                <i data-lucide="bell" class="w-5 h-5"></i>
                <span class="absolute top-0 right-0 w-2 h-2 bg-red-500 rounded-full"></span>
            </button>
        </header>

        <main class="flex-1 overflow-auto p-6">
            <div class="space-y-6">
                <!-- Welcome -->
                <div class="bg-gradient-to-br from-indigo-600/20 to-purple-600/20 backdrop-blur-xl border border-white/10 rounded-2xl p-6">
                    <h2 class="text-2xl font-bold text-white mb-2">Xin chào, ${userName}!</h2>
                    <p class="text-slate-300">Chúc bạn một ngày làm việc hiệu quả</p>
                </div>

                <!-- Stats Cards -->
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    <div class="bg-slate-900/50 backdrop-blur-xl border border-white/10 rounded-2xl p-6">
                        <div class="flex items-start justify-between">
                            <div class="flex-1">
                                <p class="text-sm text-slate-400 mb-1">Check In hôm nay</p>
                                <p class="text-2xl font-bold text-white mb-1">08:01 AM</p>
                                <p class="text-xs text-green-400">+2 phút sớm</p>
                            </div>
                            <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-green-600 to-emerald-600 flex items-center justify-center">
                                <i data-lucide="log-in" class="w-6 h-6 text-white"></i>
                            </div>
                        </div>
                    </div>

                    <div class="bg-slate-900/50 backdrop-blur-xl border border-white/10 rounded-2xl p-6">
                        <div class="flex items-start justify-between">
                            <div class="flex-1">
                                <p class="text-sm text-slate-400 mb-1">Giờ làm việc</p>
                                <p class="text-2xl font-bold text-white mb-1">7h 23m</p>
                            </div>
                            <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-600 to-cyan-600 flex items-center justify-center">
                                <i data-lucide="clock" class="w-6 h-6 text-white"></i>
                            </div>
                        </div>
                    </div>

                    <div class="bg-slate-900/50 backdrop-blur-xl border border-white/10 rounded-2xl p-6">
                        <div class="flex items-start justify-between">
                            <div class="flex-1">
                                <p class="text-sm text-slate-400 mb-1">Tỷ lệ chuyên cần</p>
                                <p class="text-2xl font-bold text-white mb-1">96%</p>
                                <p class="text-xs text-green-400">+2% so với tháng trước</p>
                            </div>
                            <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-purple-600 to-pink-600 flex items-center justify-center">
                                <i data-lucide="trending-up" class="w-6 h-6 text-white"></i>
                            </div>
                        </div>
                    </div>

                    <div class="bg-slate-900/50 backdrop-blur-xl border border-white/10 rounded-2xl p-6">
                        <div class="flex items-start justify-between">
                            <div class="flex-1">
                                <p class="text-sm text-slate-400 mb-1">Yêu cầu chờ duyệt</p>
                                <p class="text-2xl font-bold text-white mb-1">2</p>
                                <p class="text-xs text-slate-400">1 nghỉ phép, 1 báo cáo</p>
                            </div>
                            <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-orange-600 to-red-600 flex items-center justify-center">
                                <i data-lucide="alert-circle" class="w-6 h-6 text-white"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Timeline & Calendar -->
                <div class="grid lg:grid-cols-3 gap-6">
                    <div class="lg:col-span-2 bg-slate-900/50 backdrop-blur-xl border border-white/10 rounded-2xl p-6">
                        <h3 class="text-white font-bold mb-6 flex items-center gap-2">
                            <i data-lucide="clock" class="w-5 h-5"></i>
                            Lịch trình hôm nay
                        </h3>
                        <div class="space-y-4">
                            <div class="flex items-center gap-4">
                                <div class="w-10 h-10 rounded-full bg-green-600 flex items-center justify-center">
                                    <i data-lucide="log-in" class="w-5 h-5 text-white"></i>
                                </div>
                                <div class="flex-1">
                                    <div class="text-white font-medium">Check In</div>
                                    <div class="text-sm text-slate-400">08:01</div>
                                </div>
                                <div class="text-xs bg-green-600/20 text-green-400 px-2 py-1 rounded">Hoàn thành</div>
                            </div>
                            <div class="flex items-center gap-4">
                                <div class="w-10 h-10 rounded-full bg-slate-700 flex items-center justify-center">
                                    <i data-lucide="coffee" class="w-5 h-5 text-white"></i>
                                </div>
                                <div class="flex-1">
                                    <div class="text-white font-medium">Nghỉ trưa</div>
                                    <div class="text-sm text-slate-400">12:00</div>
                                </div>
                            </div>
                            <div class="flex items-center gap-4">
                                <div class="w-10 h-10 rounded-full bg-slate-700 flex items-center justify-center">
                                    <i data-lucide="coffee" class="w-5 h-5 text-white"></i>
                                </div>
                                <div class="flex-1">
                                    <div class="text-white font-medium">Quay lại làm việc</div>
                                    <div class="text-sm text-slate-400">13:00</div>
                                </div>
                            </div>
                            <div class="flex items-center gap-4">
                                <div class="w-10 h-10 rounded-full bg-slate-700 flex items-center justify-center">
                                    <i data-lucide="log-out" class="w-5 h-5 text-white"></i>
                                </div>
                                <div class="flex-1">
                                    <div class="text-white font-medium">Check Out</div>
                                    <div class="text-sm text-slate-400">17:30</div>
                                </div>
                            </div>
                            <button class="w-full px-4 py-3 mt-4 border border-white/20 text-white hover:bg-white/5 rounded-xl transition">
                                <i data-lucide="log-out" class="w-5 h-5 inline mr-2"></i>
                                Điểm danh ra ca
                            </button>
                        </div>
                    </div>

                    <div class="bg-slate-900/50 backdrop-blur-xl border border-white/10 rounded-2xl p-6">
                        <h3 class="text-white font-bold mb-6 flex items-center gap-2">
                            <i data-lucide="calendar" class="w-5 h-5"></i>
                            Lịch làm việc
                        </h3>
                        <div class="space-y-4 mt-6">
                            <div class="flex items-center gap-2 text-sm">
                                <div class="w-3 h-3 rounded-full bg-green-500"></div>
                                <span class="text-slate-400">Có mặt</span>
                            </div>
                            <div class="flex items-center gap-2 text-sm">
                                <div class="w-3 h-3 rounded-full bg-yellow-500"></div>
                                <span class="text-slate-400">Đi muộn</span>
                            </div>
                            <div class="flex items-center gap-2 text-sm">
                                <div class="w-3 h-3 rounded-full bg-red-500"></div>
                                <span class="text-slate-400">Vắng mặt</span>
                            </div>
                            <div class="flex items-center gap-2 text-sm">
                                <div class="w-3 h-3 rounded-full bg-blue-500"></div>
                                <span class="text-slate-400">Nghỉ phép</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<%@ include file="/WEB-INF/views/layouts/footer.jsp" %>
