/**
 * Attendance check - Face scanning for check-in/out
 */

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
            startPolling();
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
    if (!attendanceVideo || !attendanceVideo.videoWidth) return;
    if (Date.now() < cooldownUntil) return;

    const imageBase64 = captureFrame();
    try {
        const result = await postJson(IDENTIFY_URL, { imageBase64 });
        drawOverlay(result);
        handleMatchLogic(result, imageBase64);
    } catch (error) {
        console.error('identify lỗi:', error);
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