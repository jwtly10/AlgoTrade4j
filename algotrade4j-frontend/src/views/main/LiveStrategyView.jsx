import {useEffect, useState} from 'react';
import 'chartjs-adapter-date-fns';
import TradesTable from '../../components/backtesting/TradesTable.jsx';
import LogsTable from '../../components/backtesting/LogsTable.jsx';
import TradingViewChart from '../../components/backtesting/TradingViewChart.jsx';
import EmptyChart from '../../components/backtesting/EmptyChart.jsx';

import {Card, CardContent, CardFooter, CardHeader} from "@/components/ui/card"
import {Tabs, TabsContent, TabsList, TabsTrigger} from '@/components/ui/tabs.jsx';
import {Button} from '@/components/ui/button.jsx';
import {Edit2, Eye, Plus} from 'lucide-react';
import {useLive} from '@/hooks/use-live.js';
import LiveConfigEditModal from '@/components/modals/LiveConfigEditModal.jsx';
import LiveCreateStratModal from '@/components/modals/LiveCreateStratModal.jsx';
import LiveBrokerModal from '@/components/modals/LiveBrokersModal.jsx';
import log from '@/logger.js';
import {Badge} from "@/components/ui/badge"
import {strategyClient} from '@/api/liveClient.js';
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip"

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

    const handleAccountManagement = () => {
        setIsAccountModalOpen(true);
    };

    const handleCreateStratModalClose = async () => {
        setIsCreateModalOpen(false);
        await fetchLiveStrategies();
    };

    const handleConfigEditClose = async () => {
        setIsModalOpen(false);
        await fetchLiveStrategies();
    }

    const handleToggle = async (strategyId) => {
        try {
            const res = await strategyClient.toggleStrategy(strategyId);
            log.debug('Toggled strategy:', res);
            await fetchLiveStrategies();
        } catch (error) {
            log.error('Error toggling strategy:', error);
            toast({
                title: 'Error',
                description: `Error toggling strategy: ${error.message}`,
                variant: 'destructive',
            });
        }
    }

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
                            {[...liveStrategies]
                                .sort((a, b) => b.id - a.id)
                                .map((strategy) => (
                                    <Card key={strategy.id} className="w-full">
                                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                            <div>
                                                <h3 className="font-bold text-lg">{strategy.strategyName}</h3>
                                                <p className="text-sm text-muted-foreground">{strategy.config.strategyClass}</p>
                                            </div>
                                            <div className="flex space-x-2">
                                                <TooltipProvider>
                                                    <Tooltip>
                                                        <TooltipTrigger>
                                                            <Badge
                                                                variant={strategy.active ? "default" : "secondary"}
                                                                className="cursor-pointer"
                                                                onClick={() => handleToggle(strategy.id)}
                                                            >
                                                                {strategy.active ? 'Active' : 'Inactive'}
                                                            </Badge>
                                                        </TooltipTrigger>
                                                        <TooltipContent>
                                                            <p>Click to toggle strategy status</p>
                                                        </TooltipContent>
                                                    </Tooltip>
                                                </TooltipProvider>
                                                <Badge variant={strategy.brokerAccount.brokerType === 'LIVE' ? "destructive" : "default"}>
                                                    {strategy.brokerAccount.brokerType}
                                                </Badge>
                                            </div>
                                        </CardHeader>
                                        <CardContent>
                                            <div className="grid grid-cols-2 gap-4">
                                                <div className="flex flex-col">
                                                    <span className="text-sm font-medium text-muted-foreground">Broker</span>
                                                    <span className="font-semibold">{strategy.brokerAccount.brokerName}</span>
                                                </div>
                                                <div className="flex flex-col">
                                                    <span className="text-sm font-medium text-muted-foreground">Account ID</span>
                                                    <span className="font-semibold">{strategy.brokerAccount.accountId}</span>
                                                </div>
                                            </div>

                                            {strategy.stats ? (
                                                <div className="mt-4 grid grid-cols-2 gap-4">
                                                    <div className="flex items-center space-x-2">
                                                        <Activity className="h-4 w-4 text-muted-foreground"/>
                                                        <div className="flex flex-col">
                                                            <span className="text-sm font-medium text-muted-foreground">Total Trades</span>
                                                            <span className="font-semibold">{strategy.stats.totalTrades}</span>
                                                        </div>
                                                    </div>
                                                    <div className="flex items-center space-x-2">
                                                        <Percent className="h-4 w-4 text-muted-foreground"/>
                                                        <div className="flex flex-col">
                                                            <span className="text-sm font-medium text-muted-foreground">Win Rate</span>
                                                            <span className="font-semibold">{strategy.stats.winRate}%</span>
                                                        </div>
                                                    </div>
                                                    <div className="flex items-center space-x-2">
                                                        <DollarSign className="h-4 w-4 text-muted-foreground"/>
                                                        <div className="flex flex-col">
                                                            <span className="text-sm font-medium text-muted-foreground">Profit Factor</span>
                                                            <span className="font-semibold">{strategy.stats.profitFactor}</span>
                                                        </div>
                                                    </div>
                                                    <div className="flex items-center space-x-2">
                                                        <TrendingUp className="h-4 w-4 text-muted-foreground"/>
                                                        <div className="flex flex-col">
                                                            <span className="text-sm font-medium text-muted-foreground">Sharpe Ratio</span>
                                                            <span className="font-semibold">{strategy.stats.sharpeRatio}</span>
                                                        </div>
                                                    </div>
                                                </div>
                                            ) : (
                                                <p className="mt-4 text-sm text-muted-foreground">No stats available</p>
                                            )}
                                        </CardContent>
                                        <CardFooter className="flex justify-end space-x-2">
                                            <Button variant="outline" size="sm" onClick={() => handleViewStrategy(strategy)}>
                                                <Eye className="w-4 h-4 mr-2"/> View
                                            </Button>
                                            <Button variant="outline" size="sm" onClick={() => handleEditStrategy(strategy)}>
                                                <Edit2 className="w-4 h-4 mr-2"/> Edit
                                            </Button>
                                        </CardFooter>
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
            {pickedLiveStrategy && (
                <LiveConfigEditModal
                    open={isModalOpen}
                    onClose={handleConfigEditClose}
                    strategyConfig={pickedLiveStrategy}
                />
            )}
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