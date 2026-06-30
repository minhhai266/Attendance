const popup = document.getElementById("card-addEmployee");

function openPopup() {
    popup.classList.add("show");
}

function closePopup() {
    popup.classList.remove("show");
}

// Mở popup
document.getElementById("add-employee")
    .addEventListener("click", openPopup);

// Đóng bằng nút X
document.getElementById("closePopup")
    .addEventListener("click", closePopup);

// Đóng khi click nền tối
popup.addEventListener("click", (e) => {
    if (e.target === popup) {
        closePopup();
    }
});