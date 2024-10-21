import {useEffect, useRef, useState} from 'react';
import 'chartjs-adapter-date-fns';
import TradesTable from '../../components/backtesting/TradesTable.jsx';
import LogsTable from '../../components/backtesting/LogsTable.jsx';
import TradingViewChart from '../../components/backtesting/TradingViewChart.jsx';
import EmptyChart from '../../components/backtesting/EmptyChart.jsx';

import {Card, CardContent, CardFooter, CardHeader} from '@/components/ui/card';
import {Tabs, TabsContent, TabsList, TabsTrigger} from '@/components/ui/tabs.jsx';
import {Button} from '@/components/ui/button.jsx';
import {Bell, Edit2, Eye, Loader2, Plus, RefreshCw} from 'lucide-react';
import {useLive} from '@/hooks/use-live.js';
import LiveConfigEditModal from '@/components/modals/LiveConfigEditModal.jsx';
import LiveCreateStratModal from '@/components/modals/LiveCreateStratModal.jsx';
import LiveBrokerModal from '@/components/modals/LiveBrokersModal.jsx';
import {Popover, PopoverContent, PopoverTrigger} from '@/components/ui/popover';
import log from '@/logger.js';
import {Badge} from '@/components/ui/badge';
import {liveStrategyClient} from '@/api/liveClient.js';
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from '@/components/ui/tooltip';
import {useToast} from '@/hooks/use-toast.js';
import {useIsMobile} from '@/hooks/useIsMobile.js';

