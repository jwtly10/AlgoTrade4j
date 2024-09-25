package dev.jwtly10.core.indicators;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.strategy.Strategy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling indicators within a strategy.
 */
public class IndicatorUtils {

    /**
     * Initializes all indicators in the given strategy with the provided list of bars.
     *
     * @param strategy the strategy containing the indicators to be initialized
     * @param bars     the list of bars used to initialize the indicators
     */
    public static void initializeIndicators(Strategy strategy, List<Bar> bars) {
        List<Indicator> indicators = getIndicators(strategy);

        for (Indicator indicator : indicators) {
            for (Bar bar : bars) {
                indicator.update(bar);
            }
        }
    }

    /**
     * Updates all indicators in the given strategy with the provided bar data.
     *
     * @param strategy the strategy containing the indicators to be updated
     * @param bar      the bar data used to update the indicators
     */
    public static void updateIndicators(Strategy strategy, Bar bar) {
        Class<?> strategyClass = strategy.getClass();
        Field[] fields = strategyClass.getDeclaredFields();

        for (Field field : fields) {
            if (Indicator.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    Indicator indicator = (Indicator) field.get(strategy);
                    if (indicator != null) {
                        indicator.update(bar);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error updating indicator: " + field.getName(), e);
                }
            }
        }
    }

    /**
     * Retrieves all indicators from the given strategy.
     *
     * @param strategy the strategy containing the indicators to be retrieved
     * @return a list of indicators in the strategy
     */
    public static List<Indicator> getIndicators(Strategy strategy) {
        List<Indicator> indicators = new ArrayList<>();
        Class<?> strategyClass = strategy.getClass();
        Field[] fields = strategyClass.getDeclaredFields();

        for (Field field : fields) {
            if (Indicator.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    Indicator indicator = (Indicator) field.get(strategy);
                    if (indicator != null) {
                        indicators.add(indicator);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error accessing indicator: " + field.getName(), e);
                }
            }
        }

        return indicators;
    }
}