import { get, post } from '../api/method.js';

let attendanceStream = null;
let attendanceVideo = null;
let currentUser = null;
let pollTimer = null;
let overlayCanvas = null;
let overlayCtx = null;
let captureCanvas = null;
let captureCtx = null;
let consecutiveMatchState = { studentCode: null, count: 0 };
let cooldownUntil = 0;

const IDENTIFY_URL = '/api/face-id/identify';
const ATTENDANCE_URL = '/api/face-id/attendance';
const POLL_INTERVAL_MS = 800;
const REQUIRED_CONSECUTIVE_MATCHES = 3;
const COOLDOWN_AFTER_SUCCESS_MS = 8000;


const EMPLOYEE_LIST_URL = '/api/face-id/employees';      
const MANUAL_ATTENDANCE_URL = '/api/face-id/attendance/manual';
const AI_OFFLINE_TIMEOUT_MS = 30000;

const APP_STATE = {
    ONLINE: 'online',
    OFFLINE_LIST: 'offline_list',
    OFFLINE_MANUAL: 'offline_manual',
};

let appState = APP_STATE.ONLINE;
let aiFailureTimer = null;
let aiIsFailing = false;
let employeeCache = [];
let selectedEmployee = null;

document.addEventListener('DOMContentLoaded', () => {
    attendanceVideo = document.getElementById('camera');
    overlayCanvas = document.getElementById('overlay');
    overlayCtx = overlayCanvas.getContext('2d');
    captureCanvas = document.createElement('canvas');
    captureCtx = captureCanvas.getContext('2d');
    const captureBtn = document.getElementById('btnCaptureAttendance');
    const toggleCameraBtn = document.getElementById('btnToggleCamera');
    const currentDateEl = document.getElementById('currentDate');
    const todayLabel = document.getElementById('todayLabel');
    const themeToggle = document.getElementById('themeToggle');

    if (currentDateEl) {
        currentDateEl.textContent = new Date().toLocaleDateString('vi-VN', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }

    if (todayLabel) {
        todayLabel.textContent = new Date().toLocaleDateString('vi-VN');
    }

    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            document.body.classList.toggle('dark');
            themeToggle.textContent = document.body.classList.contains('dark') ? '☀️' : '🌙';
        });
    }

    if (captureBtn) {
        captureBtn.addEventListener('click', () => {
            showAttendanceMessage('Quét tự động đang chạy. Không cần bấm lại.', 'success');
        });
    }

    if (toggleCameraBtn) {
        toggleCameraBtn.addEventListener('click', toggleAttendanceCamera);
    }

    initOfflineFlowEvents();
    initAttendanceProfile();
    startAttendanceCamera();
    updateClock();
    setInterval(updateClock, 1000);
});

async function initAttendanceProfile() {
    try {
        currentUser = await get('/api/face-id/latest');
        setProfileInfo(currentUser);
        const faceStatus = document.getElementById('faceStatus');
        if (faceStatus) {
            faceStatus.textContent = currentUser.isAccept === true ? 'Đã xác thực' : 'Chờ duyệt';
        }
    } catch (error) {
        console.warn('Không thể lấy thông tin Face ID:', error);
        setProfileInfo(null);
        const faceStatus = document.getElementById('faceStatus');
        if (faceStatus) {
            faceStatus.textContent = 'Chưa đăng ký';
        }
        showAttendanceMessage('Bạn chưa đăng ký Face ID. Vui lòng thực hiện đăng ký trước khi điểm danh.', 'error');
    }
}

function setProfileInfo(profile) {
    const nameEl = document.getElementById('profileName');
    const usernameEl = document.getElementById('profileUsername');
    const emailEl = document.getElementById('profileEmail');

    if (profile) {
        if (nameEl) nameEl.textContent = profile.fullName || 'Chưa cập nhật';
        if (usernameEl) usernameEl.textContent = 'Mã nhân sự: ' + (profile.username || '--');
        if (emailEl) emailEl.textContent = 'Email: ' + (profile.email || '--');
        return;
    }

    if (nameEl) nameEl.textContent = 'Chưa có thông tin';
    if (usernameEl) usernameEl.textContent = 'Mã nhân sự: --';
    if (emailEl) emailEl.textContent = 'Email: --';
}

