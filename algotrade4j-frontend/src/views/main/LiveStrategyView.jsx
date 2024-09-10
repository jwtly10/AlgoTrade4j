import React, {useState} from 'react';
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
import {Button} from "@/components/ui/button.jsx";
import {Edit2, Eye, Plus} from "lucide-react";
import {useLive} from "@/hooks/use-live.js";


const LiveStrategyView = () => {
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
    } = useLive();

    const [strategyClass, setStrategyClass] = useState(null);

    const mockLiveStrategies = [
        {
            id: 1,
            strategyName: "DJATR - Original",
            config: {strategyClass: "DJATRStrategy"},
            brokerConfig: {
                broker: "OANDA",
                account_id: "001-001-1234567-001",
                type: "DEMO"
            },
            stats: {
                totalTrades: 50,
                winRate: 60,
                profitFactor: 1.5,
                sharpeRatio: 1.2
            },
            isActive: true
        },
        {
            id: 2,
            strategyName: "DJATR - Archit Variation",
            config: {strategyClass: "RSIStrategy"},
            brokerConfig: {
                broker: "MT4",
                account_id: "MT4-12345",
                type: "LIVE"
            },
            stats: {
                totalTrades: 30,
                winRate: 55,
                profitFactor: 1.3,
                sharpeRatio: 1.1
            },
            isActive: false
        },
        {
            id: 3,
            strategyName: "MACD Divergence",
            config: {strategyClass: "MACDDivergence"},
            brokerConfig: {
                broker: "VANTAGE",
                account_id: "VAN-98765",
                type: "LIVE"
            },
            stats: {
                totalTrades: 75,
                winRate: 62,
                profitFactor: 1.8,
                sharpeRatio: 1.4
            },
            isActive: true
        },
        {
            id: 4,
            strategyName: "Bollinger Bands Squeeze",
            config: {strategyClass: "BollingerSqueeze"},
            brokerConfig: {
                broker: "OANDA",
                account_id: "001-001-7654321-002",
                type: "DEMO"
            },
            stats: {
                totalTrades: 40,
                winRate: 58,
                profitFactor: 1.4,
                sharpeRatio: 1.0
            },
            isActive: true
        },
        {
            id: 5,
            strategyName: "Fibonacci Retracement",
            config: {strategyClass: "FibonacciRetracement"},
            brokerConfig: {
                broker: "MT4",
                account_id: "MT4-67890",
                type: "LIVE"
            },
            stats: {
                totalTrades: 60,
                winRate: 65,
                profitFactor: 2.0,
                sharpeRatio: 1.6
            },
            isActive: false
        }
    ];

    const handleViewStrategy = (strategyId) => {
        // Implement view logic
        console.log("Viewing strategy:", strategyId);
    };

    const handleEditStrategy = (strategyId, strategyClass) => {
        // Implement edit logic
        console.log("Editing strategy:", strategyId, "Class:", strategyClass);
        setStrategyClass(strategyClass);
        handleOpenParams();
    };

    const handleNewStrategy = () => {
        // Implement new strategy creation logic
        console.log("Creating new live strategy");
        // You might want to open a modal or navigate to a new page for strategy creation
    };

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
                <div className="w-full md:w-1/3 lg:w-1/4 min-w-[280px] p-4 bg-background shadow overflow-auto">
                    <div className="flex flex-col h-full space-y-6">
                        <div className="flex justify-between items-center">
                            <h2 className="text-2xl font-bold">Live Strategies</h2>
                            <Button variant="outline" size="sm" onClick={handleNewStrategy}>
                                <Plus className="w-4 h-4 mr-1"/> New Live Strategy
                            </Button>
                        </div>
                        <div className="space-y-4">
                            {mockLiveStrategies.map((strategy) => (
                                <Card key={strategy.id} className="p-4">
                                    <div className="flex justify-between items-center mb-2">
                                        <h3 className="text-lg font-semibold">{strategy.strategyName}</h3>
                                        <div className="flex items-center space-x-2">
            <span className={`px-2 py-1 rounded text-sm ${strategy.isActive ? 'bg-green-500 text-white' : 'bg-gray-300 text-gray-700'}`}>
                {strategy.isActive ? 'Active' : 'Inactive'}
            </span>
                                            <span className={`px-2 py-1 rounded text-sm ${strategy.brokerConfig.type === 'LIVE' ? 'bg-blue-500 text-white' : 'bg-yellow-500 text-white'}`}>
                {strategy.brokerConfig.type}
            </span>
                                        </div>
                                    </div>
                                    <div className="grid grid-cols-2 gap-2 mb-3">
                                        <div>
                                            <p className="text-sm text-muted-foreground">Broker</p>
                                            <p className="font-semibold">{strategy.brokerConfig.broker}</p>
                                        </div>
                                        <div>
                                            <p className="text-sm text-muted-foreground">Account ID</p>
                                            <p className="font-semibold">{strategy.brokerConfig.account_id}</p>
                                        </div>
                                        <div>
                                            <p className="text-sm text-muted-foreground">Total Trades</p>
                                            <p className="font-semibold">{strategy.stats.totalTrades}</p>
                                        </div>
                                        <div>
                                            <p className="text-sm text-muted-foreground">Win Rate</p>
                                            <p className="font-semibold">{strategy.stats.winRate}%</p>
                                        </div>
                                        <div>
                                            <p className="text-sm text-muted-foreground">Profit Factor</p>
                                            <p className="font-semibold">{strategy.stats.profitFactor}</p>
                                        </div>
                                        <div>
                                            <p className="text-sm text-muted-foreground">Sharpe Ratio</p>
                                            <p className="font-semibold">{strategy.stats.sharpeRatio}</p>
                                        </div>
                                    </div>
                                    <div className="flex justify-end space-x-2">
                                        <Button variant="outline" size="sm" onClick={() => handleViewStrategy(strategy.id)}>
                                            <Eye className="w-4 h-4 mr-1"/> View
                                        </Button>
                                        <Button variant="outline" size="sm" onClick={() => handleEditStrategy(strategy.id, strategy.config.strategyClass)}>
                                            <Edit2 className="w-4 h-4 mr-1"/> Edit
                                        </Button>
                                    </div>
                                </Card>
                            ))}
                        </div>
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

export default LiveStrategyView;