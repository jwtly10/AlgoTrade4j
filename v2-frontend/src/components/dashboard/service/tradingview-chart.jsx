'use client';

import React, { useEffect, useRef } from 'react';
import { ColorType, createChart, CrosshairMode, TickMarkType } from 'lightweight-charts';

import { logger } from '@/lib/default-logger';
import { useColorScheme } from '@mui/material';

export function TradingViewChart({ showChart, strategyConfig, chartData, trades, indicators }) {
  const chartContainerRef = useRef();
  const chartRef = useRef(null);
  const candlestickSeriesRef = useRef(null);
  const indicatorSeriesRef = useRef({});

  const { colorScheme } = useColorScheme();

  const colors = {
    background: colorScheme === 'dark' ? '#121517' : '#FFFFFF',
    textColor: colorScheme === 'dark' ? '#D9D9D9' : '#121212',
    watermark: colorScheme === 'dark' ? 'rgba(255, 255, 255, 0.1)': 'rgba(0,0,0,0.1)',
  };

  useEffect(() => {
    if (!showChart || chartRef.current || !strategyConfig || chartData.length === 0) {
      logger.debug(
        `Not showing chart due to missing data: Showchart: ${showChart}, StrategyConfiguration undefined?: ${strategyConfig === undefined}, CharData Length: ${chartData.length}`
      );
      return;
    }

    const instrument = strategyConfig.instrumentData;
    const period = strategyConfig.period;
    const pricePrecision = instrument.decimalPlaces || 2;
    const minMove = instrument.minimumMove || 0.01;

    const chart = createChart(chartContainerRef.current, {
      width: chartContainerRef.current.clientWidth,
      height: 500,
      layout: {
        background: { type: ColorType.Solid, color: colors.background },
        textColor: colors.textColor,
      },
      timeScale: {
        timeVisible: true,
        secondsVisible: false,
        tickMarkFormatter: (time, tickMarkType, locale) => {
          const localdate = new Date(time * 1000);
          const date = new Date(
            localdate.getUTCFullYear(),
            localdate.getUTCMonth(),
            localdate.getUTCDate(),
            localdate.getUTCHours(),
            localdate.getUTCMinutes(),
            localdate.getUTCSeconds()
          );
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
              }
              return `${hours}:00`;
            }
            return `${hours}:${minutes}`;
          }

          // Default case
          return `${month}-${day} ${hours}:${minutes}`;
        },
      },
      watermark: {
        color: colors.watermark,
        visible: true,
        text: chartData.length > 0 ? `${instrument.internalSymbol}, ${period}` : '',
        fontSize: 80,
        horzAlign: 'center',
        vertAlign: 'center',
      },
      grid: {
        vertLines: { color: 'rgba(197, 203, 206, 0.1)' },
        horzLines: { color: 'rgba(197, 203, 206, 0.1)' },
      },
      rightPriceScale: {
        borderColor: 'rgba(197, 203, 206, 0.3)',
        borderVisible: true,
        scaleMargins: {
          top: 0.1,
          bottom: 0.1,
        },
        minMove,
        precision: pricePrecision,
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
        vertLine: { color: '#758696', width: 1, style: 3, labelBackgroundColor: '#1E222D' },
        horzLine: { color: '#758696', width: 1, style: 3, labelBackgroundColor: '#1E222D' },
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
    });

    const candlestickSeries = chart.addCandlestickSeries({
      upColor: '#26a69a',
      downColor: '#ef5350',
      borderVisible: false,
      wickUpColor: '#26a69a',
      wickDownColor: '#ef5350',
      priceFormat: {
        type: 'price',
        precision: pricePrecision,
        minMove,
      },
    });

    chartRef.current = chart;
    candlestickSeriesRef.current = candlestickSeries;

    const handleResize = () => {
      chart.applyOptions({
        width: chartContainerRef.current.clientWidth,
        height: chartContainerRef.current.clientHeight,
      });
    };

    window.addEventListener('resize', handleResize);
    handleResize();

    return () => {
      window.removeEventListener('resize', handleResize);
      chart.remove();
      chartRef.current = null;
      candlestickSeriesRef.current = null;
      indicatorSeriesRef.current = {};
    };
  }, [showChart, strategyConfig]);

  useEffect(() => {
    if (!chartRef.current || !candlestickSeriesRef.current) {
      return;
    }

    try {
      candlestickSeriesRef.current.setData(chartData);
    } catch (e) {
      logger.error('Failed to set data:', e);
      logger.debug('Chart Data:', chartData);
    }

    // Update indicators
    Object.keys(indicators).forEach((indicatorName) => {
      const indicatorData = indicators[indicatorName];
      if (indicatorData && indicatorData.length > 0) {
        if (indicatorName.startsWith('ATR_CANDLE')) {
          const modifiedChartData = chartData.map((candle) => {
            const correspondingIndicator = indicatorData.find((item) => item.time === candle.time);
            if (correspondingIndicator && correspondingIndicator.value === 1) {
              return { ...candle, color: 'blue' };
            }
            return candle;
          });
          candlestickSeriesRef.current.setData(modifiedChartData);
        } else {
          const validData = indicatorData
            .filter((item) => !isNaN(item.time) && !isNaN(item.value) && item.value !== 0)
            .sort((a, b) => a.time - b.time);

          if (validData.length > 0) {
            const seriesName = `SMA${indicatorName}`;
            if (!indicatorSeriesRef.current[seriesName]) {
              indicatorSeriesRef.current[seriesName] = chartRef.current.addLineSeries({
                color: getIndicatorColor(seriesName),
                lineWidth: 2,
              });
            }
            indicatorSeriesRef.current[seriesName].setData(validData);
          }
        }
      }
    });

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
      candlestickSeriesRef.current.setMarkers(allMarkers);
    } catch (e) {
      logger.error('Failed to set trade markers:', e);
      logger.debug('All Markers:', allMarkers);
    }
  }, [chartData, trades, indicators]);

  // Helper functions
  const getIndicatorColor = (indicatorName) => {
    const colors = ['#2196F3', '#FF9800', '#4CAF50', '#E91E63', '#9C27B0'];
    return colors[indicatorName.length % colors.length];
  };

  return <div ref={chartContainerRef} style={{ width: '100%', height: '100%' }} />;
}

export default TradingViewChart;