async function toggleAttendanceCamera() {
    if (attendanceStream) {
        stopPolling();
        attendanceStream.getTracks().forEach((track) => track.stop());
        attendanceStream = null;
        if (attendanceVideo) {
            attendanceVideo.srcObject = null;
        }
        overlayCtx.clearRect(0, 0, overlayCanvas.width, overlayCanvas.height);
        showAttendanceMessage('Camera đã tắt.', 'success');
        return;
    }

    await startAttendanceCamera();
}

async function startAttendanceCamera() {
    try {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            throw new Error('Trình duyệt không hỗ trợ camera');
        }

        attendanceStream = await navigator.mediaDevices.getUserMedia({
            video: { facingMode: 'user' },
            audio: false
        });

        if (attendanceVideo) {
            attendanceVideo.srcObject = attendanceStream;
            await attendanceVideo.play();
            resizeOverlay();
            // Chỉ polling AI khi đang ở chế độ online, tránh gọi identify khi đã offline
            if (appState === APP_STATE.ONLINE) {
                startPolling();
            }
        }
    } catch (error) {
        console.error('Không thể mở camera:', error);
        showAttendanceMessage('Không thể mở camera. Vui lòng kiểm tra quyền truy cập.', 'error');
    }
}

function resizeOverlay() {
    if (!attendanceVideo) return;
    overlayCanvas.width = attendanceVideo.videoWidth || attendanceVideo.clientWidth || 640;
    overlayCanvas.height = attendanceVideo.videoHeight || attendanceVideo.clientHeight || 480;
}

function startPolling() {
    stopPolling();
    pollTimer = window.setInterval(tick, POLL_INTERVAL_MS);
}

function stopPolling() {
    if (pollTimer) {
        clearInterval(pollTimer);
        pollTimer = null;
    }
}

async function tick() {
    if (appState !== APP_STATE.ONLINE) return;
    if (!attendanceVideo || !attendanceVideo.videoWidth) return;
    if (Date.now() < cooldownUntil) return;

    const imageBase64 = captureFrame();
    try {
        const result = await postJson(IDENTIFY_URL, { imageBase64 });
        onIdentifySuccess();
        drawOverlay(result);
        handleMatchLogic(result, imageBase64);
    } catch (error) {
        console.error('identify lỗi:', error);
        onIdentifyFailure();
    }
}

function captureFrame() {
    if (!attendanceVideo) return '';
    captureCanvas.width = attendanceVideo.videoWidth || 640;
    captureCanvas.height = attendanceVideo.videoHeight || 480;
    captureCtx.drawImage(attendanceVideo, 0, 0, captureCanvas.width, captureCanvas.height);
    return captureCanvas.toDataURL('image/jpeg', 0.85);
}

function drawOverlay(result) {
    overlayCtx.clearRect(0, 0, overlayCanvas.width, overlayCanvas.height);
    if (!result || !result.faceDetected || !result.bbox) return;

    const [x1, y1, x2, y2] = result.bbox;
    const scaleX = overlayCanvas.width / (attendanceVideo.videoWidth || 1);
    const scaleY = overlayCanvas.height / (attendanceVideo.videoHeight || 1);
    const color = result.matched ? '#22c55e' : '#eab308';

    overlayCtx.strokeStyle = color;
    overlayCtx.lineWidth = 3;
    overlayCtx.strokeRect(x1 * scaleX, y1 * scaleY, (x2 - x1) * scaleX, (y2 - y1) * scaleY);

    const label = result.matched
        ? `${result.fullName || result.studentCode} (${Math.round((result.confidence || 0) * 100)}%)`
        : (result.message || 'Đang nhận diện...');
    overlayCtx.fillStyle = color;
    overlayCtx.font = '16px sans-serif';
    overlayCtx.fillText(label, x1 * scaleX, Math.max(16, y1 * scaleY - 8));
}

function handleMatchLogic(result, imageBase64) {
    if (!result || !result.matched) {
        consecutiveMatchState = { studentCode: null, count: 0 };
        return;
    }

    if (consecutiveMatchState.studentCode === result.studentCode) {
        consecutiveMatchState.count += 1;
    } else {
        consecutiveMatchState = { studentCode: result.studentCode, count: 1 };
    }

    if (consecutiveMatchState.count >= REQUIRED_CONSECUTIVE_MATCHES) {
        consecutiveMatchState = { studentCode: null, count: 0 };
        submitAttendance(result, imageBase64);
    }
}

