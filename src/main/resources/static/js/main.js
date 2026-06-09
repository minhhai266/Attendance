/**
 * FACE ID ATTENDANCE SYSTEM
 * Main JavaScript - Theme Toggle & Utilities
 */

// ===========================
// THEME MANAGEMENT
// ===========================

function initTheme() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeIcon(savedTheme);
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    updateThemeIcon(newTheme);
}

function updateThemeIcon(theme) {
    const themeBtn = document.getElementById('theme-toggle');
    if (themeBtn) {
        const icon = themeBtn.querySelector('svg');
        if (icon) {
            icon.innerHTML = theme === 'light'
                ? '<path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>'
                : '<circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line>';
        }
    }
}

// Clock, modals, validation
function updateClock() {
    const clockElements = document.querySelectorAll('.clock');
    if (clockElements.length === 0) return;
    const now = new Date();
    clockElements.forEach(clock => {
        const timeEl = clock.querySelector('.clock-time');
        const dateEl = clock.querySelector('.clock-date');
        if (timeEl) timeEl.textContent = now.toLocaleTimeString('vi-VN');
        if (dateEl) dateEl.textContent = now.toLocaleDateString('vi-VN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
    });
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

function showAlert(message, type = 'info') {
    alert(message);
}

function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;
    input.type = input.type === 'password' ? 'text' : 'password';
}

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    initTheme();
    updateClock();
    setInterval(updateClock, 1000);
});

window.AttendanceSystem = {
    toggleTheme,
    openModal,
    closeModal,
    showAlert,
    togglePassword
};
