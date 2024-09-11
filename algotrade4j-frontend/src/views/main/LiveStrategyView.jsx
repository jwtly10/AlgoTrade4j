import React, {useState} from 'react';
import 'chartjs-adapter-date-fns';
import AnalysisReport from '../../components/backtesting/AnalysisReport.jsx';
import {EquityChart} from '../../components/backtesting/EquityChart.jsx';
import TradesTable from '../../components/backtesting/TradesTable.jsx';
import LogsTable from '../../components/backtesting/LogsTable.jsx';
import TradingViewChart from "../../components/backtesting/TradingViewChart.jsx";
import EmptyChart from "../../components/backtesting/EmptyChart.jsx";

import {Card} from "@/components/ui/card.jsx";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs.jsx";
import {Button} from "@/components/ui/button.jsx";
import {Edit2, Eye, Plus} from "lucide-react";
import {useLive} from "@/hooks/use-live.js";
import LiveConfigModal from "@/components/modals/LiveConfigModal.jsx";
import LiveCreateStratModal from "@/components/modals/LiveCreateStratModal.jsx";


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
        // isModalOpen,
        // setIsModalOpen,
        strategies,
        isAsync,
        progressData,
        showChart,
        startTime,
        strategyConfig,
        setStrategyConfig,
        startStrategy,
        stopStrategy,
        // handleOpenParams,
        // handleConfigSave,
        handleChangeStrategy,
    } = useLive();

    const [pickedLiveStrategy, setPickedLiveStrategy] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

    const mockLiveStrategies = [
        {
            id: 1,
            strategyName: "DJATR - Original",
            config: {
                period: "M15",
                instrumentData: {
                    internalSymbol: "NAS100USD"
                },
                initialCash: 10000,
                strategyClass: "DJATRStrategy",
                runParams: [
                    {
                        "name": "Trade Direction",
                        "description": "Trade direction for the strategy",
                        "value": "ANY",
                        "group": "lol"
                    },
                    {
                        "name": "Start Trading Time (Hour)",
                        "description": "Trade direction for the strategy",
                        "value": "9",
                        "group": "Ungrouped"
                    },
                    {
                        "name": "End Trading Time (Hour)",
                        "description": "Trade direction for the strategy",
                        "value": "20",
                        "group": "Ungrouped"
                    },
                    {
                        "name": "Stop Loss Size (pips)",
                        "description": "Trade direction for the strategy",
                        "value": "25.0",
                        "group": "Risk"
                    },
                    {
                        "name": "Risk Ratio (RR)",
                        "description": "Trade direction for the strategy",
                        "value": "4.0",
                        "group": "Risk"
                    },
                    {
                        "name": "Risk % Per trade",
                        "description": "Trade direction for the strategy",
                        "value": "1.0",
                        "group": "Risk"
                    },
                    {
                        "name": "ATR Length",
                        "description": "Trade direction for the strategy",
                        "value": "14",
                        "group": "Indicator"
                    },
                    {
                        "name": "ATR Sensitivity",
                        "description": "Trade direction for the strategy",
                        "value": "0.6",
                        "group": "Indicator"
                    },
                    {
                        "name": "Relative Size Diff",
                        "description": "Trade direction for the strategy",
                        "value": "2.0",
                        "group": "Indicator"
                    },
                    {
                        "name": "Short EMA Length",
                        "description": "Trade direction for the strategy",
                        "value": "50",
                        "group": "Indicator"
                    },
                    {
                        "name": "Long EMA Length",
                        "description": "Trade direction for the strategy",
                        "value": "0",
                        "group": "Indicator"
                    }
                ]
            },
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
    ];

    const handleViewStrategy = async (strategyId) => {
        // Implement view logic
        console.log("Viewing strategy:", strategyId);
        startStrategy()
    };

    const handleEditStrategy = (pickedLiveStrat) => {
        console.log("Editing strategy:", pickedLiveStrat.strategyName, "Class:", pickedLiveStrat.config.strategyClass);
        setPickedLiveStrategy(pickedLiveStrat);
        setIsModalOpen(true);  // Change this from handleOpenConfig()
    };

    const handleNewStrategy = () => {
        // Implement new strategy creation logic
        console.log("Creating new live strategy");
        // You might want to open a modal or navigate to a new page for strategy creation
        setIsCreateModalOpen(true);
    };

    const handleConfigSave = () => {
        console.log("Saving strategy config");
        // Here we will need to call the backend API to save the strategy config
        // we will need validation on this
        // And there will be some logic that either restarts the app or puts the strategy back into pending state.
        // So we will need to recall the api to get the list of live strategies in the system
        setIsModalOpen(false)
    }

    const handleNewStratSave = (data) => {
        console.log("Creating new strategy", data);
        // Here we will need to call the backend API to save the new strategy
        // we will need validation on this
        // And there will be some logic that either restarts the app or puts the strategy back into pending state.
        // So we will need to recall the api to get the list of live strategies in the system
        setIsCreateModalOpen(false)
    }

    return (
        <div className="flex flex-col h-[calc(100vh-32px-68px)] w-screen overflow-hidden p-4">
            {/* Main content area */}
            <div className="flex-grow flex overflow-hidden">
                {/* Left section (3/4 width) */}
                <div className="flex-grow h-full overflow-hidden">
                    <Card className="h-full flex flex-col p-6">
                        {/* Chart Section */}
                        <div className="flex-shrink-0 h-2/5 min-h-[500px] mb-6 bg-background rounded overflow-hidden">
                            {chartData && chartData.length > 0 ? (
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
                                        <Button variant="outline" size="sm" onClick={() => handleViewStrategy(strategy)}>
                                            <Eye className="w-4 h-4 mr-1"/> View
                                        </Button>
                                        <Button variant="outline" size="sm" onClick={() => handleEditStrategy(strategy)}>
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
            <LiveConfigModal
                open={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSave={handleConfigSave}
                strategyConfig={pickedLiveStrategy}
            />
            <LiveCreateStratModal
                open={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                onSave={handleNewStratSave}
                strategies={strategies}
            />
        </div>
    );
};

export default LiveStrategyView;