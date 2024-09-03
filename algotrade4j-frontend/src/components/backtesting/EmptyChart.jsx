import React from 'react';
import {Card, CardContent} from "@/components/ui/card.jsx";
import {BarChart3} from "lucide-react";

const EmptyChart = () => {

    return (
        <div className="w-full h-full flex items-center justify-center bg-background">
            <Card className="p-6 rounded-lg w-full max-w-[600px]">
                <CardContent>
                    <h2 className="text-2xl font-semibold text-center mb-2">
                        No Chart Data Available
                    </h2>
                    <p className="text-muted-foreground text-center mb-6">
                        Run a strategy to see chart data
                    </p>

                    <div className="flex justify-center my-8">
                        <BarChart3 className="w-16 h-16 text-muted-foreground"/>
                    </div>

                    <div className="space-y-4 mt-4">
                        <p className="text-sm text-center">
                            Select a strategy and run it to populate the chart with data.
                        </p>
                        <p className="text-sm text-center">
                            You'll be able to view trade information and analysis once data is available.
                        </p>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
};

export default EmptyChart;