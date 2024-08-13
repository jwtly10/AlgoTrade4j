package dev.jwtly10.core.strategy;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling parameters annotated with {@link Parameter}.
 * This class provides methods to retrieve, validate, and set parameter values for strategies.
 */
public class ParameterHandler {

    /**
     * Retrieves all parameters annotated with {@link Parameter} from the given object.
     *
     * @param strategy The strategy to retrieve parameters from.
     * @return A Map containing parameter names as keys and their values as strings.
     */
    public static Map<String, String> getParameters(Strategy strategy) {
        Map<String, String> parameters = new HashMap<>();
        Class<?> clazz = strategy.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Parameter.class)) {
                Parameter param = field.getAnnotation(Parameter.class);
                parameters.put(param.name(), param.value());
            }
        }

        return parameters;
    }

    /**
     * Initializes all parameters annotated with {@link Parameter} in the given strategy.
     * This method validates the parameters and sets their values, ONLY if they are not already set.
     *
     * @param strategy The strategy to initialize parameters for.
     * @throws IllegalAccessException If unable to access a field.
     * @throws RuntimeException       If an error occurs during parameter initialization.
     */
    public static void initialize(Strategy strategy) throws IllegalAccessException {
        validateParameters(strategy);
        Class<?> clazz = strategy.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Parameter.class)) {
                field.setAccessible(true);
                Object currentValue = field.get(strategy);

                if (currentValue == null || isDefaultValue(currentValue, field.getType())) {
                    Parameter param = field.getAnnotation(Parameter.class);
                    try {
                        setParameter(strategy, param.name(), param.value());
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException("Error initializing parameter: " + param.name(), e);
                    }
                }
            }
        }
    }

    /**
     * Checks if the given value is the default value for the specified type.
     *
     * @param value The value to check.
     * @param type  The type of the value.
     * @return True if the value is the default value for the type, false otherwise.
     */
    private static boolean isDefaultValue(Object value, Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return ((Integer) value) == 0;
        } else if (type == double.class || type == Double.class) {
            return ((Double) value) == 0.0;
        } else if (type == boolean.class || type == Boolean.class) {
            return !((Boolean) value);
        } else if (type == String.class) {
            return value.equals("");
        }
        return false;
    }

    /**
     * Validates all parameters annotated with {@link Parameter} in the given strategy.
     * Ensures that all parameters have a non-empty value defined.
     *
     * @param strategy The object to validate parameters for.
     * @throws IllegalStateException If a parameter has an empty value.
     */
    public static void validateParameters(Strategy strategy) throws IllegalStateException {
        Class<?> clazz = strategy.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Parameter.class)) {
                Parameter param = field.getAnnotation(Parameter.class);
                if (param.value().isEmpty()) {
                    throw new IllegalStateException("Parameter '" + param.name() + "' must have a value defined in @Parameter.value");
                }
            }
        }
    }

    public static void setParameters(Strategy strategy, Map<String, String> parameters) throws IllegalAccessException {
        Class<?> clazz = strategy.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Parameter.class)) {
                Parameter param = field.getAnnotation(Parameter.class);
                String value = parameters.getOrDefault(param.name(), param.value());
                setParameterValue(strategy, field, value);
            }
        }
    }

    private static void setParameterValue(Strategy strategy, Field field, String value) throws IllegalAccessException {
        field.setAccessible(true);
        Object convertedValue = convertValue(value, field.getType());
        field.set(strategy, convertedValue);
    }

    /**
     * Sets the value of a specific parameter in the given strategy.
     *
     * @param strategy  The object containing the parameter to set.
     * @param paramName The name of the parameter to set.
     * @param value     The value to set the parameter to, as a string.
     * @throws NoSuchFieldException   If no parameter with the given name is found.
     * @throws IllegalAccessException If unable to access the field.
     */
    public static void setParameter(Strategy strategy, String paramName, String value) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = strategy.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Parameter.class)) {
                Parameter param = field.getAnnotation(Parameter.class);
                if (param.name().equals(paramName)) {
                    field.setAccessible(true);

                    // Convert the string value to the appropriate type
                    Object convertedValue = convertValue(value, field.getType());

                    field.set(strategy, convertedValue);
                    return;
                }
            }
        }

        throw new NoSuchFieldException("No parameter found with name: " + paramName);
    }

    /**
     * Converts a string value to the appropriate type based on the given class.
     *
     * @param value The string value to convert.
     * @param type  The class type to convert the value to.
     * @return The converted value.
     * @throws IllegalArgumentException If the value is null or cannot be converted to the specified type.
     */
    private static Object convertValue(String value, Class<?> type) {
        if (value == null) {
            throw new IllegalArgumentException("Parameter value cannot be null");
        }
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type.isEnum()) {
            return Enum.valueOf((Class<Enum>) type, value);
        }
        // TODO: Do we need to support other types
        return value; // Default to string
    }
}