  const video = document.getElementById("video");
        const canvas = document.getElementById("captureCanvas");
        const toggleBtn = document.getElementById("faceRegisterToggleBtn");
        const captureBtn = document.getElementById("captureFaceBtn");
        const resetBtn = document.getElementById("resetFaceScanBtn");
        const rescanBtn = document.getElementById("rescanBtn");
        const viewDetailBtn = document.getElementById("viewFaceDetailBtn");

        const sampleCountBtn = document.getElementById("faceSampleCount");
        const sampleStatusText = document.getElementById("faceSampleStatusText");
        const scanGuideCurrent = document.getElementById("scanGuideCurrent");
        const scanGuideSteps = document.getElementById("scanGuideSteps");
        const messageBox = document.getElementById("faceRegisterMessage");
        const detectionResult = document.getElementById("detectionResult");
        const detectedFace = document.getElementById("detectedFace");
        const detectionConfidence = document.getElementById("detectionConfidence");
        const statusText = document.getElementById("statusText");
        const progressText = document.getElementById("progressText");
        const scanStepsListItems = document.querySelectorAll("#scanStepsList .scan-step");

        let stream = null;
        let capturedSamples = [];
        let isStreaming = false;
        let pollTimer = null;
        let stableCount = 0;
        let missingFaceCount = 0; // [MỚI] đếm số lần liên tiếp không thấy mặt trong khung hình
        const STABLE_NEEDED = 3;       // cần đúng tư thế liên tiếp 3 lần mới chụp (~1.2s)
        const POLL_INTERVAL_MS = 400;
        const MISSING_FACE_LIMIT = 3;  // [MỚI] mất mặt liên tiếp ~1.2s (3 x 400ms) mới coi là "out khỏi khung hình" và bắt reset

        // Thứ tự bước phải khớp với "pose" trả về từ AI: front, left, right, up, down
        // Thứ tự này BẮT BUỘC phải làm tuần tự: xong bước i mới được sang bước i+1.
        const requiredSteps = ["front", "left", "right", "up", "down"];
        const guideTextMap = {
            front: "Nhìn thẳng vào camera",
            left: "Quay mặt sang trái",
            right: "Quay mặt sang phải",
            up: "Ngẩng mặt lên",
            down: "Cúi mặt xuống",
        };

        function setMessage(text, type = "info") {
            messageBox.innerHTML = text;
            messageBox.className = `message ${type}`;
        }

        function currentStepIndex() {
            return capturedSamples.length;
        }

        function currentStepKey() {
            return requiredSteps[currentStepIndex()];
        }

        function updateGuideText() {
            const idx = currentStepIndex();
            if (idx >= requiredSteps.length) {
                scanGuideCurrent.textContent = "Hoàn thành đăng ký";
                scanGuideSteps.textContent = "5 / 5";
                progressText.textContent = "5 / 5";
                return;
            }
            scanGuideCurrent.textContent = guideTextMap[requiredSteps[idx]];
            scanGuideSteps.textContent = `Bước ${idx + 1} / 5`;
            progressText.textContent = `Bước ${idx + 1} / 5`;
        }

        // Cập nhật danh sách bước bên phải: bước đã xong -> done,
        // bước hiện tại -> active, các bước sau -> locked (chưa được làm tới).
        function updateStepsList() {
            const idx = currentStepIndex(); // số mẫu đã chụp = số bước đã hoàn thành
            scanStepsListItems.forEach((li, i) => {
                li.classList.remove("is-done", "is-active", "is-locked");
                if (i < idx) {
                    li.classList.add("is-done");
                } else if (i === idx) {
                    li.classList.add("is-active");
                } else {
                    li.classList.add("is-locked");
                }
            });
        }

        // [MỚI] Xóa toàn bộ mẫu đã chụp và quay lại Bước 1, dùng chung cho các trường hợp
        // reset thủ công (nút Làm lại/Quét lại) lẫn reset tự động khi mặt out khỏi khung hình.
        function resetScanProcess(message, type = "error") {
            capturedSamples = [];
            stableCount = 0;
            missingFaceCount = 0;
            updateUI();
            setMessage(message, type);
        }

        function grabFrameBase64() {
            const ctx = canvas.getContext("2d");
            canvas.width = video.videoWidth || 640;
            canvas.height = video.videoHeight || 480;
            ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
            return canvas.toDataURL("image/jpeg", 0.85);
        }

        async function pollPose() {
            if (!isStreaming || capturedSamples.length >= 5) return;

            const imageBase64 = grabFrameBase64();

            try {
                const res = await fetch('/api/face-id/pose-check', {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ image_base64: imageBase64 }),
                });
                const data = await res.json();

                if (!data.ok) {
                    stableCount = 0;
                    missingFaceCount++; // [MỚI]
                    detectedFace.textContent = data.message || "Chưa phát hiện gương mặt";
                    detectedFace.style.color = "#e74c3c";
                    detectionConfidence.textContent = "";

                    // [MỚI] Mặt đã ra khỏi khung hình đủ lâu giữa lúc đang quét dở -> làm lại từ đầu.
                    if (missingFaceCount >= MISSING_FACE_LIMIT && capturedSamples.length > 0) {
                        resetScanProcess("⚠️ Khuôn mặt đã ra khỏi khung hình. Vui lòng quét lại từ Bước 1 / 5.");
                    }
                    return;
                }

                missingFaceCount = 0; // [MỚI] lại thấy mặt trong khung -> reset bộ đếm mất mặt

                // Chỉ chấp nhận đúng bước hiện tại theo thứ tự (không cho phép nhảy cóc/bỏ qua bước).
                const expected = currentStepKey();
                if (data.pose === expected) {
                    stableCount++;
                    detectedFace.textContent = `✅ Đúng tư thế (${stableCount}/${STABLE_NEEDED})`;
                    detectedFace.style.color = "#27ae60";
                    detectionConfidence.textContent = data.message || "";

                    if (stableCount >= STABLE_NEEDED) {
                        stableCount = 0;
                        capturedSamples.push(imageBase64);
                        updateUI();
                        setMessage(`✅ Đã thu thập mẫu ${capturedSamples.length}/5 (${guideTextMap[expected]})`, "success");

                        if (capturedSamples.length === 5) {
                            setMessage("✅ Đã thu thập đủ 5 mẫu, đang gửi...", "success");
                            await submitSamples();
                        }
                    }
                } else if (data.pose && requiredSteps.includes(data.pose)) {
                    // Người dùng đang làm sai thứ tự (ví dụ nhảy sang bước sau khi chưa xong bước hiện tại).
                    stableCount = 0;
                    detectedFace.textContent = `⚠️ Vui lòng thực hiện đúng thứ tự: ${guideTextMap[expected]} trước đã`;
                    detectedFace.style.color = "#e67e22";
                    detectionConfidence.textContent = "";
                } else {
                    stableCount = 0;
                    detectedFace.textContent = data.message || `Đang ở tư thế: ${data.pose || '?'}`;
                    detectedFace.style.color = "#3498db";
                    detectionConfidence.textContent = "";
                }
            } catch (error) {
                console.error("Pose check error:", error);
            }
        }

        function startPolling() {
            stopPolling();
            pollTimer = setInterval(pollPose, POLL_INTERVAL_MS);
        }

        function stopPolling() {
            if (pollTimer) {
                clearInterval(pollTimer);
                pollTimer = null;
            }
            stableCount = 0;
            missingFaceCount = 0; // [MỚI]
        }

        toggleBtn.addEventListener("click", async () => {
            if (stream) {
                stream.getTracks().forEach(track => track.stop());
                stream = null;
                video.srcObject = null;
                toggleBtn.textContent = "Bật camera";
                isStreaming = false;
                stopPolling();
                detectionResult.style.display = "none";
                setMessage("Camera đã được tắt.", "info");
                updateUI();
                return;
            }

            if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
                setMessage("❌ Trình duyệt không hỗ trợ camera.", "error");
                return;
            }

            try {
                stream = await navigator.mediaDevices.getUserMedia({
                    video: { facingMode: "user", width: { ideal: 640 }, height: { ideal: 480 } },
                    audio: false
                });

                video.srcObject = stream;
                await video.play().catch(() => { });
                toggleBtn.textContent = "Tắt camera";
                isStreaming = true;
                detectionResult.style.display = "block";
                setMessage("📹 Camera đã sẵn sàng. Làm theo hướng dẫn từng bước.", "info");
                updateUI();
                startPolling();
            } catch (error) {
                console.error(error);
                setMessage("❌ Không thể mở camera. Vui lòng kiểm tra quyền truy cập.", "error");
            }
        });

        resetBtn.addEventListener("click", () => {
            resetScanProcess("Đã làm lại quá trình quét.", "info"); // [MỚI] dùng chung hàm reset
        });

        rescanBtn.addEventListener("click", () => {
            resetScanProcess("Đã bắt đầu quét lại từ đầu.", "info"); // [MỚI] dùng chung hàm reset
        });

        async function submitSamples() {
            stopPolling();
            try {
                const response = await fetch('/api/face-id/register', {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ samples: capturedSamples }),
                });

                if (response.ok) {
                    const result = await response.json();
                    if (result.success) {
                        setMessage(`✅ ${result.message}`, "success");
                    } else {
                        setMessage(`❌ Lỗi: ${result.message}`, "error");
                    }
                } else {
                    setMessage("❌ Lỗi: Không thể gửi dữ liệu lên server", "error");
                }
            } catch (error) {
                console.error(error);
                setMessage(`❌ Lỗi kết nối: ${error.message}`, "error");
            }
        }

        function updateUI() {
            const count = capturedSamples.length;
            sampleCountBtn.textContent = `${count} / 5 mẫu`;
            sampleStatusText.textContent = count === 0 ? "Chưa có mẫu nào." : `Đã thu thập ${count}/5 mẫu khuôn mặt`;
            captureBtn.disabled = true;
            resetBtn.disabled = count === 0;
            rescanBtn.disabled = count === 0;
            updateGuideText();
            updateStepsList();
            statusText.textContent = !isStreaming ? "Chờ kích hoạt camera" : (count === 5 ? "Hoàn tất thu thập" : "Đang quét, làm theo hướng dẫn");
        }

        updateUI();