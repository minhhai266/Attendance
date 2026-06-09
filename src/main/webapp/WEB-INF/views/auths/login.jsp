<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/layouts/header.jsp" %>

<div class="min-h-screen bg-gradient-to-br from-slate-950 via-indigo-950 to-slate-950 flex items-center justify-center p-4">
    <div class="w-full max-w-md bg-slate-900/50 backdrop-blur-xl border border-white/10 rounded-2xl p-8">
        <div class="text-center mb-8">
            <div class="w-16 h-16 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
                <i data-lucide="camera" class="w-8 h-8 text-white"></i>
            </div>
            <h1 class="text-3xl font-bold text-white mb-2">Đăng nhập</h1>
            <p class="text-slate-400">Truy cập vào hệ thống quản lý điểm danh</p>
        </div>

        <form action="${pageContext.request.contextPath}/login" method="POST" class="space-y-4">
            <div class="space-y-2">
                <label for="email" class="block text-sm text-slate-300">Email</label>
                <input
                    type="email"
                    id="email"
                    name="email"
                    placeholder="example@company.com"
                    class="w-full px-4 py-3 bg-slate-800/50 border border-white/10 rounded-xl text-white placeholder:text-slate-500 focus:outline-none focus:border-indigo-500 transition"
                    required
                />
            </div>

            <div class="space-y-2">
                <label for="password" class="block text-sm text-slate-300">Mật khẩu</label>
                <div class="relative">
                    <input
                        type="password"
                        id="password"
                        name="password"
                        placeholder="••••••••"
                        class="w-full px-4 py-3 bg-slate-800/50 border border-white/10 rounded-xl text-white placeholder:text-slate-500 focus:outline-none focus:border-indigo-500 transition pr-10"
                        required
                    />
                    <button
                        type="button"
                        onclick="togglePassword()"
                        class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white"
                    >
                        <i data-lucide="eye" id="eyeIcon" class="w-5 h-5"></i>
                    </button>
                </div>
            </div>

            <div class="space-y-3 pt-4">
                <button
                    type="submit"
                    formaction="${pageContext.request.contextPath}/employee/dashboard"
                    class="w-full px-4 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 rounded-xl font-medium transition"
                >
                    Đăng nhập với vai trò Nhân viên
                </button>
                <button
                    type="submit"
                    formaction="${pageContext.request.contextPath}/teacher/dashboard"
                    class="w-full px-4 py-3 bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 rounded-xl font-medium transition"
                >
                    Đăng nhập với vai trò Quản lý
                </button>
                <button
                    type="submit"
                    formaction="${pageContext.request.contextPath}/admin/dashboard"
                    class="w-full px-4 py-3 bg-gradient-to-r from-pink-600 to-orange-600 hover:from-pink-700 hover:to-orange-700 rounded-xl font-medium transition"
                >
                    Đăng nhập với vai trò Admin
                </button>
            </div>
        </form>

        <div class="text-center text-sm text-slate-400 mt-6">
            Chưa có tài khoản?
            <a href="${pageContext.request.contextPath}/register" class="text-indigo-400 hover:text-indigo-300 font-medium">
                Đăng ký ngay
            </a>
        </div>
    </div>
</div>

<script>
function togglePassword() {
    const passwordInput = document.getElementById('password');
    const eyeIcon = document.getElementById('eyeIcon');
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        eyeIcon.setAttribute('data-lucide', 'eye-off');
    } else {
        passwordInput.type = 'password';
        eyeIcon.setAttribute('data-lucide', 'eye');
    }
    lucide.createIcons();
}
</script>

<%@ include file="/WEB-INF/views/layouts/footer.jsp" %>
