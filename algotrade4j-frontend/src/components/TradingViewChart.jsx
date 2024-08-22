import React, {useEffect, useRef} from 'react';
import {ColorType, createChart, CrosshairMode, TickMarkType} from 'lightweight-charts';
import {Box} from "@mui/material";

const TradingViewChart = ({chartData, trades, indicators}) => {
    const chartContainerRef = useRef();

    useEffect(() => {
        const handleResize = () => {
            chart.applyOptions({
                width: chartContainerRef.current.clientWidth,
                height: chartContainerRef.current.clientHeight,
            });
        };

        const chart = createChart(chartContainerRef.current, {
            width: chartContainerRef.current.clientWidth,
            height: 500,
            layout: {
                background: {type: ColorType.Solid, color: '#121212'},
                textColor: '#D9D9D9',
            },
            timeScale: {
                timeVisible: true,
                secondsVisible: false,
                tickMarkFormatter: (time, tickMarkType, locale) => {
                    const localdate = new Date(time * 1000);
                    const date = new Date(localdate.getUTCFullYear(), localdate.getUTCMonth(), localdate.getUTCDate(),
                        localdate.getUTCHours(), localdate.getUTCMinutes(), localdate.getUTCSeconds());
                    const month = (date.getMonth() + 1).toString().padStart(2, '0');
                    const day = date.getDate().toString().padStart(2, '0');
                    const hours = date.getHours();
                    const minutes = date.getMinutes().toString().padStart(2, '0');

                    if (tickMarkType === TickMarkType.Year) {
                        return date.getFullYear().toString();
                    } else if (tickMarkType === TickMarkType.Month) {
                        return `${month}-${day}`;
                    } else if (tickMarkType === TickMarkType.DayOfMonth) {
                        return `${month}-${day}`;
                    } else if (tickMarkType === TickMarkType.Time) {
                        if (minutes === '00') {
                            if (hours === 0) {
                                return `${month}-${day}`;
                            } else if (hours % 12 === 0) {
                                return hours === 12 ? '12:00' : '00:00';
                            } else {
                                return `${hours}:00`;
                            }
                        } else {
                            return `${hours}:${minutes}`;
                        }
                    }

                    // Default case
                    return `${month}-${day} ${hours}:${minutes}`;
                },
            },
            watermark: {
                color: 'rgba(255, 255, 255, 0.1)',
                visible: true,
                text: chartData.length > 0 ? chartData[0].instrument : '',
                fontSize: 80,
                horzAlign: 'center',
                vertAlign: 'center',
            },
            grid: {
                vertLines: {color: 'rgba(197, 203, 206, 0.1)'},
                horzLines: {color: 'rgba(197, 203, 206, 0.1)'},
            },
        });

        chart.timeScale().applyOptions({
            rightOffset: 12,
            barSpacing: 8,
            borderColor: 'rgba(197, 203, 206, 0.3)',
        });

        chart.applyOptions({
            handleScroll: {
                mouseWheel: true,
                pressedMouseMove: true,
            },
            handleScale: {
                mouseWheel: true,
                pinch: true,
            },
            crosshair: {
                mode: CrosshairMode.Normal,
                vertLine: {color: '#758696', width: 1, style: 3, labelBackgroundColor: '#1E222D'},
                horzLine: {color: '#758696', width: 1, style: 3, labelBackgroundColor: '#1E222D'},
            },
            tooltip: {
                fontFamily: 'Arial',
                fontSize: 10,
                backgroundColor: 'rgba(30, 34, 45, 0.9)',
                borderColor: '#2962FF',
                textColor: '#D9D9D9',
            },
            legend: {
                visible: true,
                fontSize: 12,
                fontFamily: 'Arial',
                color: '#D9D9D9',
            },
            rightPriceScale: {
                borderColor: 'rgba(197, 203, 206, 0.3)',
                borderVisible: true,
                scaleMargins: {
                    top: 0.1,
                    bottom: 0.1,
                },
            },
        });

        window.addEventListener('resize', handleResize);
        handleResize();

        const candlestickSeries = chart.addCandlestickSeries({
            upColor: '#26a69a',
            downColor: '#ef5350',
            borderVisible: false,
            wickUpColor: '#26a69a',
            wickDownColor: '#ef5350',
        });

        try {
            candlestickSeries.setData(chartData);
        } catch (e) {
            console.error('Failed to set data:', e);
            console.log(chartData);
        }

        // Add indicator series
        const indicatorSeries = {};
        Object.keys(indicators).forEach((indicatorName) => {
            const indicatorData = indicators[indicatorName];
            if (indicatorData && indicatorData.length > 0) {
                // Filter out zero values and invalid entries
                const validData = indicatorData
                    .filter((item) => !isNaN(item.time) && !isNaN(item.value) && item.value !== 0)
                    .sort((a, b) => a.time - b.time);

                if (validData.length > 0) {
                    indicatorSeries[indicatorName] = chart.addLineSeries({
                        color: getIndicatorColor(indicatorName),
                        lineWidth: 2,
                    });
                    indicatorSeries[indicatorName].setData(validData);
                }
            }
        });

        // addTradePriceLines(chart, candlestickSeries, trades);

        const openMarkers = trades.map((trade) => ({
            time: trade.openTime,
            position: trade.position === 'long' ? 'belowBar' : 'aboveBar',
            color: trade.position === 'long' ? '#26a69a' : '#ef5350',
            shape: 'arrowUp',
            text: `#${trade.tradeId} OPEN ${trade.position.toUpperCase()} @ ${trade.entry}`,
        }));

        const closeMarkers = trades
            .filter((trade) => trade.closePrice && trade.closeTime)
            .map((trade) => ({
                time: trade.closeTime,
                position: trade.position === 'long' ? 'aboveBar' : 'belowBar',
                color: trade.position === 'long' ? '#ef5350' : '#26a69a',
                shape: 'arrowDown',
                text: `#${trade.tradeId} CLOSE ${trade.position.toUpperCase()} @ ${trade.closePrice}`,
            }));

        const allMarkers = [...openMarkers, ...closeMarkers].sort((a, b) => a.time - b.time);

        try {
            candlestickSeries.setMarkers(allMarkers);
        } catch (e) {
            console.error('Failed to set trade markers:', e);
            console.log(allMarkers);
        }

        return () => {
            window.removeEventListener('resize', handleResize);
            chart.remove();
        };
    }, [chartData, trades]);


    // Helper functions
    const getIndicatorColor = (indicatorName) => {
        const colors = ['#2196F3', '#FF9800', '#4CAF50', '#E91E63', '#9C27B0'];
        return colors[indicatorName.length % colors.length];
    };

    function addTradePriceLines(chart, candlestickSeries, trades) {
        trades.forEach((trade) => {
            const color = trade.position === 'long' ? '#26a69a' : '#ef5350';
            const priceLine = {
                price: trade.entry,
                color: color,
                lineWidth: 2,
                lineStyle: 2, // Dashed line
                axisLabelVisible: true,
                title: `#${trade.tradeId}`,
            };
            candlestickSeries.createPriceLine(priceLine);
        });
    }


    return <Box ref={chartContainerRef} style={{width: '100%', height: '100%'}}/>;
};

export default TradingViewChart;