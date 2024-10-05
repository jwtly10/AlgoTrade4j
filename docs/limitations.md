# Limitations

While AlgoTrade4j implements many key features of a trading platform there are some limitations:

### HFT:

- The platform is profiled, well tested and optimisation for performance, but HFT is not a goal of the platform. While you may have success with HFT strategies, the platform is not designed for this use case, and there will be edge cases where the platform may not be suitable.
- The system has not been stress tested for HFT strategies, and issues may arise under high load, which will not be addressed for now.

### Live Trading:

- While being used internally, the live trading service is not mature enough yet to assure an error-free experience.
- It's recommended to not yet use the live service for real funds, and we take no responsibility for any errors which may cause you to lose money.
- It's recommend to use the backtesting application to quickly test over millions of data points, and reimplement your strategy in a supported platform.