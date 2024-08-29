package dev.jwtly10.core.utils;

import dev.jwtly10.core.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Slf4j
public class StrategyReflectionUtils {
    /**
     * Get a strategy instance from a class name.
     *
     * @param className The class name of the strategy.
     * @param customId  A custom strategy ID to use for the instance of the strategy. Can be null, in which case will not set a custom strategyId
     * @return A new instance of the strategy.
     */
    public static Strategy getStrategyFromClassName(String className, @Nullable String customId) throws Exception {
        try {
            Class<?> clazz;
            try {
                clazz = Class.forName("dev.jwtly10.core.strategy." + className);
            } catch (ClassNotFoundException e) {
                try {
                    clazz = Class.forName("dev.jwtly10.core.strategy.private_strats." + className);
                } catch (ClassNotFoundException err) {
                    log.error("Could not find strategy class name: {}", className, err);
                    throw err;
                }
            }

            Strategy strategy;

            if (customId != null) {
                try {
                    Constructor<?> constructor = clazz.getConstructor(String.class);
                    strategy = (Strategy) constructor.newInstance(customId);
                } catch (NoSuchMethodException e) {
                    log.error("No constructor with String parameter found for {}. All Strategy implementation must support a strategy ID constructor ", className);
                    throw e;
                }
            } else {
                // Use the no-arg constructor if customId is null
                Constructor<?> constructor = clazz.getConstructor();
                strategy = (Strategy) constructor.newInstance();
            }

            return strategy;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("Error initializing strategy: {}", className, e);
            throw new Exception("Error getting strategy from " + className + ": " + e.getClass() + " " + e.getMessage());
        }
    }
}