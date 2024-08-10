import React, { useEffect, useRef } from 'react';
import Chart from 'chart.js/auto';
import 'chartjs-adapter-date-fns';


export const EquityChart = ({ equityHistory }) => {
    const chartRef = useRef(null);
    const chartInstance = useRef(null);

    useEffect(() => {
        if (chartInstance.current) {
            chartInstance.current.destroy();
        }

        const ctx = chartRef.current.getContext('2d');

        const chartData = equityHistory.map(point => ({
            x: point.timestamp * 1000,
            y: point.equity.value
        }));

        chartInstance.current = new Chart(ctx, {
            type: 'line',
            data: {
                datasets: [{
                    label: 'Equity',
                    data: chartData,
                    borderColor: 'rgb(75, 192, 192)',
                    tension: 0.1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    x: {
                        type: 'time',
                        time: {
                            unit: 'day'
                        },
                        title: {
                            display: true,
                            text: 'Date'
                        }
                    },
                    y: {
                        title: {
                            display: true,
                            text: 'Equity'
                        }
                    }
                },
                plugins: {
                    title: {
                        display: true,
                        text: 'Equity Over Time'
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                let label = context.dataset.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                if (context.parsed.y !== null) {
                                    label += new Intl.NumberFormat('en-US', {style: 'currency', currency: 'USD'}).format(context.parsed.y);
                                }
                                return label;
                            }
                        }
                    }
                }
            }
        });

        return () => {
            if (chartInstance.current) {
                chartInstance.current.destroy();
            }
        };
    }, [equityHistory]);

    return <canvas ref={chartRef} />;
};