<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<aside class="w-64 bg-slate-900/50 backdrop-blur-xl border-r border-white/10 flex flex-col">
    <!-- Logo -->
    <div class="p-6 border-b border-white/10">
        <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-gradient-to-br ${roleColor} rounded-xl flex items-center justify-center">
                <i data-lucide="camera" class="w-6 h-6 text-white"></i>
            </div>
            <div>
                <div class="font-bold text-white">Smart Attendance</div>
                <div class="text-xs text-slate-400">${roleLabel}</div>
            </div>
        </div>
    </div>

    <!-- Menu -->
    <nav class="flex-1 p-4 space-y-1">
        <c:forEach items="${menuItems}" var="item">
            <a href="${pageContext.request.contextPath}${item.path}"
                class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${currentPage == item.path ? 'bg-gradient-to-r ' + roleColor + ' text-white' : 'text-slate-400 hover:text-white hover:bg-white/5'}">
                <i data-lucide="${item.icon}" class="w-5 h-5"></i>
                <span>${item.label}</span>
            </a>
        </c:forEach>
    </nav>

    <!-- User Profile -->
    <div class="p-4 border-t border-white/10">
        <div class="flex items-center gap-3 p-3 rounded-xl bg-slate-800/50">
            <div class="w-10 h-10 rounded-full bg-gradient-to-br ${roleColor} flex items-center justify-center text-white font-bold">
                ${userName.substring(0, 1)}
            </div>
            <div class="flex-1 min-w-0">
                <div class="text-sm font-medium text-white truncate">${userName}</div>
                <div class="text-xs text-slate-400">${roleLabel}</div>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/login"
           class="flex items-center gap-2 w-full px-4 py-2 mt-2 text-slate-400 hover:text-white rounded-xl hover:bg-white/5 transition-all">
            <i data-lucide="log-out" class="w-4 h-4"></i>
            <span>Đăng xuất</span>
        </a>
    </div>
</aside>