async function submitAttendance(result, imageBase64) {
    const payload = {
        studentCode: result.studentCode,
        imageBase64,
        confidence: result.confidence,
        trackingId: crypto.randomUUID(),
        cameraId: 'web-camera',
        capturedAt: new Date().toISOString(),
    };

    try {
        const res = await postJson(ATTENDANCE_URL, payload);
        showAttendanceMessage(res.message || 'Điểm danh thành công', res.success ? 'success' : 'error');
        const faceStatus = document.getElementById('faceStatus');
        if (faceStatus) {
            faceStatus.textContent = res.success ? 'Đã xác thực' : 'Chờ duyệt';
        }
    } catch (error) {
        showAttendanceMessage('Lỗi điểm danh: ' + error.message, 'error');
    } finally {
        cooldownUntil = Date.now() + COOLDOWN_AFTER_SUCCESS_MS;
    }
}

async function postJson(url, body) {
    const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok){
        const errorMessage = data.message || ('HTTP ' + res.status);
        throw new Error(errorMessage);
    }
    return data;
}

function showAttendanceMessage(message, type) {
    const resultCard = document.getElementById('attendanceMessage');
    if (!resultCard) {
        return;
    }

    resultCard.textContent = message;
    resultCard.className = 'result-card ' + (type === 'success' ? 'success-result' : 'error-result');
    resultCard.style.display = 'block';

    clearTimeout(resultCard._hideTimer);
    resultCard._hideTimer = setTimeout(() => {
        resultCard.style.display = 'none';
    }, 3000);
}

function updateClock() {
    const clockEl = document.getElementById('clock');
    if (clockEl) {
        clockEl.textContent = new Date().toLocaleTimeString('vi-VN');
    }
}

window.addEventListener('beforeunload', () => {
    stopPolling();
    if (attendanceStream) {
        attendanceStream.getTracks().forEach(track => track.stop());
    }
});

/* ==================================================
   OFFLINE FLOW
================================================== */

function onIdentifySuccess() {
    // AI đã phản hồi bình thường -> hủy đếm ngược chuyển offline (nếu có)
    if (aiFailureTimer) {
        clearTimeout(aiFailureTimer);
        aiFailureTimer = null;
    }
    aiIsFailing = false;
}

function onIdentifyFailure() {
    if (appState !== APP_STATE.ONLINE) return;
    if (aiIsFailing) return; // đã đang đếm ngược rồi, không đặt lại

    aiIsFailing = true;
    showAttendanceMessage('Không thể kết nối hệ thống nhận diện AI. Đang thử lại...', 'error');

    aiFailureTimer = setTimeout(() => {
        if (appState === APP_STATE.ONLINE) {
            enterOfflineMode();
        }
    }, AI_OFFLINE_TIMEOUT_MS);
}

function initOfflineFlowEvents() {
    const btnReconnect = document.getElementById('btnReconnect');
    const btnManualCapture = document.getElementById('btnManualCapture');
    const btnBackToList = document.getElementById('btnBackToList');
    const employeeSearch = document.getElementById('employeeSearch');

    if (btnReconnect) {
        btnReconnect.addEventListener('click', () => {
            // Kết nối lại AI = tải lại trang, quay về chế độ online từ đầu
            window.location.reload();
        });
    }

    if (btnManualCapture) {
        btnManualCapture.addEventListener('click', manualCaptureAttendance);
    }

    if (btnBackToList) {
        btnBackToList.addEventListener('click', backToEmployeeList);
    }

    if (employeeSearch) {
        employeeSearch.addEventListener('input', () => {
            renderEmployeeList(filterEmployees(employeeSearch.value));
        });
    }
}

function enterOfflineMode() {
    appState = APP_STATE.OFFLINE_LIST;
    stopPolling();
    overlayCtx.clearRect(0, 0, overlayCanvas.width, overlayCanvas.height);

    const onlineControls = document.getElementById('onlineControls');
    const offlinePanel = document.getElementById('offlinePanel');
    const manualControls = document.getElementById('manualControls');

    if (onlineControls) onlineControls.classList.add('hidden');
    if (manualControls) manualControls.classList.add('hidden');
    if (offlinePanel) offlinePanel.classList.remove('hidden');

    showAttendanceMessage('Đã chuyển sang điểm danh thủ công do mất kết nối AI.', 'error');
    loadEmployeeList();
}

async function loadEmployeeList() {
    const listEl = document.getElementById('employeeList');
    if (listEl) {
        listEl.innerHTML = '<div class="employee-empty">Đang tải danh sách nhân viên...</div>';
    }

    try {
        const data = await get(EMPLOYEE_LIST_URL);
        employeeCache = Array.isArray(data) ? data : (data.items || []);
        renderEmployeeList(employeeCache);
    } catch (error) {
        console.error('Không thể tải danh sách nhân viên:', error);
        if (listEl) {
            listEl.innerHTML = '<div class="employee-empty">Không thể tải danh sách nhân viên. Vui lòng thử "Kết nối lại".</div>';
        }
    }
}

