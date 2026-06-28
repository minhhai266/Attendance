
document.addEventListener("DOMContentLoaded", function () {

    const ctx = document.getElementById("attendanceTrendChart");

    new Chart(ctx, {
        type: "line",
        data: {
            labels: ["Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6"],
            datasets: [{
                label: "Tỷ lệ chuyên cần (%)",
                data: [92, 94, 95, 93, 97, 98],
                borderColor: "#3b82f6",
                backgroundColor: "rgba(59,130,246,0.15)",
                borderWidth: 3,
                tension: 0.4,
                fill: true,
                pointRadius: 5,
                pointHoverRadius: 7
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true
                }
            },
            scales: {
                y: {
                    beginAtZero: false,
                    min: 80,
                    max: 100,
                    ticks: {
                        callback: function(value) {
                            return value + "%";
                        }
                    }
                }
            }
        }
    });

});
