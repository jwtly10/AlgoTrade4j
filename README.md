# AlgoTrade4j

🏗️ Note this is a WIP project, and is not ready for production use yet. Documentation also WIP. 🏗️

AlgoTrade4j is a complete (strategy-dev, backtesting and live trading), high-performance algorithmic trading platform for Java, designed to be simple yet opinionated. Originally built to port strategies from MQL (MetaTrader4) to Java, it offers robust and flexible ways to backtest and extend functionality. The platform is capable of processing 50,000 ticks per second, making it suitable for relatively high-frequency trading strategies.

⚠️It is a bespoke implementation so may not be suitable for all use cases. ⚠️

Here is a small video demo of the system. 

https://github.com/user-attachments/assets/dc23724b-5104-4816-a33a-532f44149c36

## Key Features:

- Asynchronous event-driven architecture for responsive strategy execution
- Aggressively optimised & profiled for performance
- Dev /generate-heapdump endpoint for efficient memory profiling
- Clean interfaces for fetching bar data from multiple external providers
- Synthetic backtest tick generation from acquired bar data for enhanced price movement granularity
- Real-time updates via WebSockets and event publishers
- REST APIs for seamless external integrations
- High-performance processing (capable of handling 50,000+ ticks/second)
- Comprehensive test coverage
- Integrated frontend for strategy management and execution
- Dynamic strategy parameter annotation system for flexible configuration
- Advanced optimization tools for efficient backtesting
- Robust authentication and authorization system
- Separate live service for live trading, supporting independent scaling

## Architecture

The framework consists of 5 main components:

1. core Module: Contains the main trading logic, event system and implemented defaults
2. backtest-api Module: A Spring REST API for handling backtesting operations and core system operations
3. live-api Module: A Spring service for live trading, and other broker related operations
4. market-data Module: Manages the integration with external market data providers.
5. React Frontend Module: A React-based frontend providing a base user interface for interacting with the system.

This is a high-level overview of the framework and how it handles strategies:

- A data provider is initialised with the data that needs to be listened to (e.g. Broker price streams, or historical API data).
- A data manager is wrapper around this data provider, to transform raw data into ticks & bars.
- A data listener/executor is then created to listen callbacks from the data manager. This interface handles the data, and orchestrates other flows that are required for running a strategy:
  - Handles updating account data (PNL, balance, etc)
  - Handles updating trade stats (open trades, closed trades, etc)
  - Emits events to the running strategy instance.
- A strategy is then created, and the data listener is attached to it. The strategy is then run on the data listener, and the users strategy logic is executed on each event.

Each part of the system has access to the global async event publisher to support real-time updates and external communications.

## Getting started

### Usage (AlgoTrade4j-Core Library)

AlgoTrade4j-Core is the main logic module of the platform. It can be extended to create custom indicators, strategies or backtest engines. Some defaults have been created, which are used by default. Below is a brief overview of how you can implement your own strategies. Full documentation [here](). (TODO)

#### Strategies

To create new strategies you must extend the `BaseStrategy` [class](https://github.com/jwtly10/AlgoTrade4j/blob/main/algotrade4j-core/src/main/java/dev/jwtly10/core/strategy/BaseStrategy.java).

And implement the 'OnTick', 'OnBarClose' methods at minimum. And example of a strategy is [here](https://github.com/jwtly10/AlgoTrade4j/blob/main/algotrade4j-core/src/main/java/dev/jwtly10/core/strategy/SMACrossoverStrategy.java).

AlgoTrade4j supports dynamic strategy configuration through a custom annotation system. Strategy parameters can be defined using the `@Parameter` annotation:

```java
 // Risk parameters
@Parameter(name = "riskRatio", description = "Risk ratio of SL to TP", value = "5", group = "risk")
private int riskRatio;

// Indicator parameters
@Parameter(name = "atrLength", description = "Length of ATR", value = "14", group = "indicator")
private int atrLength;

// Trade parameters
@Parameter(name = "tradeDirection", description = "Direction to trade", value = "ANY", enumClass = TradeDir.class, group = "trade")
private TradeDir tradeDirection;

public enum TradeDir {
    LONG, SHORT, ANY
}
```

These annotated parameters are exposed via an API, allowing for dynamic updates at runtime. This feature enables:

- Real-time strategy adjustments without recompilation, via the frontend
- Rapid prototyping and optimization of trading strategies
- Real-time validation of strategy parameters and behavior

You can modify strategy behavior on-the-fly, significantly reducing the development and testing cycle for new trading ideas.

#### Indicators

To create new Indicators you must implement the `Indicator` [interface](https://github.com/jwtly10/AlgoTrade4j/blob/main/algotrade4j-core/src/main/java/dev/jwtly10/core/indicators/Indicator.java).

Currently, indicators only trigger on BarClose. This will be refined in future to be supported on tick where needed. Some indicators have already been implemented in `dev.jwtly10.core.indicators`.
[Example SMA](https://github.com/jwtly10/AlgoTrade4j/blob/main/algotrade4j-core/src/main/java/dev/jwtly10/core/indicators/iSMA.java)

Indicator params can be paired with strategy `@Parameter`'s as mentioned above, to change Indicator settings on-the-fly.

#### Market Data

Currently, only Oanda has been supported. You can find the implementation [here](https://github.com/jwtly10/AlgoTrade4j/blob/main/algotrade4j-market-data/src/main/java/dev/jwtly10/marketdata/dataclients/OandaDataClient.java). To implement new clients, the `ExternalDataClient` [interface](https://github.com/jwtly10/AlgoTrade4j/blob/main/algotrade4j-market-data/src/main/java/dev/jwtly10/marketdata/common/ExternalDataClient.java) must be implemented (The Oanda client was an example). Supported
symbols and Instrument meta data can be found [here](https://github.com/jwtly10/AlgoTrade4j/blob/main/algotrade4j-core/src/main/java/dev/jwtly10/core/model/Instrument.java).

### Running the application stack

#### Dev

*Backend*

The simplest way to run the application locally is through creating a Dockerized Postgres DB and running the application in Intellij.

To create a Postgres DB:

```shell
docker run --name algotrade4j-postgres -e POSTGRES_DB=algotrade4j-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=dev -p 5432:5432 -d postgres
```

Ensure the SQL in ./db has been run on the Database in schema algotrade4j, and all migration patches have been run.

Intellij Setup for both the backtest-api and live-api:

- Profile:
    - dev
- Environment Vars:
    - OANDA_API_URL=https://api-fxpractice.oanda.com;
    - OANDA_API_KEY=<your_api_key>;
    - TELEGRAM_BOT_TOKEN=7711666751:AAGDIcfDKo6NrwrEOvm-e_bdsoQTDp-7RII # This is required for live-api only

Properties are defined in the respective application.properties files.

*Frontend*

```shell
# From project root
cd ./algotrade4j-frontend
npm run dev
```

Supported environment vars can be found here: ./algotrade4j-frontend/.env

The frontend application should be running at localhost:5173, with the main-api running on localhost:8080 and live-api running on localhost:8081.

#### Creating new prod instances

THe build steps (CI/CD) are defined in the .github/workflows folder. Docker images are built and pushed to docker hub which can be used to deploy the application anywhere.

Requirements:
- Docker
- Docker-compose
- Nginx