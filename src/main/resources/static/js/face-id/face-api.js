/**
 * API calls for face-id module
 */

const FaceIdApi = {
    /**
     * Đăng ký khuôn mặt (5 ảnh mẫu)
     */
    async registerFace(samples, roomCode) {
        return await post('/api/face-id/register', {
            samples: samples,
            roomCode: roomCode
        });
    },

    /**
     * Lấy ảnh mới nhất của user
     */
    async getLatestFace() {
        return await get('/api/face-id/latest');
    },

    /**
     * Cập nhật ảnh cho điểm danh (xóa ảnh cũ, lưu ảnh mới)
     */
    async updateForAttendance(base64Image) {
        return await post('/api/face-id/update-for-attendance', {
            samples: [base64Image]
        });
    },

    /**
     * Xóa ảnh mẫu
     */
    async deleteSample(sampleId) {
        return await fetch('/api/face-id/sample/' + sampleId, {
            method: 'DELETE'
        });
    }
};