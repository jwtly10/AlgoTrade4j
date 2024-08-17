# AlgoTrade4j

🏗️ Note this is a WIP project, and it's not ready for production use yet. Documentation is also WIP. 🏗️

AlgoTrade4j is an algo trading framework for Java, it's designed to be simple to use, but opinionated. I built this as a way to port my MT4 trading strategies to Java, and to have a more robust and flexible way to backtest and run my trading strategies, and make extending functionality easier.

As such, it's built with my personal needs in mind, and may not be suitable for all use cases.

You can use a demo of the system with some prebuilt strategies [here]().

## Architecture

The framework consists of 4 main components:

1. Core Module: Contains the main trading logic, event system and implemented defaults
2. API Module: A Spring-based REST API for handling external requests and WebSocket connections
3. Market Data Module: Manages the integration with external market data providers.
4. React Frontend: A React-based frontend providing a user interface for interacting with the system.

The system utilizes an event-driven architecture with a global event publisher for external communications.

## Features:

- Event driven. Responsive strategy execution and easily extendable to external systems.
- Well tested, with a focus on TDD.
- Built in frontend for managing and executing strategies.
- Built in optimization tools. Run thousands of backtests with different parameters in seconds.
- Built in user management, you can share strategies with other users, and have members of the team run/test strategies.

## Getting started

### Personal Development

#### Strategies

To create new strategies you must extend the `BaseStrategy`[class](https://github.com/jwtly10/AlgoTrade4j/blob/568adcebd76611c1aaff954e20a3362eab5206ee/algotrade4j-core/src/main/java/dev/jwtly10/core/strategy/BaseStrategy.java).

And implement the 'OnTick', 'OnBarClose' methods at minimum. And example of a strategy is [here](https://github.com/jwtly10/AlgoTrade4j/blob/568adcebd76611c1aaff954e20a3362eab5206ee/algotrade4j-core/src/main/java/dev/jwtly10/core/strategy/SimpleSMAStrategy.java).

#### Indicators

To create new Indicators you must implement the `Indicator` [interface](https://github.com/jwtly10/AlgoTrade4j/blob/568adcebd76611c1aaff954e20a3362eab5206ee/algotrade4j-core/src/main/java/dev/jwtly10/core/indicators/Indicator.java).

Note indicators in this framework are very opinionated. This works for the indicators that I use, but may not work for all of them. Some indicators have already been implemented in `dev.jwtly10.core.indicators`.
[Example SMA](https://github.com/jwtly10/AlgoTrade4j/blob/568adcebd76611c1aaff954e20a3362eab5206ee/algotrade4j-core/src/main/java/dev/jwtly10/core/indicators/SMA.java)

#### Market Data

Currently, I have only implemented the Brokers/External Data providers that I personally use. So far that has just been Oanda and you can find the implementation [here](). To implement new clients, the `ExternalDataClient` [interface]() must be implemented (The Oanda client was an example).