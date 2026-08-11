document.addEventListener('DOMContentLoaded', function () {
    const track = document.querySelector('.carousel-track');
    const slides = Array.from(document.querySelectorAll('.carousel-slide'));
    const prevBtn = document.querySelector('.carousel-prev');
    const nextBtn = document.querySelector('.carousel-next');
    let currentIndex = 0;

    function updateCarousel() {
        const width = slides[0].getBoundingClientRect().width;
        track.style.transform = `translateX(-${currentIndex * width}px)`;
    }

    function showPrev() {
        currentIndex = (currentIndex - 1 + slides.length) % slides.length;
        updateCarousel();
    }

    function showNext() {
        currentIndex = (currentIndex + 1) % slides.length;
        updateCarousel();
    }

    prevBtn?.addEventListener('click', showPrev);
    nextBtn?.addEventListener('click', showNext);

    window.addEventListener('resize', updateCarousel);
    updateCarousel();
});
