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
        if (!Number.isFinite(numericMinutes) || numericMinutes <= 0) return '--';
        const hours = Math.floor(numericMinutes / 60);
        const mins = numericMinutes % 60;
        let result = '';
        if (hours > 0) result += `${hours} giờ`;
        if (mins > 0) result += (result ? ' ' : '') + `${mins} phút`;
        return result || '--';
    }

    function formatDate(isoDate) {
        if (!isoDate) return '--';
        const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(isoDate);
        if (match) {
            const [, year, month, day] = match;
            return `${day}/${month}/${year}`;
        }
        return isoDate;
    }

    async function loadStats() {
        const params = new URLSearchParams();
        const startDate = el('attendanceDateFrom')?.value || el('dateFilter')?.value;
        const endDate = el('attendanceDateTo')?.value || el('dateFilter')?.value;
        const status = el('attendanceStatusFilter')?.value;
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
        const startDate = el('attendanceDateFrom')?.value || el('dateFilter')?.value;
        const endDate = el('attendanceDateTo')?.value || el('dateFilter')?.value;
        const status = el('attendanceStatusFilter')?.value;
        if (startDate) params.set('startDate', startDate);
        if (endDate) params.set('endDate', endDate);
        if (status) params.set('status', status);
        try {
            const list = await fetchJSON(`${API_BASE}/attendance/manager/list?${params.toString()}`);
            tbody.innerHTML = '';
            if (!Array.isArray(list) || list.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8" class="text-center">Không có dữ liệu</td></tr>';
                return;
            }
            for (const item of list) {
                const tr = document.createElement('tr');
                const name = item?.userFullName || '--';
                const checkIn = formatTime(item?.checkInTime);
                const checkOut = formatTime(item?.checkOutTime);
                const workingHours = formatWorkingHours(item?.workingMinutes);
                const statusMap = {
                    ABSENT: '<span class="badge absent">Vắng mặt</span>',
                    LEAVE: '<span class="badge leave">Nghỉ phép</span>',
                    'DAY_OFF': '<span class="badge day-off">Ngày nghỉ</span>'
                };
                const checkStatusMap = {
                    LATE: '<span class="badge late">Đi muộn</span>',
                    EARLY_LEAVE: '<span class="badge warning">Về sớm</span>'
                };
                let checkStatusBadges = '';
                if (Array.isArray(item?.checkStatuses)) {
                    for (const cs of item.checkStatuses) {
                        if (checkStatusMap[cs]) checkStatusBadges += (checkStatusBadges ? ' ' : '') + checkStatusMap[cs];
                    }
                }
                const statusBadges = statusMap[item?.status] || checkStatusBadges || '<span class="badge success">Đúng giờ</span>';
                tr.innerHTML = `
                    <td>${formatDate(item?.attendanceDate)}</td>
                    <td>${escapeHtml(name)}</td>
                    <td>${checkIn}</td>
                    <td>${checkOut}</td>
                    <td>${workingHours}</td>
                    <td>${statusBadges}</td>
                    <td>${escapeHtml(item?.note ?? '--')}</td>
                    <td><button type="button" class="detail-button" onclick="openAttendanceDetail(${item?.id}, '/api/attendance/manager')">Xem</button></td>
                `;
                tbody.appendChild(tr);
            }
        } catch (e) {
            console.error('Failed to load list', e);
            const target = getListTarget();
            if (target) target.innerHTML = '<tr><td colspan="8" class="text-center">Lỗi tải dữ liệu</td></tr>';
        }
    }

    function escapeHtml(value) {
        return String(value).replace(/[&<>"']/g, (character) => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        })[character]);
    }

    window.loadAttendanceRecords = loadList;

    window.resetAttendanceFilters = function () {
        const today = new Date().toISOString().slice(0, 10);
        const dateFrom = el('attendanceDateFrom');
        const dateTo = el('attendanceDateTo');
        const statusFilter = el('attendanceStatusFilter');

        if (dateFrom) dateFrom.value = today;
        if (dateTo) dateTo.value = today;
        if (statusFilter) statusFilter.value = '';
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
