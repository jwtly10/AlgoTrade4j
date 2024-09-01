# AlgoTrade4j

🏗️ Note this is a WIP project, and is not ready for production use yet. Documentation also WIP. 🏗️

AlgoTrade4j is a complete, high-performance algorithmic trading platform for Java, designed to be simple yet opinionated. Originally built to port strategies from MQL (MetaTrader4) to Java, it offers robust and flexible ways to backtest and extend functionality. The platform is capable of processing 50,000 ticks per second, making it suitable for high-frequency trading strategies.

⚠️It is a bespoke implementation so may not be suitable for all use cases. ⚠️

Here is a small video demo of the system. You can interact directly with this [demo site]() (TODO).

https://github.com/user-attachments/assets/c3e88572-b61b-4977-a7d8-7d9efc36b7d0

## Key Features:

- Extensive server side APM integration with DataDog, for monitoring and optimisation
- Asynchronous event-driven architecture for responsive strategy execution
- Aggressively optimised for performance
- /generate-heapdump endpoint for on quick memory profiling
- External API integration for fetching bar data from multiple providers
- Synthetic tick generation from acquired bar data for enhanced price movement granularity
- Real-time updates via WebSockets and event publishers
- REST APIs for seamless external integrations
- High-performance processing (capable of handling 50,000+ ticks/second)
- Comprehensive test coverage (80%+ in core)
- Integrated frontend for strategy management and execution
- Dynamic strategy parameter annotation system for flexible configuration
- Advanced optimization tools for efficient backtesting
- Robust authentication and authorization system

## Architecture

The framework consists of 4 main components:

1. Core Module: Contains the main trading logic, event system and implemented defaults
2. API Module: A Spring-based REST API for handling external requests and WebSocket connections
3. Market Data Module: Manages the integration with external market data providers.
4. React Frontend Module: A React-based frontend providing a base user interface for interacting with the system.

The system utilizes an event-driven architecture with a global event publisher for external communications.

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
```

These annotated parameters are exposed via an API, allowing for dynamic updates at runtime. This feature enables:

- Real-time strategy adjustments without recompilation, via the frontend
- Rapid prototyping and optimization of trading strategies

You can modify strategy behavior on-the-fly, significantly reducing the development and testing cycle for new trading ideas.

#### Indicators

To create new Indicators you must implement the `Indicator` [interface](https://github.com/jwtly10/AlgoTrade4j/blob/main/algotrade4j-core/src/main/java/dev/jwtly10/core/indicators/Indicator.java).

Note indicators in this framework are very opinionated. They only trigger on BarClose which works for the Indicators that we use internally. This will be refined in future. Some indicators have already been implemented in `dev.jwtly10.core.indicators`.
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

Ensure the SQL in ./algotrade4j-api/db has been run on the Database in schema algotrade4j.

Intellij Setup:

- Profile:
    - dev
- Environment Vars:
    - OANDA_API_URL=https://api-fxpractice.oanda.com;
    - OANDA_API_KEY=<your_api_key>;
    - OANDA_ACCOUNT_ID=<your_account_id>

Other application properties can be found here: ./algotrade4j-api/src/main/resources/application*.properties

If you have a DataDog instance & agent running on your local machine and want to see metrics. You can enable this and JMX metrics by including:

- VMOptions:
  -javaagent:/Path/to/dd-java-agent.jar
  -Ddd.service=algotrade4j-api-dev
  -Ddd.env=local
  -Dcom.sun.management.jmxremote
  -Dcom.sun.management.jmxremote.port=9010
  -Dcom.sun.management.jmxremote.authenticate=false
  -Dcom.sun.management.jmxremote.ssl=false
  -Ddd.tags=env:local

*Frontend*

```shell
# From project root
cd ./algotrade4j-frontend
npm run dev
```

Supported environment vars can be found here: ./algotrade4j-frontend/.env

The frontend application should now be running at localhost:5173, with the api running on localhost:8080

#### Docker/Staging

*Not supported yet - Docker/Docker-compose files in project are used for production instances*

#### Creating new prod instances

Some basic steps for running the API application on an Ubuntu VM

1. Install Docker/DockerCompose/Nginx
2. See ./.github/workflows/main-ci.yml for example deploy steps, these steps can be used on any clean ubuntu environment to deploy the application
