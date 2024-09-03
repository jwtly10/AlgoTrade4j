import React, {useMemo} from 'react';
import {Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis,} from 'recharts';
import {format} from 'date-fns';

export const EquityChart = ({equityHistory}) => {
    const formatXAxis = (timestamp) => {
        return format(new Date(timestamp), 'MMM d, yyyy');
    };

    const formatYAxis = (value) => {
        return `$${value.toLocaleString()}`;
    };

    const formatTooltipValue = (value) => {
        return `$${value.toLocaleString()}`;
    };

    const decimateData = (data, maxPoints = 1000) => {
        if (data.length <= maxPoints) return data;
        const skip = Math.ceil(data.length / maxPoints);
        return data.filter((_, index) => index % skip === 0);
    };

    const data = useMemo(() => {
        const rawData = equityHistory.map((point) => ({
            timestamp: point.timestamp * 1000,
            equity: point.equity,
        }));
        return decimateData(rawData);
    }, [equityHistory]);

    return (
        <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={data} margin={{top: 10, right: 30, left: 15, bottom: 0}}>
                <defs>
                    <linearGradient id="colorEquity" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#8884d8" stopOpacity={0.8}/>
                        <stop offset="95%" stopColor="#8884d8" stopOpacity={0}/>
                    </linearGradient>
                </defs>
                <XAxis
                    dataKey="timestamp"
                    tickFormatter={formatXAxis}
                    type="number"
                    scale="time"
                    domain={['dataMin', 'dataMax']}
                />
                <YAxis tickFormatter={formatYAxis}/>
                <CartesianGrid strokeDasharray="3 3"/>
                <Tooltip
                    labelFormatter={formatXAxis}
                    formatter={(value) => [formatTooltipValue(value), 'Equity']}
                />
                <Area
                    type="monotone"
                    dataKey="equity"
                    stroke="#8884d8"
                    fillOpacity={1}
                    fill="url(#colorEquity)"
                    isAnimationActive={false}
                />
            </AreaChart>
        </ResponsiveContainer>
    );
};