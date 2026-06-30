const roomSelect = document.getElementById("roomSelect");
const roomCode = document.getElementById("roomCode");

const roomCodes = {
    DE211: "DE211-X7A9",
    DE212: "DE212-LP81",
    DE213: "DE213-KM32"
};

roomSelect.addEventListener("change", () => {
    roomCode.textContent =
        roomCodes[roomSelect.value] || "--------";
});