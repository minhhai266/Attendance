var options = {
    chart: {
        type: 'line'
    },
    series: [{
        data: [93, 95, 94, 96, 97, 96]
    }],
    xaxis: {
        categories: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6']
    }
};

new ApexCharts(
    document.querySelector("#weeklyAttendanceChart"),
    options
).render();