import React, {useEffect, useState} from 'react';
import {Card, CardContent} from "@/components/ui/card.jsx";
import {Progress} from "@/components/ui/progress.jsx";

const LoadingChart = ({progressData, startTime}) => {
    const {
        percentageComplete,
        currentIndex,
        totalDays,
        fromDay,
        toDay,
        currentDay,
        instrument,
        ticksModelled,
        strategyId
    } = progressData || {};

    const [timeElapsed, setTimeElapsed] = useState('00:00');

    useEffect(() => {
        const updateTimeElapsed = () => {
            if (startTime) {
                const now = new Date();
                const start = new Date(startTime);
                const elapsedMilliseconds = now - start;
                const elapsedSeconds = Math.floor(elapsedMilliseconds / 1000);
                const minutes = Math.floor(elapsedSeconds / 60);
                const seconds = elapsedSeconds % 60;
                const newTimeElapsed = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
                setTimeElapsed(newTimeElapsed);
            }
        };

        updateTimeElapsed(); // Initial update
        const timer = setInterval(updateTimeElapsed, 1000);

        return () => clearInterval(timer);
    }, [startTime]);

    const formatDate = (timestamp) => {
        if (!timestamp) return '';
        return new Date(timestamp * 1000).toLocaleDateString();
    };

    function formatNumber(number) {
        if (!number) {
            return number
        }
        const decimalPlaces = 0;
        let [integerPart, decimalPart] = number.toFixed(decimalPlaces).split('.');
        integerPart = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
        return decimalPart ? `${integerPart}.${decimalPart}` : integerPart;
    }

    return (
        <div className="w-full h-full flex items-center justify-center">
            <Card className="p-6 max-w-[600px] w-full">
                <CardContent>
                    <h2 className="text-2xl font-semibold text-center mb-2">Strategy Running</h2>
                    <p className="text-muted-foreground text-center mb-6">{strategyId} on {instrument}</p>

                    <div className="my-8">
                        <Progress value={percentageComplete || 0} className="h-2"/>
                        <p className="text-sm text-muted-foreground text-right mt-2">
                            {percentageComplete?.toFixed(2)}% Complete
                        </p>
                    </div>

                    <div className="grid grid-cols-2 gap-4 mt-4">
                        <p className="text-sm">Progress:</p>
                        <p className="text-sm text-right">Day {currentIndex} of {totalDays}</p>
                        <p className="text-sm">Time Elapsed:</p>
                        <p className="text-sm text-right">{timeElapsed}</p>
                        <p className="text-sm">Start Date:</p>
                        <p className="text-sm text-right">{formatDate(fromDay)}</p>
                        <p className="text-sm">End Date:</p>
                        <p className="text-sm text-right">{formatDate(toDay)}</p>
                        <p className="text-sm">Current Date:</p>
                        <p className="text-sm text-right">{formatDate(currentDay)}</p>
                        <p className="text-sm">Ticks Modelled:</p>
                        <p className="text-sm text-right">{formatNumber(ticksModelled)}</p>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
};

export default LoadingChart;