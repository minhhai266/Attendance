document.addEventListener("DOMContentLoaded", function () {

    const ctx = document.getElementById("accountTypeChart");

    new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: [
                "Quản trị viên",
                "Giảng viên",
                "Sinh viên"
            ],
            datasets: [{
                data: [5, 40, 320],
                backgroundColor: [
                    "#ef4444",
                    "#3b82f6",
                    "#10b981"
                ],
                borderWidth: 2,
                borderColor: "#ffffff"
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: "65%",
            plugins: {
                legend: {
                    position: "bottom",
                    labels: {
                        padding: 20,
                        usePointStyle: true,
                        pointStyle: "circle"
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.label + ": " + context.raw + " tài khoản";
                        }
                    }
                }
            }
        }
    });

});