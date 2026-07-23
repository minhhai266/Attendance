// Lấy dữ liệu từ biến global đã được HTML (Thymeleaf) nạp sẵn
const roomCodes = window.serverRoomCodes || {};

console.log("Dữ liệu đồng bộ từ Backend:", roomCodes);

// Logic xử lý giao diện của FE giữ nguyên
const roomSelect = document.getElementById("roomSelect");
const roomCode = document.getElementById("roomCode");

roomSelect.addEventListener("change", () => {
    roomCode.textContent = roomCodes[roomSelect.value] || "--------";
});