const LiveStrategyView = ({user}) => {
    const {
        resetChart,
        isConnected,
        trades,
        analysisData,
        indicators,
        chartData,
        logs,
        tabValue,
        setTabValue,
        strategies,
        viewStrategy,
    } = useLive();

    const {toast} = useToast();

    const [pickedLiveStrategy, setPickedLiveStrategy] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isAccountModalOpen, setIsAccountModalOpen] = useState(false);
    const [liveStrategies, setLiveStrategies] = useState([]);
    const [viewingStrategy, setViewingStrategy] = useState(null);

    const [togglingStrategyId, setTogglingStrategyId] = useState(null);

    // UI State to prevent multiple clicks
    const [uiIsToggling, setUiIsToggling] = useState(false);

    const intervalRef = useRef(null);

    const {isMobile} = useIsMobile();

    useEffect(() => {
        // Initial fetch
        fetchLiveStrategies();

        // Set up polling every 5 seconds
        intervalRef.current = setInterval(() => {
            fetchLiveStrategies();
        }, 5000);

        // Clean up function to clear the interval when the component unmounts
        return () => {
            if (intervalRef.current) {
                clearInterval(intervalRef.current);
            }
        };
    }, []);

    const fetchLiveStrategies = async () => {
        try {
            const res = await liveStrategyClient.getLiveStrategies();
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

    const handleViewStrategy = async (strategy) => {
        log.debug('Viewing strategy:', strategy.id);
        setViewingStrategy(strategy);
        await viewStrategy(strategy.strategyName);
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
    };

    const handleToggle = async (strategyId) => {
        setTogglingStrategyId(strategyId);
        setUiIsToggling(true);

        if (viewingStrategy && viewingStrategy.id === strategyId) {
            resetChart();
            setViewingStrategy(null);
        }

        try {
            const res = await liveStrategyClient.toggleStrategy(strategyId);
            log.debug('Toggled strategy:', res);
        } catch (error) {
            log.error('Error toggling strategy:', error);
            toast({
                title: 'Error',
                description: `${error.message}`,
                variant: 'destructive',
            });
        }
        await fetchLiveStrategies();
        setTogglingStrategyId(null);
        setUiIsToggling(false);
    };

    const StatCard = ({title, value, valueColor = 'text-foreground'}) => {
        const isMobile = useIsMobile();

        return (
            <div
                className={`bg-background p-2 ${isMobile ? 'text-sm' : 'text-base'} rounded-md shadow-sm`}
            >
                <h4 className="text-xs md:text-sm font-medium text-muted-foreground mb-1">
                    {title}
                </h4>
                <p className={`font-semibold ${valueColor}`}>{value}</p>
            </div>
        );
    };

    return (
        <div className="flex flex-col w-screen p-4 md:h-[calc(100vh-32px-68px)] md:overflow-hidden">
            {/* Main content area */}
            <div className="flex-grow flex flex-col md:flex-row overflow-hidden">
                {/* Left section (3/4 width) */}
                <div className="w-full md:flex-grow h-full overflow-hidden">
                    <Card className="h-full flex flex-col p-6">
                        {isConnected ? (
                            <>
                                {/* Chart Section */}
                                <div className="flex-shrink-0 h-2/5 min-h-[500px] mb-6 bg-background rounded overflow-hidden">
                                    {chartData && viewingStrategy && chartData.length > 0 ? (
                                        <TradingViewChart
                                            showChart={true}
                                            strategyConfig={viewingStrategy?.config}
                                            chartData={chartData}
                                            trades={trades}
                                            indicators={indicators}
                                        />
                                    ) : (
                                        <EmptyChart/>
                                    )}
                                </div>

                                {/* Stats Widget Section */}
                                {viewingStrategy && analysisData && (
                                    <div className="mb-4 bg-card rounded-md shadow-sm p-2 md:p-4">
                                        <h3 className="text-base md:text-lg font-semibold mb-2 md:mb-3">
                                            Strategy Live Performance
                                        </h3>
                                        {analysisData.stats ? (
                                            <div className="grid grid-cols-2 md:grid-cols-4 gap-2 md:gap-4">
                                                <StatCard
                                                    title="Balance"
                                                    value={`${analysisData.stats.balance.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})} USD`}
                                                    valueColor={
                                                        analysisData.balance >= 0
                                                            ? 'text-green-500'
                                                            : 'text-red-500'
                                                    }
                                                />
                                                <StatCard
                                                    title="Equity"
                                                    value={`${analysisData.stats.equity.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}`}
                                                    valueColor={
                                                        analysisData.equity >= 0
                                                            ? 'text-green-500'
                                                            : 'text-red-500'
                                                    }
                                                />
                                                <StatCard
                                                    title="Running PnL"
                                                    value={`${analysisData.stats.openTradeProfit.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}`}
                                                    valueColor={
                                                        analysisData.openTradeProfit >= 0
                                                            ? 'text-green-500'
                                                            : 'text-red-500'
                                                    }
                                                />
                                                <StatCard
                                                    title="Win Rate"
                                                    value={`${analysisData.stats.winRate.toFixed(2)}%`}
                                                />
                                                <StatCard
                                                    title="Profit Factor"
                                                    value={analysisData.stats.profitFactor.toFixed(
                                                        2
                                                    )}
                                                />
                                                <StatCard
                                                    title="Sharpe Ratio"
                                                    value={analysisData.stats.sharpeRatio.toFixed(
                                                        2
                                                    )}
                                                />
                                                <StatCard
                                                    title="Total Trades"
                                                    value={analysisData.stats.totalTrades}
                                                />
                                            </div>
                                        ) : (
                                            <div className="flex justify-center items-center h-20 md:h-32">
                                                <p className="text-sm md:text-base text-gray-500">
                                                    Loading stats...
                                                </p>
                                            </div>
                                        )}
                                    </div>
                                )}

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
                                            <TradesTable
                                                user={user}
                                                trades={trades}
                                                strategy={viewingStrategy}
                                                useLiveSplit={true}
                                            />
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
                                    Select a strategy from the strategy panel to view live data.
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
                            <Button
                                variant="outline"
                                disabled={user.role !== 'ADMIN'}
                                size="sm"
                                onClick={handleNewStrategy}
                            >
                                <Plus className="w-4 h-4 mr-1"/> New Live Strategy
                            </Button>
                        </div>
                        <div className="space-y-4">
                            {[...liveStrategies]
                                .sort((a, b) => b.id - a.id)
                                .map((strategy) => (
                                    <Card
                                        key={strategy.id}
                                        className={`w-full ${viewingStrategy && viewingStrategy.id === strategy.id ? 'border-2 border-primary' : ''}`}
                                    >
                                        <CardHeader className="flex flex-row items-center justify-between pb-2">
                                            <div>
                                                <div className="flex items-center">
                                                    <h3 className="font-bold text-lg">
                                                        {strategy.strategyName}
                                                    </h3>
                                                    {viewingStrategy &&
                                                        viewingStrategy.id === strategy.id && (
                                                            <Badge
                                                                variant="secondary"
                                                                className="ml-2"
                                                            >
                                                                Viewing
                                                            </Badge>
                                                        )}
                                                </div>
                                                <p className="text-xs text-muted-foreground">
                                                    {strategy.config.strategyClass}
                                                </p>
                                            </div>
                                            <div className="flex space-x-2 items-center">
                                                {strategy.lastErrorMsg && (
                                                    <Popover>
                                                        <PopoverTrigger asChild>
                                                            <Button
                                                                variant="ghost"
                                                                size="icon"
                                                                className="h-8 w-8 p-0"
                                                            >
                                                                <Bell className="h-4 w-4 text-destructive"/>
                                                            </Button>
                                                        </PopoverTrigger>
                                                        <PopoverContent className="w-80">
                                                            <h4 className="font-medium leading-none mb-2">
                                                                Live Alert
                                                            </h4>
                                                            <p className="text-sm text-muted-foreground">
                                                                {strategy.lastErrorMsg}
                                                            </p>
                                                        </PopoverContent>
                                                    </Popover>
                                                )}
                                                <TooltipProvider>
                                                    <Tooltip>
                                                        <TooltipTrigger>
                                                            <Badge
                                                                variant={
                                                                    strategy.active
                                                                        ? 'default'
                                                                        : 'secondary'
                                                                }
                                                                className={`flex items-center gap-2 ${
                                                                    strategy.active
                                                                        ? 'bg-green-500 hover:bg-green-600'
                                                                        : ''
                                                                } ${
                                                                    user.role !== 'ADMIN' ||
                                                                    uiIsToggling
                                                                        ? 'cursor-not-allowed'
                                                                        : 'cursor-pointer'
                                                                }`}
                                                                onClick={
                                                                    user.role === 'ADMIN' &&
                                                                    !uiIsToggling
                                                                        ? () =>
                                                                            handleToggle(
                                                                                strategy.id
                                                                            )
                                                                        : null
                                                                }
                                                            >
                                                                {togglingStrategyId ===
                                                                strategy.id ? (
                                                                    <Loader2 className="h-3 w-3 animate-spin"/>
                                                                ) : strategy.active ? (
                                                                    'Active'
                                                                ) : (
                                                                    'Inactive'
                                                                )}
                                                            </Badge>
                                                        </TooltipTrigger>
                                                        <TooltipContent>
                                                            <p>Click to toggle strategy status</p>
                                                        </TooltipContent>
                                                    </Tooltip>
                                                </TooltipProvider>
                                                <Badge
                                                    variant={
                                                        strategy.brokerAccount.brokerEnv === 'LIVE'
                                                            ? 'destructive'
                                                            : 'default'
                                                    }
                                                >
                                                    {strategy.brokerAccount.brokerEnv}
                                                </Badge>
                                            </div>
                                        </CardHeader>
                                        <CardContent>
                                            <div className="space-y-4">
                                                <div className="bg-muted p-3 rounded-md">
                                                    <div className="gap-x-4 gap-y-2 text-sm">
                                                        <div className="flex items-center gap-2">
                                                            <span className="text-muted-foreground">
                                                                Account:
                                                            </span>
                                                            <span className="font-medium">
                                                                ({strategy.brokerAccount.brokerType}
                                                                ){' '}
                                                                {strategy.brokerAccount.brokerName}
                                                            </span>
                                                        </div>
                                                    </div>
                                                </div>

                                                {strategy.stats && (
                                                    <div>
                                                        <h4 className="text-sm font-semibold mb-2">
                                                            Account Statistics
                                                        </h4>
                                                        <div className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
                                                            <div className="flex justify-between">
                                                                <span className="text-muted-foreground">
                                                                    Balance:
                                                                </span>
                                                                <span className="font-medium">
                                                                    $
                                                                    {parseFloat(
                                                                        strategy.stats
                                                                            .accountBalance
                                                                    )
                                                                        ? parseFloat(
                                                                            strategy.stats.accountBalance.toFixed(
                                                                                2
                                                                            )
                                                                        ).toLocaleString()
                                                                        : strategy.stats
                                                                            .accountBalance}
                                                                </span>
                                                            </div>
                                                            <div className="flex justify-between">
                                                                <span className="text-muted-foreground">
                                                                    Running PNL:
                                                                </span>
                                                                <span className="font-medium">
                                                                    $
                                                                    {parseFloat(
                                                                        strategy.stats
                                                                            .openTradeProfit
                                                                    )
                                                                        ? parseFloat(
                                                                            strategy.stats.openTradeProfit.toFixed(
                                                                                2
                                                                            )
                                                                        ).toLocaleString()
                                                                        : strategy.stats
                                                                            .openTradeProfit}
                                                                </span>
                                                            </div>
                                                            <div className="flex justify-between">
                                                                <span className="text-muted-foreground">
                                                                    PNL:
                                                                </span>
                                                                <span className="font-medium">
                                                                    $
                                                                    {parseFloat(
                                                                        strategy.stats.profit
                                                                    )
                                                                        ? parseFloat(
                                                                            strategy.stats.profit.toFixed(
                                                                                2
                                                                            )
                                                                        ).toLocaleString()
                                                                        : strategy.stats.profit}
                                                                </span>
                                                            </div>
                                                            <div className="flex justify-between">
                                                                <span className="text-muted-foreground">
                                                                    Total Trades:
                                                                </span>
                                                                <span className="font-medium">
                                                                    {strategy.stats.totalTrades}
                                                                </span>
                                                            </div>
                                                            <div className="flex justify-between">
                                                                <span className="text-muted-foreground">
                                                                    Win Rate:
                                                                </span>
                                                                <span className="font-medium">
                                                                    {strategy.stats.winRate.toFixed(
                                                                        2
                                                                    )}
                                                                    %
                                                                </span>
                                                            </div>
                                                            <div className="flex justify-between">
                                                                <span className="text-muted-foreground">
                                                                    Profit Factor:
                                                                </span>
                                                                <span className="font-medium">
                                                                    {strategy.stats.profitFactor.toFixed(
                                                                        2
                                                                    )}
                                                                </span>
                                                            </div>
                                                            <div className="flex justify-between">
                                                                <span className="text-muted-foreground">
                                                                    Sharpe Ratio:
                                                                </span>
                                                                <span className="font-medium">
                                                                    {strategy.stats.sharpeRatio.toFixed(
                                                                        2
                                                                    )}
                                                                </span>
                                                            </div>
                                                        </div>
                                                    </div>
                                                )}
                                            </div>
                                        </CardContent>
                                        <CardFooter className="flex justify-end space-x-2 pt-2">
                                            {strategy.active && !uiIsToggling && (
                                                <Button
                                                    variant={
                                                        viewingStrategy &&
                                                        viewingStrategy.id === strategy.id
                                                            ? 'default'
                                                            : 'outline'
                                                    }
                                                    size="sm"
                                                    onClick={() => handleViewStrategy(strategy)}
                                                >
                                                    {viewingStrategy &&
                                                    viewingStrategy.id === strategy.id ? (
                                                        <>
                                                            <RefreshCw className="w-4 h-4 mr-2 animate-spin"/>{' '}
                                                            Refresh
                                                        </>
                                                    ) : (
                                                        <>
                                                            <Eye className="w-4 h-4 mr-2"/> View
                                                        </>
                                                    )}
                                                </Button>
                                            )}
                                            <Button
                                                variant="outline"
                                                size="sm"
                                                onClick={() => handleEditStrategy(strategy)}
                                                disabled={user.role !== 'ADMIN'}
                                            >
                                                <Edit2 className="w-4 h-4 mr-2"/> Edit
                                            </Button>
                                        </CardFooter>
                                    </Card>
                                ))}
                        </div>
                        <div className="flex-grow"/>

                        <p className="text-sm text-muted-foreground text-center">
                            Only admins can create and manage live strategies.
                        </p>
                        <Button
                            variant="outline"
                            size="md"
                            className="p-1"
                            onClick={handleAccountManagement}
                            disabled={user.role !== 'ADMIN'}
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
                    clearChart={resetChart}
                    isStrategyInUse={
                        viewingStrategy && viewingStrategy.id === pickedLiveStrategy.id
                    }
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