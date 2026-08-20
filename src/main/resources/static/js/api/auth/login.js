import {post} from "../method.js"
export async function login(request) {
    return post(
        "/api/auth/login", request
    );
}

document.getElementById("loginForm").addEventListener("submit", async function(event) {
    event.preventDefault(); 
    const formData = new FormData(event.target);
    const requestData = Object.fromEntries(formData);

    try {
        const result = await login(requestData);
        console.log("Đăng nhập thành công!", result);
        window.location.href = "/home"; 
    } catch (error) {
        console.error("Lỗi:", error.message);
        alert("Đăng nhập thất bại: " + error.message);
    }
});