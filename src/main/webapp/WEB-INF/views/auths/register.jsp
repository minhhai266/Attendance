<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/layouts/header.jsp" %>

<div class="min-h-screen bg-gradient-to-br from-slate-950 via-indigo-950 to-slate-950 flex items-center justify-center p-4">
    <div class="w-full max-w-md bg-slate-900/50 backdrop-blur-xl border border-white/10 rounded-2xl p-8">
        <div class="text-center mb-8">
            <div class="w-16 h-16 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
                <i data-lucide="camera" class="w-8 h-8 text-white"></i>
            </div>
            <h1 class="text-3xl font-bold text-white mb-2">Đăng ký</h1>
            <p class="text-slate-400">Tạo tài khoản mới để sử dụng hệ thống</p>
        </div>

        <form action="${pageContext.request.contextPath}/register" method="POST" class="space-y-4">
            <div class="space-y-2">
                <label for="fullName" class="block text-sm text-slate-300">Họ và tên</label>
                <input
                    type="text"
                    id="fullName"
                    name="fullName"
                    placeholder="Nguyễn Văn A"
                    class="w-full px-4 py-3 bg-slate-800/50 border border-white/10 rounded-xl text-white placeholder:text-slate-500 focus:outline-none focus:border-indigo-500 transition"
                    required
                />
            </div>

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
                <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="••••••••"
                    class="w-full px-4 py-3 bg-slate-800/50 border border-white/10 rounded-xl text-white placeholder:text-slate-500 focus:outline-none focus:border-indigo-500 transition"
                    required
                />
            </div>

            <div class="space-y-2">
                <label for="confirmPassword" class="block text-sm text-slate-300">Xác nhận mật khẩu</label>
                <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    placeholder="••••••••"
                    class="w-full px-4 py-3 bg-slate-800/50 border border-white/10 rounded-xl text-white placeholder:text-slate-500 focus:outline-none focus:border-indigo-500 transition"
                    required
                />
            </div>

            <button
                type="submit"
                class="w-full px-4 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 rounded-xl font-medium transition"
            >
                Đăng ký
            </button>
        </form>

        <div class="text-center text-sm text-slate-400 mt-6">
            Đã có tài khoản?
            <a href="${pageContext.request.contextPath}/login" class="text-indigo-400 hover:text-indigo-300 font-medium">
                Đăng nhập
            </a>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layouts/footer.jsp" %>
