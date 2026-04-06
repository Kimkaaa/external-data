document.addEventListener("DOMContentLoaded", function () {
    const dashboardData = window.dashboardData || {};
    const kospiData = dashboardData.kospi || [];
    const kosdaqData = dashboardData.kosdaq || [];

    function formatNumber(value) {
        if (value === null || value === undefined || value === "") {
            return "";
        }

        const number = Number(value);
        if (Number.isNaN(number)) {
            return value;
        }

        return number.toLocaleString("ko-KR", {
            minimumFractionDigits: 0,
            maximumFractionDigits: 2
        });
    }

    function formatDate(value) {
        if (!value) {
            return "";
        }

        const dateString = String(value);

        if (/^\d{8}$/.test(dateString)) {
            const month = dateString.substring(4, 6);
            const day = dateString.substring(6, 8);
            return `${month}.${day}`;
        }

        return dateString;
    }

    function createChart(canvasId, data, label) {
        if (!data || data.length === 0) {
            return;
        }

        const canvas = document.getElementById(canvasId);
        if (!canvas) {
            return;
        }

        const labels = data.map(item => formatDate(item.date));
        const prices = data.map(item => Number(item.closePrice));
        const ctx = canvas.getContext("2d");

        new Chart(ctx, {
            type: "line",
            data: {
                labels: labels,
                datasets: [{
                    label: label,
                    data: prices,
                    tension: 0.2,
                    borderWidth: 2,
                    fill: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: "index",
                    intersect: false
                },
                plugins: {
                    tooltip: {
                        callbacks: {
                            title: function (tooltipItems) {
                                return tooltipItems[0]?.label || "";
                            },
                            label: function (context) {
                                return `${label}: ${formatNumber(context.raw)}`;
                            }
                        }
                    },
                    legend: {
                        display: false
                    }
                },
                scales: {
                    x: {
                        ticks: {
                            maxRotation: 0,
                            autoSkip: true,
                            maxTicksLimit: 6
                        },
                        grid: {
                            display: false
                        }
                    },
                    y: {
                        ticks: {
                            callback: function (value) {
                                return formatNumber(value);
                            }
                        }
                    }
                }
            }
        });
    }

    createChart("kospiChart", kospiData, "코스피");
    createChart("kosdaqChart", kosdaqData, "코스닥");
});