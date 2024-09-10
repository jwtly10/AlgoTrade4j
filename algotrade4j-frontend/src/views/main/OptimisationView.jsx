import React from 'react';
import 'chartjs-adapter-date-fns';
import {Play, Settings} from "lucide-react";
import ConfigModal from '../components/modals/ConfigModal.jsx';
import OptimisationTaskList from "../components/optimisation/OptimisationTaskList.jsx";
import {Button} from "../components/ui/button";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "../components/ui/select";
import {Card, CardContent, CardHeader, CardTitle} from "../components/ui/card";
import {useBacktest} from "@/hooks/use-backtest.js";

const OptimisationView = () => {
    const {
        isModalOpen,
        setIsModalOpen,
        strategies,
        strategyClass,
        strategyConfig,
        setStrategyConfig,
        startOptimisation,
        handleOpenParams,
        handleConfigSave,
        handleChangeStrategy,
    } = useBacktest();

    return (
        <div className="flex flex-col h-[calc(100vh-32px-68px)] w-screen overflow-hidden p-4">
            <div className="flex-grow flex overflow-hidden">
                <div className="flex-grow h-full overflow-hidden flex flex-col">
                    <Card className="flex-grow flex flex-col p-6 overflow-hidden">
                        <CardHeader>
                            <CardTitle>Optimisation Tasks</CardTitle>
                        </CardHeader>
                        <CardContent className="flex-grow overflow-auto">
                            <OptimisationTaskList/>
                        </CardContent>
                    </Card>
                </div>


                <div className="w-full md:w-1/3 lg:w-1/4 min-w-[280px] p-4 bg-background shadow-md overflow-auto">
                    <div className="flex flex-col h-full space-y-6">
                        <div className="space-y-4">
                            <h3 className="text-lg font-semibold">Optimisation Control</h3>
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
                                    variant="default"
                                    onClick={startOptimisation}
                                    disabled={strategyClass === ""}
                                    size="lg"
                                    className="w-full sm:w-1/2"
                                >
                                    <Play className="mr-2 h-4 w-4"/> Start Optimisation
                                </Button>

                                <Button
                                    variant="outline"
                                    onClick={handleOpenParams}
                                    disabled={strategyClass === ""}
                                    size="lg"
                                    className="w-full sm:w-1/2"
                                >
                                    <Settings className="mr-2 h-4 w-4"/> Configure Parameters
                                </Button>
                            </div>
                        </div>

                        <div className="bg-card text-card-foreground rounded-lg p-4">
                            <h4 className="text-sm font-semibold mb-2">Note</h4>
                            <p className="text-sm text-muted-foreground">
                                Parameters and run configuration are shared between Backtesting and Optimisation screens.
                            </p>
                        </div>

                        <div className="flex-grow"/>

                        <p className="text-sm text-muted-foreground text-center">
                            Select a strategy and configure parameters before starting an optimisation run.
                        </p>
                    </div>
                </div>


            </div>

            <ConfigModal
                open={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSave={handleConfigSave}
                strategyConfig={strategyConfig}
                setStrategyConfig={setStrategyConfig}
                strategyClass={strategyClass}
                showOptimiseParams={true}
            />
        </div>
    );
};

export default OptimisationView;