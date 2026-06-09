<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/WEB-INF/views/layouts/header.jsp" %>

<div class="min-h-screen bg-slate-950 flex">
    <!-- Sidebar -->
    <%@ include file="/WEB-INF/views/layouts/sidebar.jsp" %>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col">
        <!-- Header -->
        <header class="h-16 bg-slate-900/50 backdrop-blur-xl border-b border-white/10 flex items-center justify-between px-6">
            <div>
                <h1 class="text-xl font-bold text-white">${pageTitle}</h1>
            </div>
            <div class="flex items-center gap-4">
                <button class="relative text-slate-400 hover:text-white">
                    <i data-lucide="bell" class="w-5 h-5"></i>
                    <span class="absolute top-0 right-0 w-2 h-2 bg-red-500 rounded-full"></span>
                </button>
            </div>
        </header>

        <!-- Page Content -->
        <main class="flex-1 overflow-auto p-6">
            <jsp:include page="${contentPage}" />
        </main>
    </div>
</div>

<%@ include file="/WEB-INF/views/layouts/footer.jsp" %>
