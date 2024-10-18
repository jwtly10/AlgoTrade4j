# Parameter Annotation

The `@Parameter` annotation is a crucial component in defining configurable parameters for trading strategies in AlgoTrade4j. It allows for dynamic configuration and optimization of strategy parameters.

You can of course define your own variables in the strategy class, but they will not have access to the runtime dynamic updates that `@Parameter` annotated variables do.

## Purpose

The `@Parameter` annotation serves several important purposes:

1. **Strategy Configuration**: It defines parameters that can be adjusted when initializing or running a strategy.
2. **Optimization**: It allows the optimizer to access and modify strategy parameters during backtesting and optimization processes.
3. **UI Integration**: It provides metadata for generating user interfaces for strategy configuration.

## Usage

To use the `@Parameter` annotation, apply it to fields in your strategy class that you want to make configurable. The annotation takes several arguments to define the parameter's properties.

### Example

```java
public class MyStrategy extends BaseStrategy {
    @Parameter(name = "stopLossTicks", description = "Tick size of the stop loss", value = "300", group = "Risk")
    private int stopLossTicks;

    @Parameter(name = "riskRatio", description = "Risk ratio of SL to TP", value = "5", group = "Risk")
    private int riskRatio;

    @Parameter(name = "riskPercentage", description = "Percentage of balance to risk per trade", value = "1", group = "Risk")
    private double riskPercentage;

    @Parameter(name = "balanceToRisk", description = "Balance to risk (static)", value = "10000", group = "Risk")
    private double balanceToRisk;

    @Parameter(name = "riskProfile", description = "Risk profile to use", value = "NONE", enumClass = RiskProfile.class, group = "Risk")
    private RiskProfile riskProfile;

    // Strategy implementation...
}
```

### Annotation Properties

- **name**: The name of the parameter. It should match the variable name it's annotating.
- **description**: A brief description of the parameter's purpose or effect.
- **value**: The default value of the parameter.
- **group**: Used for UI customization and parameter organization.
- **enumClass**: Specifies the enum class if the parameter is an enumeration type.

### Important Notes

- The name property should match the variable name it's annotating. This is used to match the parameter to the variable during runtime configuration.
- The [ParameterHandler](https://github.com/jwtly10/AlgoTrade4j/blob/main/algotrade4j-core/src/main/java/dev/jwtly10/core/strategy/ParameterHandler.java) class in AlgoTrade4j uses reflection to process these annotations, allowing for dynamic parameter handling and validation.
- Parameters can be of various types including int, double, boolean, String, and custom enum types, which all will be validated during runtime.

### Benefits

- Flexibility: Easily adjust strategy behavior without modifying code.
- Optimization: Enables automated parameter tuning during backtesting.
- User-Friendly: Facilitates the creation of intuitive UIs for strategy configuration.
- Standardization: Provides a consistent way to define and handle strategy parameters across different strategies.