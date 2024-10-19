# User Guide: Creating New Strategies

## Overview

This guide will walk you through the process of creating a new trading strategy in AlgoTrade4j. Strategies in AlgoTrade4j extend the `BaseStrategy` class, which provides a rich set of utilities and abstractions for common trading operations.

## Prerequisites

Before creating a new strategy, ensure you're familiar with:

- Java programming
- Basic concepts of algorithmic trading
- AlgoTrade4j's [`BaseStrategy`](./components/base-strategy.md) class and its methods

## Step-by-Step Guide

### 1. Create a New Class

Start by creating a new Java class that extends [`BaseStrategy`](./components/base-strategy.md):

```java
import dev.jwtly10.core.strategy.BaseStrategy;

public class MyNewStrategy extends BaseStrategy {
    public MyNewStrategy() {
        super("MyNewStrategyID");
    }
}
```

### 2. Define Strategy Parameters

Use the [`@Parameter`](./components/parameters.md) annotation to define configurable parameters for your strategy:

```java 
import dev.jwtly10.core.strategy.Parameter;

@Parameter(name = "stopLossTicks", description = "Tick size of the stop loss", value = "300", group = "Risk")
private int stopLossTicks;

@Parameter(name = "riskRatio", description = "Risk ratio of SL to TP", value = "5", group = "Risk")
private int riskRatio;

@Parameter(name = "shortSMALength", description = "Length of short-term SMA", value = "50", group = "Indicator")
private int shortSMALength;
```

Parameters can be grouped for better organization. The value field sets the default value.

### 3. Initialize Indicators

Override the `initIndicators()` method to create and initialize your strategy's indicators:

```java

@Override
protected void initIndicators() {
    shortSMA = createIndicator(iSMA.class, shortSMALength);
    atrIndicator = createIndicator(iATRCandle.class, atrLength, atrSensitivity, relativeSize);
}
```

Use the [`createIndicator()`](components/base-strategy.md/#createindicatorclasst-indicatorclass-object-params) method provided by BaseStrategy to instantiate indicators.

### 4. Implement Required Methods

At a minimum, you need to implement the following methods:

```java

@Override
public void onTick(Tick tick, Bar currentBar) {
// React to each new tick
}

@Override
public void onBarClose(Bar bar) {
// Implement your main strategy logic here
}

@Override
public RiskProfileConfig getRiskProfileConfig() {
    return riskProfile.getConfig();
}
```

### 5. Implement Strategy Logic

In the onBarClose/onTickMethod method, implement your strategy's core logic. This typically involves:

1. Checking if indicators are ready
2. Evaluating entry conditions
3. Opening trades if conditions are met

Or any other custom ideas you may have

Example:

```java

@Override
public void onBarClose(Bar bar) {
    if (isReady() && isTimeToTrade(bar)) {
        if (entryCheck(bar, true)) {
            var id = openLong(createTradeParameters(true));
            sendNotification(String.format("Opened long trade %s @  %s", id, Ask()));
        } else if (entryCheck(bar, false)) {
            var id = openShort(createTradeParameters(false));
            sendNotification(String.format("Opened short trade %s @ %s", id, Bid()));
        }
    }
}
```

### 6. Utilize BaseStrategy Methods

Make use of the methods provided by[`BaseStrategy`](./components/base-strategy.md/#methods-for-strategy-implementation):

Here are a few main ones:

- [`openLong()`](./components/base-strategy.md/#openlongtradeparameters-params) and [`openShort()`](./components/base-strategy.md/#openshorttradeparameters-params) for opening trades
- [`Ask()`](./components/base-strategy.md/#ask) and [`Bid()`](./components/base-strategy.md/#bid) for current market prices
- [`sendNotification()`](./components/base-strategy.md/#sendnotificationstring-message) for sending alerts
- [`getStopLossGivenInstrumentPriceDir()`](./components/base-strategy.md/#getstoplossgiveninstrumentpricedirinstrument-instrument-number-price-int-ticks-boolean-islong) for calculating stop loss prices without any manual calcs

## Conclusion

By following this guide, you should be able to create a new strategy that leverages the power of AlgoTrade4j's BaseStrategy, and its platform.

## Examples

[SMACrossoverStrategy](https://github.com/jwtly10/AlgoTrade4j/blob/main/algotrade4j-core/src/main/java/dev/jwtly10/core/strategy/demo/SMACrossoverStrategy.java)