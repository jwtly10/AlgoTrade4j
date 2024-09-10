import React from 'react';
import 'chartjs-adapter-date-fns';
import AnalysisReport from '../../components/backtesting/AnalysisReport.jsx';
import {EquityChart} from '../../components/backtesting/EquityChart.jsx';
import TradesTable from '../../components/backtesting/TradesTable.jsx';
import LogsTable from '../../components/backtesting/LogsTable.jsx';
import ConfigModal from '../../components/modals/ConfigModal.jsx';
import LoadingChart from "../../components/backtesting/LoadingChart.jsx";
import TradingViewChart from "../../components/backtesting/TradingViewChart.jsx";
import EmptyChart from "../../components/backtesting/EmptyChart.jsx";

import {Card} from "@/components/ui/card.jsx";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs.jsx";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select.jsx";
import {Button} from "@/components/ui/button.jsx";
import {CircleStop as StopIcon, PlayIcon, Settings} from "lucide-react";
import {useBacktest} from "@/hooks/use-backtest.js";

const BacktestView = () => {
    const {
        isStrategyRunning,
        account,
        trades,
        indicators,
        chartData,
        analysisData,
        equityHistory,
        logs,
        tabValue,
        setTabValue,
        isModalOpen,
        setIsModalOpen,
        strategies,
        strategyClass,
        isAsync,
        progressData,
        showChart,
        startTime,
        strategyConfig,
        setStrategyConfig,
        startStrategy,
        stopStrategy,
        handleOpenParams,
        handleConfigSave,
        handleChangeStrategy,
    } = useBacktest();

    return (
        <div className="flex flex-col h-[calc(100vh-32px-68px)] w-screen overflow-hidden p-4">
            {/* Main content area */}
            <div className="flex-grow flex overflow-hidden">
                {/* Left section (3/4 width) */}
                <div className="flex-grow h-full overflow-hidden">
                    <Card className="h-full flex flex-col p-6">
                        {/* Chart Section */}
                        <div className="flex-shrink-0 h-2/5 min-h-[500px] mb-6 bg-background rounded overflow-hidden">
                            {isStrategyRunning && isAsync ? (
                                <LoadingChart progressData={progressData} startTime={startTime}/>
                            ) : chartData && chartData.length > 0 ? (
                                <TradingViewChart showChart={showChart} strategyConfig={strategyConfig} chartData={chartData} trades={trades} indicators={indicators}/>
                            ) : (
                                <EmptyChart trades={trades} showChart={showChart}/>
                            )}
                        </div>

                        {/* Tabs and Content Section */}
                        <Tabs value={tabValue} onValueChange={setTabValue} className="flex-grow flex flex-col overflow-hidden">
                            <TabsList>
                                <TabsTrigger value="trades">Trades</TabsTrigger>
                                <TabsTrigger value="analysis">Analysis</TabsTrigger>
                                <TabsTrigger value="equity">Equity History</TabsTrigger>
                                <TabsTrigger value="logs">Logs</TabsTrigger>
                            </TabsList>
                            <div className="flex-grow overflow-hidden">
                                <TabsContent value="trades" className="h-full overflow-auto">
                                    <TradesTable trades={trades}/>
                                </TabsContent>
                                <TabsContent value="analysis" className="h-full overflow-auto">
                                    {analysisData !== null ? (
                                        <AnalysisReport data={analysisData}/>
                                    ) : (
                                        <p className="p-4 text-center">No analysis data available yet.</p>
                                    )}
                                </TabsContent>
                                <TabsContent value="equity" className="h-full">
                                    {equityHistory.length > 0 ? (
                                        <div className="h-full">
                                            <EquityChart equityHistory={equityHistory}/>
                                        </div>
                                    ) : (
                                        <p className="p-4 text-center">No equity history available yet.</p>
                                    )}
                                </TabsContent>
                                <TabsContent value="logs" className="h-full overflow-auto">
                                    {logs.length > 0 ? (
                                        <LogsTable logs={logs}/>
                                    ) : (
                                        <p className="p-4 text-center">Logs are available once you run a new strategy.</p>
                                    )}
                                </TabsContent>
                            </div>
                        </Tabs>
                    </Card>
                </div>

                {/* Right section (1/4 width) */}
                {/* Right section (responsive width) */}
                <div className="w-full md:w-1/3 lg:w-1/4 min-w-[280px] p-4 bg-background shadow overflow-auto">
                    <div className="flex flex-col h-full space-y-6">
                        <div className="bg-background rounded-lg shadow-sm p-4">
                            <h3 className="text-lg font-semibold mb-3">Account Summary</h3>
                            <div className="grid grid-cols-2 gap-3">
                                {[
                                    {label: 'Initial Balance', value: account.initialBalance.toLocaleString()},
                                    {
                                        label: 'Profit',
                                        value: account.balance !== 0.0 ? Math.round((account.balance - account.initialBalance + Number.EPSILON) * 100) / 100 : 0,
                                        diff: ((account.balance - account.initialBalance) / account.initialBalance * 100).toFixed(2)
                                    },
                                    {
                                        label: 'Current Balance',
                                        value: account.balance,
                                        diff: ((account.balance - account.initialBalance) / account.initialBalance * 100).toFixed(2)
                                    },
                                    {
                                        label: 'Equity',
                                        value: account.equity,
                                        diff: ((account.equity - account.initialBalance) / account.initialBalance * 100).toFixed(2)
                                    }
                                ].map((item, index) => (
                                    <div key={index} className="bg-card text-card-foreground rounded p-2">
                                        <p className="text-sm text-muted-foreground">{item.label}</p>
                                        <p className="text-md font-semibold">
                                            ${item.value}
                                            {item.diff !== undefined && account.balance !== 0 && (
                                                <span className={`ml-2 text-sm ${parseFloat(item.diff) >= 0 ? 'text-green-500' : 'text-red-500'}`}>
                    ({item.diff > 0 ? '+' : ''}{item.diff}%)
                </span>
                                            )}
                                        </p>
                                    </div>
                                ))}
                            </div>
                        </div>

                        <div className="space-y-4">
                            <h3 className="text-lg font-semibold">Strategy Control</h3>
                            <Select
                                value={strategyClass}
                                onValueChange={handleChangeStrategy}
                            >
                                <SelectTrigger className="w-full">
                                    <SelectValue placeholder="Select a strategy"/>
                                </SelectTrigger>
                                <SelectContent>
                                    {strategies.length > 0 ? (
                                        strategies.map((strategy, index) => (
                                            <SelectItem key={index} value={strategy || `strategy-${index}`}>
                                                {strategy}
                                            </SelectItem>
                                        ))
                                    ) : (
                                        <SelectItem value="no-strategies" disabled>
                                            No strategies available
                                        </SelectItem>
                                    )}
                                </SelectContent>
                            </Select>

                            <div className="flex flex-col sm:flex-row space-y-3 sm:space-y-0 sm:space-x-3">
                                <Button
                                    variant={isStrategyRunning ? "destructive" : "default"}
                                    onClick={isStrategyRunning ? stopStrategy : startStrategy}
                                    disabled={strategyClass === ""}
                                    size="lg"
                                    className="w-full sm:w-1/2"
                                >
                                    {isStrategyRunning ? (
                                        <>
                                            <StopIcon className="mr-2 h-4 w-4"/>
                                            Stop Strategy
                                        </>
                                    ) : (
                                        <>
                                            <PlayIcon className="mr-2 h-4 w-4"/>
                                            Start Strategy
                                        </>
                                    )}
                                </Button>

                                <Button
                                    variant="outline"
                                    onClick={handleOpenParams}
                                    disabled={strategyClass === ""}
                                    size="lg"
                                    className="w-full sm:w-1/2"
                                >
                                    <Settings className="mr-2 h-4 w-4"/>
                                    Configure
                                </Button>
                            </div>
                        </div>

                        <div className="flex-grow"/>

                        <p className="text-sm text-muted-foreground text-center">
                            Select a strategy and configure parameters before starting.
                        </p>
                    </div>
                </div>

            </div>

            {/* Modals */}
            <ConfigModal
                open={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSave={handleConfigSave}
                strategyConfig={strategyConfig}
                setStrategyConfig={setStrategyConfig}
                strategyClass={strategyClass}
            />
        </div>
    );
};

export default BacktestView;