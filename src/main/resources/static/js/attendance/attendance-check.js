/**
 * Attendance check - Face scanning for check-in/out
 */

let attendanceStream = null;
let attendanceVideo = null;
let currentUser = null;
let isCheckingAttendance = false;

document.addEventListener('DOMContentLoaded', () => {
    attendanceVideo = document.getElementById('camera');
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
        captureBtn.addEventListener('click', captureAndCheckIn);
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
        attendanceStream.getTracks().forEach((track) => track.stop());
        attendanceStream = null;
        if (attendanceVideo) {
            attendanceVideo.srcObject = null;
        }
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
        }
    } catch (error) {
        console.error('Không thể mở camera:', error);
        showAttendanceMessage('Không thể mở camera. Vui lòng kiểm tra quyền truy cập.', 'error');
    }
}

async function captureAndCheckIn() {
    if (isCheckingAttendance) {
        return;
    }

    if (!attendanceVideo || !attendanceVideo.srcObject) {
        showAttendanceMessage('Camera chưa sẵn sàng. Vui lòng bật camera trước.', 'error');
        return;
    }

    if (!currentUser) {
        showAttendanceMessage('Bạn chưa có Face ID đã đăng ký. Hãy đăng ký trước khi điểm danh.', 'error');
        return;
    }

    isCheckingAttendance = true;
    showAttendanceMessage('Đang quét khuôn mặt...', 'success');

    try {
        const canvas = document.createElement('canvas');
        canvas.width = attendanceVideo.videoWidth || 640;
        canvas.height = attendanceVideo.videoHeight || 480;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(attendanceVideo, 0, 0, canvas.width, canvas.height);
        const base64Image = canvas.toDataURL('image/jpeg', 0.85);

        await post('/api/face-id/update-for-attendance', {
            samples: [base64Image]
        });

        const result = await post('/api/face-id/attendance', {
            studentCode: currentUser.username,
            confidence: 0.98,
            capturedAt: new Date().toISOString(),
            cameraId: 'webcam',
            trackingId: `${currentUser.username}-${Date.now()}`,
            liveness: true
        });

        if (result.success) {
            showAttendanceMessage(result.message || 'Điểm danh bằng Face ID thành công.', 'success');
            const faceStatus = document.getElementById('faceStatus');
            if (faceStatus) {
                faceStatus.textContent = 'Đã xác thực';
            }
        } else {
            showAttendanceMessage(result.message || 'Không thể xác thực khuôn mặt.', 'error');
        }
    } catch (error) {
        console.error('Lỗi điểm danh:', error);
        showAttendanceMessage('Lỗi điểm danh: ' + (error.message || 'Không rõ nguyên nhân'), 'error');
    } finally {
        isCheckingAttendance = false;
    }
}

function showAttendanceMessage(message, type) {
    const resultCard = document.getElementById('attendanceMessage');
    if (!resultCard) {
        return;
    }

    resultCard.textContent = message;
    resultCard.className = 'result-card ' + (type === 'success' ? 'success-result' : 'error-result');
    resultCard.style.display = 'block';

    setTimeout(() => {
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
    if (attendanceStream) {
        attendanceStream.getTracks().forEach(track => track.stop());
    }
});