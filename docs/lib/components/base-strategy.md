# BaseStrategy

## Overview

The `BaseStrategy` class is a fundamental component of the AlgoTrade4j framework. It serves as an abstract base class for all trading strategies, providing a rich set of methods and utilities that abstract away common trading functionalities. By extending this class, strategy developers can focus on implementing their specific trading logic while leveraging the robust infrastructure provided by AlgoTrade4j.

## Key Features

- Abstracts away complex trading operations
- Provides easy access to market data and account information
- Offers utility methods for common trading calculations
- Manages strategy parameters and indicators
- Handles lifecycle events of a trading strategy

## Methods for Strategy Implementation

### Trade Management

#### `openLong(TradeParameters params)`

- **Description**: Opens a long position with the specified trade parameters.
- **Returns**: Integer (trade ID)
- **Note**: Returns -1 if the trade is not allowed by the risk manager.

#### `openShort(TradeParameters params)`

- **Description**: Opens a short position with the specified trade parameters.
- **Returns**: Integer (trade ID)
- **Note**: Returns -1 if the trade is not allowed by the risk manager.

#### `createTradeParameters(Instrument instrument, int stopLossTicks, boolean isLong, double riskRatio, double riskPercentage, double balanceToRisk)`

- **Description**: Automatically creates a new TradeParameters object based on the strategy's risk management settings.
- **Returns**: TradeParameters
- **Note**: This is the recommended way to create trade parameters for a trade. All trades in a strategy should open trades based on a given risk ratio, given a defined tick size, to ensure consistency.

### Account Information

#### `getInitialBalance()`

- **Description**: Returns the initial balance of the account.
- **Returns**: double

#### `getBalance()`

- **Description**: Returns the current balance of the account.
- **Returns**: double

#### `getEquity()`

- **Description**: Returns the current equity of the account.
- **Returns**: double

### Market Data Access

#### `getLastBar()`

- **Description**: Returns the last bar in the series.
- **Returns**: Bar

#### `getBar(int index)`

- **Description**: Returns the bar at the specified index.
- **Returns**: Bar

#### `Ask()`

- **Description**: Returns the current ask price.
- **Returns**: Number

#### `Bid()`

- **Description**: Returns the current bid price.
- **Returns**: Number

### Utility Methods

#### `useSystemNotifications()`

- **Description**: Turns on the use of system notifications for a strategy. System notifications are configured at the instance level and used for 24/7 monitoring of the strategy.
-

#### `useCustomNotifications()`

- **Description**: Turns on the use of custom developer created notifications for a strategy.

#### `getStopLossGivenInstrumentPriceDir(Instrument instrument, Number price, int ticks, boolean isLong)`

- **Description**: Calculates the stop loss price based on the given parameters.
- **Returns**: Number

#### `sendCustomNotification(String message)`

- **Description**: Sends a notification message to the external notifier implementation. Uses the live strategies chatId configuration item.

### Indicator Management

#### `createIndicator(Class<T> indicatorClass, Object... params)`

- **Description**: Factory method for creating indicators.
- **Returns**: T (the created indicator)

### Parameter Management

#### `setParameters(Map<String, String> parameters)`

- **Description**: Sets the parameters of the strategy.

#### `getCurrentParameters()`

- **Description**: Returns the current parameters of the strategy.
- **Returns**: Map<String, String>

## Lifecycle Methods

The BaseStrategy class provides several lifecycle methods that can be overridden by strategy implementations:

#### `onStart()`

- **Description**: Custom initialization method called after indicators are initialized but before the strategy starts processing bars.

#### `onTick(Tick tick, Bar currentBar)`

- **Description**: Called on each tick. Here you can define your strategy logic that runs on each tick.

#### `onBarClose(Bar bar)`

- **Description**: Called when a bar closes. Here you can define your strategy logic that runs on each bar close.

#### `onTradeClose(Trade trade)`

- **Description**: Called when trade is closed. Some internal logic runs but this can be overridden to add custom logic.

#### `onNewDay(ZonedDateTime newDay)`

- **Description**: Called at the start of each new trading day. Here you can define your strategy logic that runs at the start of each new day.

#### `onEnd()`

- **Description**: Called when the strategy processing ends. Use this for cleanup or final calculations.

## Abstracted Functionality

The BaseStrategy class abstracts away several complex operations:

1. **Data Management**: Provides easy access to market data through the DataManager.
2. **Account Management**: Handles account-related operations via the AccountManager.
3. **Trade Execution**: Manages trade execution through the TradeManager.
4. **Event Handling**: Utilizes an EventPublisher for system-wide event management.
5. **Performance Analysis**: Integrates with a PerformanceAnalyser for strategy evaluation.
6. **Risk Management**: Incorporates risk management checks when opening trades.
7. **Indicator Handling**: Manages the creation and initialization of technical indicators.
8. **Parameter Management**: Handles strategy parameter initialization and updates.

By extending the BaseStrategy class, developers can create sophisticated trading strategies while focusing on their core trading logic, leaving the complexities of system integration and management to the AlgoTrade4j framework.