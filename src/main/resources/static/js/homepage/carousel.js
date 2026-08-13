document.addEventListener('DOMContentLoaded', function () {
    const window_ = document.querySelector('.carousel-window');
    const track = document.querySelector('.carousel-track');
    const slides = Array.from(document.querySelectorAll('.carousel-slide'));
    const prevBtn = document.querySelector('.carousel-prev');
    const nextBtn = document.querySelector('.carousel-next');

    const total = slides.length;
    let currentIndex = 0;
    let autoplayTimer = null;
    const AUTOPLAY_DELAY = 4000;

    function render() {
        slides.forEach((slide, i) => {
            // khoảng cách ngắn nhất tới slide hiện tại (có wrap-around)
            let diff = i - currentIndex;
            if (diff > total / 2) diff -= total;
            if (diff < -total / 2) diff += total;

            const abs = Math.abs(diff);
            const translateX = diff * 62;      // % chiều rộng slide
            const scale = 1 - abs * 0.22;
            const rotateY = diff * -32;         // deg
            const opacity = abs === 0 ? 1 : abs === 1 ? 0.55 : 0;
            const zIndex = 10 - abs;
            const blur = abs === 0 ? 0 : 2;

            slide.style.transform =
                `translate(-50%, -50%) translateX(${translateX}%) scale(${scale}) rotateY(${rotateY}deg)`;
            slide.style.opacity = opacity;
            slide.style.zIndex = zIndex;
            slide.style.filter = `blur(${blur}px)`;
            slide.style.pointerEvents = abs === 0 ? 'auto' : 'none';

            slide.classList.toggle('is-active', abs === 0);
        });
    }

    function showPrev() {
        currentIndex = (currentIndex - 1 + total) % total;
        render();
    }

    function showNext() {
        currentIndex = (currentIndex + 1) % total;
        render();
    }

    function startAutoplay() {
        stopAutoplay();
        autoplayTimer = setInterval(showNext, AUTOPLAY_DELAY);
    }

    function stopAutoplay() {
        if (autoplayTimer) {
            clearInterval(autoplayTimer);
            autoplayTimer = null;
        }
    }

    function resetAutoplay() {
        startAutoplay();
    }

    // click nút điều hướng -> chạy tay + reset lại bộ đếm 4s
    prevBtn?.addEventListener('click', () => { showPrev(); resetAutoplay(); });
    nextBtn?.addEventListener('click', () => { showNext(); resetAutoplay(); });

    // click vào ảnh bên cạnh -> nhảy tới ảnh đó luôn
    slides.forEach((slide, i) => {
        slide.addEventListener('click', () => {
            if (i !== currentIndex) {
                currentIndex = i;
                render();
                resetAutoplay();
            }
        });
    });

    // dừng auto-play khi hover, chạy lại khi rời chuột
    window_?.addEventListener('mouseenter', stopAutoplay);
    window_?.addEventListener('mouseleave', startAutoplay);

    // dừng khi tab không active để đỡ tốn tài nguyên
    document.addEventListener('visibilitychange', () => {
        if (document.hidden) stopAutoplay();
        else startAutoplay();
    });

    render();
    startAutoplay();
});