function normalizeText(text) {
    return (text || '')
        .toString()
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '');
}

function filterEmployees(keyword) {
    const kw = normalizeText(keyword).trim();
    if (!kw) return employeeCache;
    return employeeCache.filter((emp) => {
        return normalizeText(emp.fullName).includes(kw) || normalizeText(emp.code).includes(kw);
    });
}

function renderEmployeeList(employees) {
    const listEl = document.getElementById('employeeList');
    if (!listEl) return;

    if (!employees || employees.length === 0) {
        listEl.innerHTML = '<div class="employee-empty">Không tìm thấy nhân viên phù hợp.</div>';
        return;
    }

    listEl.innerHTML = '';
    employees.forEach((emp) => {
        const item = document.createElement('div');
        item.className = 'employee-item';
        item.innerHTML = `
            <div class="employee-info">
                <div class="employee-name"></div>
                <div class="employee-code"></div>
            </div>
            <div class="employee-pick">Chọn ›</div>
        `;
        item.querySelector('.employee-name').textContent = emp.fullName || '--';
        item.querySelector('.employee-code').textContent = 'Mã NV: ' + (emp.code || '--');
        item.addEventListener('click', () => selectEmployee(emp));
        listEl.appendChild(item);
    });
}

async function selectEmployee(emp) {
    selectedEmployee = emp;
    appState = APP_STATE.OFFLINE_MANUAL;

    const offlinePanel = document.getElementById('offlinePanel');
    const manualControls = document.getElementById('manualControls');
    const badge = document.getElementById('selectedEmployeeBadge');

    if (offlinePanel) offlinePanel.classList.add('hidden');
    if (manualControls) manualControls.classList.remove('hidden');
    if (badge) badge.textContent = 'Đã chọn: ' + (emp.fullName || '--') + ' (' + (emp.code || '--') + ')';

    // Đảm bảo camera vẫn đang bật để chụp ảnh thủ công
    if (!attendanceStream) {
        await startAttendanceCamera();
    }
}

function backToEmployeeList() {
    selectedEmployee = null;
    appState = APP_STATE.OFFLINE_LIST;

    const offlinePanel = document.getElementById('offlinePanel');
    const manualControls = document.getElementById('manualControls');

    if (manualControls) manualControls.classList.add('hidden');
    if (offlinePanel) offlinePanel.classList.remove('hidden');
}

async function manualCaptureAttendance() {
    if (!selectedEmployee) {
        showAttendanceMessage('Vui lòng chọn nhân viên trước khi chụp ảnh.', 'error');
        return;
    }
    if (appState !== APP_STATE.OFFLINE_MANUAL) {
        showAttendanceMessage('Chế độ điểm danh thủ công chưa sẵn sàng.', 'error');
        return;
    }
    if (!attendanceVideo || attendanceVideo.readyState < HTMLMediaElement.HAVE_CURRENT_DATA || !attendanceVideo.videoWidth) {
        showAttendanceMessage('Camera chưa sẵn sàng. Vui lòng thử lại.', 'error');
        return;
    }

    const btnManualCapture = document.getElementById('btnManualCapture');
    if (btnManualCapture) btnManualCapture.disabled = true;

    stopPolling();
    const imageBase64 = captureFrame();
    if (!imageBase64) {
        showAttendanceMessage('Không thể chụp ảnh từ camera. Vui lòng thử lại.', 'error');
        if (btnManualCapture) btnManualCapture.disabled = false;
        return;
    }
    const payload = {
        employeeCode: selectedEmployee.code,
        imageBase64,
        trackingId: crypto.randomUUID(),
        cameraId: 'web-camera-manual',
        capturedAt: new Date().toISOString(),
    };

    try {
        const res = await postJson(MANUAL_ATTENDANCE_URL, payload);
        showAttendanceMessage(res.message || 'Điểm danh thủ công thành công', res.success !== false ? 'success' : 'error');
        setTimeout(backToEmployeeList, 1500);
    } catch (error) {
        showAttendanceMessage('Lỗi điểm danh: ' + error.message, 'error');
    } finally {
        if (btnManualCapture) btnManualCapture.disabled = false;
    }
}