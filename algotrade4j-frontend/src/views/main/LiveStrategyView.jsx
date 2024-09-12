import React, {useEffect, useState} from 'react';
import 'chartjs-adapter-date-fns';
import TradesTable from '../../components/backtesting/TradesTable.jsx';
import LogsTable from '../../components/backtesting/LogsTable.jsx';
import TradingViewChart from '../../components/backtesting/TradingViewChart.jsx';
import EmptyChart from '../../components/backtesting/EmptyChart.jsx';

import {Card} from '@/components/ui/card.jsx';
import {Tabs, TabsContent, TabsList, TabsTrigger} from '@/components/ui/tabs.jsx';
import {Button} from '@/components/ui/button.jsx';
import {Edit2, Eye, Plus} from 'lucide-react';
import {useLive} from '@/hooks/use-live.js';
import LiveConfigModal from '@/components/modals/LiveConfigModal.jsx';
import LiveCreateStratModal from '@/components/modals/LiveCreateStratModal.jsx';
import LiveBrokerModal from '@/components/modals/LiveBrokersModal.jsx';
import log from '@/logger.js';
import {strategyClient} from '@/api/liveClient.js';

const LiveStrategyView = () => {
    const {
        isConnected,
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
    const [isAccountModalOpen, setIsAccountModalOpen] = useState(false);
    const [liveStrategies, setLiveStrategies] = useState([]);

    useEffect(() => {
        fetchLiveStrategies();
    }, []);

    const fetchLiveStrategies = async () => {
        try {
            const res = await strategyClient.getLiveStrategies();
            setLiveStrategies(res);
        } catch (error) {
            log.error('Error getting live strategies from db', error);
            toast({
                title: 'Error',
                description: `Error getting live strategies from db: ${error.message}`,
                variant: 'destructive',
            });
        }
    };

    const handleViewStrategy = async (strategyId) => {
        log.debug('Viewing strategy:', strategyId);
        startStrategy();
    };

    const handleEditStrategy = (pickedLiveStrat) => {
        setPickedLiveStrategy(pickedLiveStrat);
        setIsModalOpen(true);
    };

    const handleNewStrategy = () => {
        setIsCreateModalOpen(true);
    };

    const handleConfigSave = () => {
        console.log('Saving strategy config');
        // Here we will need to call the backend API to save the strategy config
        // we will need validation on this
        // And there will be some logic that either restarts the app or puts the strategy back into pending state.
        // So we will need to recall the api to get the list of live strategies in the system
        setIsModalOpen(false);
    };

    const handleAccountManagement = () => {
        setIsAccountModalOpen(true);
    };

    const handleCreateStratModalClose = () => {
        setIsCreateModalOpen(false);
        fetchLiveStrategies();
    };

    return (
        <div className="flex flex-col h-[calc(100vh-32px-68px)] w-screen overflow-hidden p-4">
            {/* Main content area */}
            <div className="flex-grow flex overflow-hidden">
                {/* Left section (3/4 width) */}
                <div className="flex-grow h-full overflow-hidden">
                    <Card className="h-full flex flex-col p-6">
                        {isConnected ? (
                            <>
                                {/* Chart Section */}
                                <div className="flex-shrink-0 h-2/5 min-h-[500px] mb-6 bg-background rounded overflow-hidden">
                                    {chartData && chartData.length > 0 ? (
                                        <TradingViewChart
                                            showChart={showChart}
                                            strategyConfig={strategyConfig}
                                            chartData={chartData}
                                            trades={trades}
                                            indicators={indicators}
                                        />
                                    ) : (
                                        <EmptyChart trades={trades} showChart={showChart}/>
                                    )}
                                </div>

                                {/* Tabs and Content Section */}
                                <Tabs
                                    value={tabValue}
                                    onValueChange={setTabValue}
                                    className="flex-grow flex flex-col overflow-hidden"
                                >
                                    <TabsList>
                                        <TabsTrigger value="trades">Trades</TabsTrigger>
                                        <TabsTrigger value="logs">Logs</TabsTrigger>
                                    </TabsList>
                                    <div className="flex-grow overflow-hidden">
                                        <TabsContent
                                            value="trades"
                                            className="h-full overflow-auto"
                                        >
                                            <TradesTable trades={trades} split={true}/>
                                        </TabsContent>
                                        <TabsContent value="logs" className="h-full overflow-auto">
                                            {logs.length > 0 ? (
                                                <LogsTable logs={logs}/>
                                            ) : (
                                                <p className="p-4 text-center">
                                                    No logs available yet.
                                                </p>
                                            )}
                                        </TabsContent>
                                    </div>
                                </Tabs>
                            </>
                        ) : (
                            <div className="h-full flex flex-col items-center justify-center">
                                <h2 className="text-2xl font-bold mb-4">
                                    No Live Strategy Connected
                                </h2>
                                <p className="text-center mb-6">
                                    Select a strategy from the right panel to view live data.
                                </p>
                            </div>
                        )}
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
                            {liveStrategies.map((strategy) => (
                                <Card key={strategy.id} className="p-4">
                                    <div className="flex justify-between items-center mb-2">
                                        <h3 className="text-lg font-semibold">
                                            {strategy.strategyName}
                                        </h3>
                                        <div className="flex items-center space-x-2">
                                            <span
                                                className={`px-2 py-1 rounded text-sm ${strategy.isActive ? 'bg-green-500 text-white' : 'bg-gray-300 text-gray-700'}`}
                                            >
                                                {strategy.isActive ? 'Active' : 'Inactive'}
                                            </span>
                                            <span
                                                className={`px-2 py-1 rounded text-sm ${strategy.brokerAccount.brokerType === 'LIVE' ? 'bg-blue-500 text-white' : 'bg-yellow-500 text-white'}`}
                                            >
                                                {strategy.brokerAccount.brokerType}
                                            </span>
                                        </div>
                                    </div>
                                    <div className="grid grid-cols-2 gap-2 mb-3">
                                        <div>
                                            <p className="text-sm text-muted-foreground">Broker</p>
                                            <p className="font-semibold">
                                                {strategy.brokerAccount.brokerName}
                                            </p>
                                        </div>
                                        <div>
                                            <p className="text-sm text-muted-foreground">
                                                Account ID
                                            </p>
                                            <p className="font-semibold">
                                                {strategy.brokerAccount.accountId}
                                            </p>
                                        </div>
                                        {strategy.stats ? (
                                            <>
                                                <div>
                                                    <p className="text-sm text-muted-foreground">
                                                        Total Trades
                                                    </p>
                                                    <p className="font-semibold">
                                                        {strategy.stats.totalTrades}
                                                    </p>
                                                </div>
                                                <div>
                                                    <p className="text-sm text-muted-foreground">
                                                        Win Rate
                                                    </p>
                                                    <p className="font-semibold">
                                                        {strategy.stats.winRate}%
                                                    </p>
                                                </div>
                                                <div>
                                                    <p className="text-sm text-muted-foreground">
                                                        Profit Factor
                                                    </p>
                                                    <p className="font-semibold">
                                                        {strategy.stats.profitFactor}
                                                    </p>
                                                </div>
                                                <div>
                                                    <p className="text-sm text-muted-foreground">
                                                        Sharpe Ratio
                                                    </p>
                                                    <p className="font-semibold">
                                                        {strategy.stats.sharpeRatio}
                                                    </p>
                                                </div>
                                            </>
                                        ) : (
                                            <div className="col-span-2">
                                                <p className="text-sm text-muted-foreground text-start">
                                                    No stats available
                                                </p>
                                            </div>
                                        )}
                                    </div>
                                    <div className="flex justify-end space-x-2">
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => handleViewStrategy(strategy)}
                                        >
                                            <Eye className="w-4 h-4 mr-1"/> View
                                        </Button>
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => handleEditStrategy(strategy)}
                                        >
                                            <Edit2 className="w-4 h-4 mr-1"/> Edit
                                        </Button>
                                    </div>
                                </Card>
                            ))}
                        </div>
                        <div className="flex-grow"/>

                        <p className="text-sm text-muted-foreground text-center">
                            Start/Pause/View Strategies above in real time
                        </p>
                        <Button
                            variant="outline"
                            size="md"
                            className="p-1"
                            onClick={handleAccountManagement}
                        >
                            Account Management
                        </Button>
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
                onClose={handleCreateStratModalClose}
                strategies={strategies}
            />
            <LiveBrokerModal
                open={isAccountModalOpen}
                onClose={() => setIsAccountModalOpen(false)}
            />
        </div>
    );
};

export default LiveStrategyView;