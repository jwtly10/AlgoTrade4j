# Indicators in AlgoTrade4j

## Overview

Indicators are essential tools in technical analysis, used to analyze price movements and trends in financial markets. In AlgoTrade4j, indicators are implemented as classes that conform to the `Indicator` interface.

## The Indicator Interface

The `Indicator` interface defines the core functionality that all indicators must implement:

```java
public interface Indicator {
    List<IndicatorValue> getValues();

    void update(Bar bar);

    double getValue();

    double getValue(int index);

    String getName();

    boolean isReady();

    int getRequiredPeriods();

    void setEventPublisher(EventPublisher eventPublisher);

    void setStrategyId(String strategyId);
}
```

## Implementing a Custom Indicator

To create a custom indicator, follow these steps:

- Create a new class that implements the Indicator interface.
- Implement all required methods.
- Add any additional fields or methods specific to your indicator.

### Example: Simple Moving Average (SMA) Indicator

Here's a simplified example of how to implement a Simple Moving Average (SMA) indicator:

``` java
public class iSMA implements Indicator {
private final int period;
private final List<Double> rawValues;
private final List<IndicatorValue> values;
private final String name;
private String strategyId;
private EventPublisher eventPublisher;

    public iSMA(int period) {
        this.period = period;
        this.rawValues = new ArrayList<>();
        this.values = new ArrayList<>();
        this.name = "SMA " + period;
    }

    @Override
    public void update(Bar bar) {
        rawValues.add(bar.getClose().getValue().doubleValue());
        if (isReady()) {
            double sum = rawValues.subList(rawValues.size() - period, rawValues.size())
                    .stream().mapToDouble(Double::doubleValue).sum();
            double average = sum / period;
            IndicatorValue indicatorValue = new IndicatorValue(average, bar.getOpenTime());
            values.add(indicatorValue);
            if (eventPublisher != null) {
                eventPublisher.publishEvent(new IndicatorEvent(strategyId, bar.getInstrument(), getName(), indicatorValue));
            }
        }
    }

    @Override
    public double getValue() {
        return values.isEmpty() ? 0 : values.get(values.size() - 1).getValue();
    }

    // Implement other methods...

}
```

## Key Considerations

When implementing a custom indicator, keep the following in mind:

- **Performance**: Indicators are updated frequently. Ensure your implementation is efficient, especially for computationally intensive indicators.
- **Accuracy**: Double-check your calculations against known good implementations to ensure accuracy.
- **Event Publishing**: Use the eventPublisher to notify the system of new indicator values. This is crucial for real-time updates and visualization.
- **Initialization**: Handle the case where there's not enough data to calculate the indicator value (before isReady() returns true).
- **Historical Values**: Implement getValue(int index) correctly to allow strategies to access past indicator values.
- **Threading**: Ensure your indicator is thread-safe if it will be used in a multi-threaded environment.

## Using Custom Indicators in Strategies

To use your custom indicator in a strategy:

1, Create the indicator in your strategy's initIndicators() method:

```java 

@Override
protected void initIndicators() {
    smaIndicator = createIndicator(iSMA.class, 14);
}
```

2, Use the indicator in your strategy logic:

```java

@Override
public void onBarClose(Bar bar) {
    if (smaIndicator.isReady()) {
        double smaValue = smaIndicator.getValue();
// Your strategy logic here
    }
}
```

## Built-in Indicators

AlgoTrade4j comes with several built-in indicators. You can find these in the `dev.jwtly10.core.indicators` package. These serve as examples and can be used directly in your strategies.

## Best Practices

- Name your indicator classes with a lowercase 'i' prefix (e.g., `iSMA`, `iRSI`) for consistency with built-in indicators.
- Document your indicator thoroughly, especially if it involves complex calculations.
- Write unit tests for your indicators to ensure they behave correctly under various market conditions.

By following these guidelines, you can create custom indicators for use in your AlgoTrade4j strategies.