document.addEventListener("DOMContentLoaded", function () {

    const ctx = document.getElementById("systemActivityChart");

    new Chart(ctx, {
        type: "bar",
        data: {
            labels: [
                "Tháng 1",
                "Tháng 2",
                "Tháng 3",
                "Tháng 4",
                "Tháng 5",
                "Tháng 6"
            ],
            datasets: [{
                label: "Số hoạt động",
                data: [1200, 1450, 1320, 1680, 1820, 2100],
                backgroundColor: "#3b82f6",
                borderRadius: 8,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.raw + " hoạt động";
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 500
                    },
                    grid: {
                        color: "#e5e7eb"
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            }
        }
    });

});