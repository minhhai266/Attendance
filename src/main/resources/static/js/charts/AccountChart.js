document.addEventListener("DOMContentLoaded", function () {

    const ctx = document.getElementById("accountTypeChart");
    const chartData = window.accountTypeChartData || {};
    const labels = chartData.labels || [];
    const values = chartData.values || [];

    new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: [
                    "#ef4444",
                    "#3b82f6",
                    "#10b981",
                    "#f59e0b",
                    "#8b5cf6",
                    "#06b6d4",
                    "#f97316",
                    "#64748b"
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
