package dev.jwtly10.core.indicators;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.strategy.Strategy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class IndicatorUtils {
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