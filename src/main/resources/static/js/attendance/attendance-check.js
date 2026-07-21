/**
 * Attendance check - Face scanning for check-in/out
 */

let attendanceStream = null;
let attendanceVideo = null;

// Khởi tạo camera khi trang load
document.addEventListener('DOMContentLoaded', () => {
    attendanceVideo = document.getElementById('camera');
    if (attendanceVideo) {
        startAttendanceCamera();
    }
});

async function startAttendanceCamera() {
    try {
        attendanceStream = await navigator.mediaDevices.getUserMedia({
            video: true,
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

// Chụp ảnh và gửi lên server cho điểm danh
async function captureAndCheckIn() {
    if (!attendanceVideo || !attendanceVideo.srcObject) {
        showAttendanceMessage('Camera chưa sẵn sàng', 'error');
        return;
    }

    const canvas = document.createElement('canvas');
    canvas.width = attendanceVideo.videoWidth;
    canvas.height = attendanceVideo.videoHeight;
    
    const ctx = canvas.getContext('2d');
    ctx.drawImage(attendanceVideo, 0, 0, canvas.width, canvas.height);
    
    const base64Image = canvas.toDataURL('image/jpeg');
    
    try {
        // Cập nhật ảnh face cho điểm danh
        const result = await post('/api/face-id/update-for-attendance', {
            samples: [base64Image]
        });
        
        if (result.success) {
            showAttendanceMessage('✅ Điểm danh thành công!', 'success');
            
            // Tự động gọi API check-in sau khi lưu ảnh
            await post('/attendance/check-in');
        } else {
            showAttendanceMessage('❌ Lỗi: ' + result.message, 'error');
        }
    } catch (error) {
        console.error('Lỗi điểm danh:', error);
        showAttendanceMessage('❌ Lỗi kết nối: ' + error.message, 'error');
    }
}

function showAttendanceMessage(message, type) {
    const resultCard = document.querySelector('.result-card');
    if (resultCard) {
        resultCard.textContent = message;
        resultCard.className = 'result-card ' + (type === 'success' ? 'success-result' : 'error-result');
        resultCard.style.display = 'block';
        
        setTimeout(() => {
            resultCard.style.display = 'none';
        }, 3000);
    }
}

// Dừng camera khi rời trang
window.addEventListener('beforeunload', () => {
    if (attendanceStream) {
        attendanceStream.getTracks().forEach(track => track.stop());
    }
});