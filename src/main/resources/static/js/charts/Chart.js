document.addEventListener("DOMContentLoaded", function () {

    const ctx = document.getElementById("weeklyAttendanceChart");

    new Chart(ctx, {
        type: "bar",
        data: {
            labels: weeklyStatsData.map(item => item.dayName),
            datasets: [
                {
                    label: "Có mặt",
                    data: weeklyStatsData.map(item => item.present),
                    backgroundColor: "#10b981",
                    borderRadius: 4
                },
                {
                    label: "Đi muộn",
                    data: weeklyStatsData.map(item => item.late),
                    backgroundColor: "#f59e0b",
                    borderRadius: 4
                },
                {
                    label: "Vắng mặt",
                    data: weeklyStatsData.map(item => item.absent),
                    backgroundColor: "#ef4444",
                    borderRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: "top"
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                }
            }
        }
    });

});
