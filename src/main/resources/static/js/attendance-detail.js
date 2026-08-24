/**
 * attendance-detail.js
 * Shared logic for the attendance detail modal.
 * Works on both /attendance (manager) and /attendance/history (employee) pages.
 *
 * Usage:
 *   openAttendanceDetail(recordId, apiBase)   // apiBase = '/api/attendance/manager' or '/api/attendance'
 *   closeAttendanceDetail()
 */
(function () {
    "use strict";

    /* ---------- formatting helpers (same locale as attendance-manager.js) ---------- */

    function formatTime(iso) {
        if (!iso) return '--';
        var match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})(?:\.\d+)?Z$/.exec(iso);
        if (match) {
            return match[4] + ':' + match[5];
        }
        var d = new Date(iso);
        if (isNaN(d.getTime())) return iso;
        return d.toLocaleString('vi-VN', {
            timeZone: 'Asia/Ho_Chi_Minh',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        });
    }

    function formatDate(isoDate) {
        if (!isoDate) return '--';
        var match = /^(\d{4})-(\d{2})-(\d{2})/.exec(isoDate);
        if (match) {
            return match[3] + '/' + match[2] + '/' + match[1];
        }
        return isoDate;
    }

    function formatWorkingHours(minutes) {
        if (minutes === null || minutes === undefined || minutes === '') return '--';
        var numericMinutes = Number(minutes);
        if (!isFinite(numericMinutes) || numericMinutes <= 0) return '--';
        var hours = Math.floor(numericMinutes / 60);
        var mins = numericMinutes % 60;
        var result = '';
        if (hours > 0) result += hours + ' giờ';
        if (mins > 0) result += (result ? ' ' : '') + mins + ' phút';
        return result || '--';
    }

    function statusLabel(status) {
        if (!status) return 'Không rõ';
        var map = {
            PRESENT: { text: 'Đúng giờ', cls: 'present' },
            ABSENT: { text: 'Vắng mặt', cls: 'absent' },
            LEAVE: { text: 'Nghỉ phép', cls: 'leave' },
            'DAY_OFF': { text: 'Ngày nghỉ', cls: 'day-off' }
        };
        var entry = map[status] || { text: status, cls: '' };
        return '<span class="status-badge ' + entry.cls + '">' + entry.text + '</span>';
    }

    function checkStatusLabel(status) {
        var map = {
            LATE: { text: 'Đi muộn', cls: 'late' },
            'EARLY_LEAVE': { text: 'Về sớm', cls: 'early-leave' }
        };
        var entry = map[status] || { text: status, cls: '' };
        return '<span class="status-badge ' + entry.cls + '">' + entry.text + '</span>';
    }

    /* ---------- modal open / close ---------- */

    function openAttendanceDetail(recordId, apiBase) {
        var overlay = document.getElementById('attendanceDetailModal');
        if (!overlay) return;
        overlay.style.display = 'flex';

        var loading = document.getElementById('attendanceDetailLoading');
        var content = document.getElementById('attendanceDetailContent');
        if (loading) loading.style.display = 'block';
        if (content) content.style.display = 'none';

        fetch(apiBase + '/detail/' + recordId)
            .then(function (res) {
                if (!res.ok) throw new Error('Không thể tải chi tiết bản ghi');
                return res.json();
            })
            .then(function (data) {
                renderDetail(data);
                if (loading) loading.style.display = 'none';
                if (content) content.style.display = 'block';
            })
            .catch(function (err) {
                console.error('Detail error:', err);
                if (loading) {
                    loading.innerHTML = '<p style="color:var(--text-gray);text-align:center;">' +
                        err.message + '</p>';
                }
            });
    }

    function closeAttendanceDetail() {
        var overlay = document.getElementById('attendanceDetailModal');
        if (overlay) overlay.style.display = 'none';
    }

    /* ---------- render detail data ---------- */

    function renderDetail(data) {
        var content = document.getElementById('attendanceDetailContent');
        if (!content) return;

        var html = '';
        html += '<div class="detail-info">';
        html += '  <div class="detail-info-grid">';
        html += '    <div class="detail-info-item"><label>Tên sinh viên</label><div class="value">' + escapeHtml(data.fullName || '--') + '</div></div>';
        html += '    <div class="detail-info-item"><label>Ngày</label><div class="value">' + formatDate(data.attendanceDate) + '</div></div>';
        html += '    <div class="detail-info-item"><label>Vào</label><div class="value">' + formatTime(data.checkInTime) + '</div></div>';
        html += '    <div class="detail-info-item"><label>Ra</label><div class="value">' + formatTime(data.checkOutTime) + '</div></div>';
        html += '    <div class="detail-info-item"><label>Tổng giờ</label><div class="value">' + formatWorkingHours(data.workingMinutes) + '</div></div>';
        var checkStatusHtml = '';
        if (Array.isArray(data.checkStatuses)) {
            for (var i = 0; i < data.checkStatuses.length; i++) {
                checkStatusHtml += (checkStatusHtml ? ' ' : '') + checkStatusLabel(data.checkStatuses[i]);
            }
        }
        var statusHtml = checkStatusHtml || '<span class="status-badge present">Đúng giờ</span>';
        html += '    <div class="detail-info-item"><label>Trạng thái</label><div class="value">' + statusHtml + '</div></div>';
        html += '    <div class="detail-info-item"><label>Ghi chú</label><div class="value">' + escapeHtml(data.note || '-') + '</div></div>';
        html += '  </div>';
        html += '</div>';

        html += '<div class="detail-images">';
        html += '  <div>';
        html += '    <div class="detail-image-label">Ảnh check in</div>';
        html += '    <div class="detail-image-container" id="detailCheckInImage">' + buildImageHtml(data.checkInImageUrl) + '</div>';
        html += '  </div>';
        html += '  <div>';
        html += '    <div class="detail-image-label">Ảnh checkout</div>';
        html += '    <div class="detail-image-container" id="detailCheckOutImage">' + buildImageHtml(data.checkOutImageUrl) + '</div>';
        html += '  </div>';
        html += '</div>';

        content.innerHTML = html;
    }

    function escapeHtml(value) {
        return String(value).replace(/[&<>"']/g, function (character) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[character];
        });
    }

    /**
     * Build HTML for an image container.
     * If imageUrl is present → <img> element.
     * If null → plain black container (no <img>, keeps dark background).
     */
    function buildImageHtml(imageUrl) {
        if (!imageUrl) {
            return '<div class="no-image">Chưa có ảnh</div>';
        }
        return '<img src="' + imageUrl + '" alt="Evidence image" onerror="this.onerror=null;this.parentElement.innerHTML=\'<div class=\'no-image\'>Lỗi tải ảnh</div>\';">';
    }

    /* ---------- global exposure ---------- */

    window.openAttendanceDetail = openAttendanceDetail;
    window.closeAttendanceDetail = closeAttendanceDetail;

    // Close modal on Escape key or outside click
    document.addEventListener('DOMContentLoaded', function () {
        var overlay = document.getElementById('attendanceDetailModal');
        if (!overlay) return;

        // Click outside to close
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) {
                closeAttendanceDetail();
            }
        });

        // Escape key to close
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && overlay.style.display === 'flex') {
                closeAttendanceDetail();
            }
        });
    });
})();
