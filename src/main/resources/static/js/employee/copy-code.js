const copyBtn = document.getElementById("copyBtn");

copyBtn.addEventListener("click", () => {
    navigator.clipboard.writeText(
        document.getElementById("roomCode").textContent
    );
});