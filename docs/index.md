# Welcome to AlgoTrade4j

🏗️ Note: This project is currently a work in progress and not yet ready for production live trading use 🏗️

## Introduction

AlgoTrade4j is a high-performance, comprehensive algorithmic trading platform designed for Java developers. Originally conceived to port strategies from MQL (MetaTrader4) to Java, it has evolved into a robust and flexible system capable of strategy development, backtesting, and live trading.

## Key Features

- **High Performance**: Processes up to 50,000 ticks per second.
- **Asynchronous Architecture**: Event-driven design ensures responsive strategy execution.
- **Flexible Data Integration**: Clean interfaces for fetching bar data from multiple external providers.
- **Advanced Backtesting**: Synthetic tick generation from bar data for enhanced price movement granularity.
- **Real-time Updates**: WebSocket support and event publishers for live data streaming.
- **REST APIs**: Seamless external integrations for extended functionality.
- **Comprehensive Testing**: Extensive test coverage for reliability.
- **Integrated Frontend**: React-based UI for strategy management and execution.
- **Dynamic Configuration**: Annotation-based system for flexible strategy parameterization.
- **Optimization Tools**: Advanced utilities for efficient backtesting and strategy optimization.
- **Robust Security**: Integrated authentication and authorization system.
- **Scalable Architecture**: Separate services for backtesting and live trading, supporting independent scaling.

## System Architecture

AlgoTrade4j consists of five main components:

1. **Core Module**: Contains the main trading logic, event system, and implemented defaults.
2. **Backtest API Module**: A Spring REST API for handling backtesting operations and core system functions.
3. **Live API Module**: A Spring service for live trading and broker-related operations.
4. **Market Data Module**: Manages integration with external market data providers.
5. **React Frontend Module**: Provides a user interface for interacting with the system.

## Getting Started

To begin using AlgoTrade4j, you'll want to familiarize yourself with:

- [Creating Strategies](lib/user-guide.md): Learn how to implement your trading algorithms.
- [Configuring Parameters](lib/components/parameters.md): Understand how to use the `@Parameter` annotation for dynamic strategy configuration.
- [Using Indicators](lib/components/indicators.md): Explore how to create and use technical indicators in your strategies.
- [Backtesting](frontend/backtesting.md): Learn how to test your strategies against historical data.
- [Live Trading](frontend/live-trading.md): Understand how to deploy your strategies in a live trading environment.

## Development and Deployment

Guides coming soon

[//]: # (For detailed instructions on setting up a development environment or deploying AlgoTrade4j, please refer to our [Development Guide]&#40;development-guide.md&#41; and [Deployment Guide]&#40;deployment-guide.md&#41;.)

## Important Notes

⚠️ AlgoTrade4j is a bespoke implementation and may not be suitable for all use cases. Always thoroughly test strategies before live deployment and use at your own risk.