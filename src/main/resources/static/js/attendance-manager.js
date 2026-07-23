(function () {
    const API_BASE = '/api';

    async function fetchJSON(url) {
        const res = await fetch(url);
        if (!res.ok) {
            const text = await res.text();
            throw new Error(text || 'Request failed');
        }
        return res.json();
    }

    function el(id) { return document.getElementById(id); }

    function getListTarget() {
        return el('attendanceList') || el('attendanceTable');
    }

    function formatTime(iso) {
        if (!iso) return '--';
        const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})(?:\.\d+)?Z$/.exec(iso);
        if (match) {
            const [, year, month, day, hour, minute] = match;
            return `${hour}:${minute}`;
        }
        const d = new Date(iso);
        if (isNaN(d.getTime())) return iso;
        return d.toLocaleString('vi-VN', {
            timeZone: 'Asia/Ho_Chi_Minh',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        });
    }

    function formatWorkingHours(minutes) {
        if (minutes === null || minutes === undefined || minutes === '') return '--';
        const numericMinutes = Number(minutes);
        if (!Number.isFinite(numericMinutes) || numericMinutes <= 0) return '0.0';
        const hours = numericMinutes / 60;
        return hours.toFixed(1);
    }

    async function loadStats() {
        const params = new URLSearchParams();
        const dept = el('departmentFilter')?.value || el('departmentId')?.value;
        const startDate = el('attendanceDateFrom')?.value || el('dateFilter')?.value;
        const endDate = el('attendanceDateTo')?.value || el('dateFilter')?.value;
        const status = el('attendanceStatusFilter')?.value;
        if (dept) params.set('departmentId', dept);
        if (startDate) params.set('startDate', startDate);
        if (endDate) params.set('endDate', endDate);
        if (status) params.set('status', status);
        try {
            const data = await fetchJSON(`${API_BASE}/attendance/manager/stats?${params.toString()}`);
            const totalEl = el('statTotal') || el('totalCount');
            const presentEl = el('statPresent') || el('presentCount');
            const lateEl = el('statLate') || el('lateCount');
            const absentEl = el('statAbsent') || el('absentCount');
            const checkedOutEl = el('statCheckedOut') || el('checkedOutCount');
            const pendingEl = el('pendingCount');
            if (totalEl) totalEl.textContent = data.totalEmployees ?? 0;
            if (presentEl) presentEl.textContent = data.checkedIn ?? 0;
            if (checkedOutEl) checkedOutEl.textContent = data.checkedOut ?? 0;
            if (lateEl) lateEl.textContent = data.lateArrivals ?? 0;
            if (absentEl) absentEl.textContent = data.absent ?? 0;
            if (pendingEl) pendingEl.textContent = Math.max((data.totalEmployees ?? 0) - (data.checkedIn ?? 0), 0);
        } catch (e) {
            console.error('Failed to load stats', e);
        }
    }

    async function loadList() {
        const tbody = getListTarget();
        if (!tbody) return;
        const params = new URLSearchParams();
        const dept = el('departmentFilter')?.value || el('departmentId')?.value;
        const startDate = el('attendanceDateFrom')?.value || el('dateFilter')?.value;
        const endDate = el('attendanceDateTo')?.value || el('dateFilter')?.value;
        const status = el('attendanceStatusFilter')?.value;
        if (dept) params.set('departmentId', dept);
        if (startDate) params.set('startDate', startDate);
        if (endDate) params.set('endDate', endDate);
        if (status) params.set('status', status);
        try {
            const list = await fetchJSON(`${API_BASE}/attendance/manager/list?${params.toString()}`);
            tbody.innerHTML = '';
            if (!Array.isArray(list) || list.length === 0) {
                tbody.innerHTML = '<tr><td colspan="10" class="text-center">Không có dữ liệu</td></tr>';
                return;
            }
            for (const item of list) {
                const tr = document.createElement('tr');
                const name = item?.fullName || '--';
                const department = item?.department || '--';
                const checkIn = formatTime(item?.checkInTime);
                const checkOut = formatTime(item?.checkOutTime);
                const workingHours = formatWorkingHours(item?.workingMinutes);
                const statusValue = item?.status;
                let statusClass = 'status-absent';
                let statusText = 'Vắng';
                if (statusValue === 'PRESENT' || statusValue === 'LATE') {
                    if (statusValue === 'LATE') {
                        statusClass = 'status-late';
                        statusText = 'Đi muộn';
                    } else if (item.checkOutTime) {
                        statusClass = 'status-checked-out';
                        statusText = 'Đã checkout';
                    } else {
                        statusClass = 'status-present';
                        statusText = 'Đã điểm danh';
                    }
                }
                tr.innerHTML = `
                    <td>${item?.attendanceDate ?? '--'}</td>
                    <td>${name}</td>
                    <td>${checkIn}</td>
                    <td>${checkOut}</td>
                    <td><span class="history-status ${statusClass}">${statusText}</span></td>
                    <td>${item?.department ?? '--'}</td>
                    <td>${item?.late ? 'Có' : 'Không'}</td>
                    <td>${item?.earlyLeave ? 'Có' : 'Không'}</td>
                    <td>${workingHours}</td>
                    <td>${item?.note ?? '--'}</td>
                `;
                tbody.appendChild(tr);
            }
        } catch (e) {
            console.error('Failed to load list', e);
            const target = getListTarget();
            if (target) target.innerHTML = '<tr><td colspan="10" class="text-center">Lỗi tải dữ liệu</td></tr>';
        }
    }

    window.loadAttendanceRecords = loadList;

    window.resetAttendanceFilters = function () {
        const today = new Date().toISOString().slice(0, 10);
        const dateFrom = el('attendanceDateFrom');
        const dateTo = el('attendanceDateTo');
        const statusFilter = el('attendanceStatusFilter');
        const deptFilter = el('departmentFilter');

        if (dateFrom) dateFrom.value = today;
        if (dateTo) dateTo.value = today;
        if (statusFilter) statusFilter.value = '';
        if (deptFilter) deptFilter.value = '';
        loadStats();
        loadList();
    };

    document.addEventListener('DOMContentLoaded', () => {
        loadStats();
        loadList();
        const filterForm = el('filterForm');
        if (filterForm) {
            filterForm.addEventListener('submit', (e) => {
                e.preventDefault();
                loadStats();
                loadList();
            });
        }
    });
})();