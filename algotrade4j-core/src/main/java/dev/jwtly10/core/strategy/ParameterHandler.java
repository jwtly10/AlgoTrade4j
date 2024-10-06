package dev.jwtly10.core.strategy;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Utility class for handling parameters annotated with {@link Parameter}.
 * This class provides methods to retrieve, validate, and set parameter values for strategies.
 */
public class ParameterHandler {

    /**
     * Validates the run parameters against the strategy's @Parameter annotations.
     * This only validates the parameter names and coercible types, not the values.
     * So in the case of ENUMS, it wouldn't validate the value is valid
     *
     * @param strategy  The strategy instance to validate against.
     * @param runParams A map of parameter names to their values.
     * @throws IllegalArgumentException If validation fails.
     */
    public static void validateRunParameters(Strategy strategy, Map<String, String> runParams) {
        Class<?> clazz = strategy.getClass();
        Map<String, Parameter> annotatedParams = new HashMap<>();

        // Collect all @Parameter annotations
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Parameter.class)) {
                Parameter param = field.getAnnotation(Parameter.class);
                annotatedParams.put(param.name(), param);
            }
        }

        // Validate each provided run parameter
        for (Map.Entry<String, String> entry : runParams.entrySet()) {
            String paramName = entry.getKey();
            String paramValue = entry.getValue();

            if (!annotatedParams.containsKey(paramName)) {
                throw new IllegalArgumentException("Unexpected parameter: " + paramName);
            }

            Parameter annotation = annotatedParams.get(paramName);
            Field field = null;
            try {
                field = clazz.getDeclaredField(paramName);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("No field found for parameter: " + paramName);
            }

            // Validate type
            validateParameterType(field.getType(), paramValue, annotation);
        }

        // Check if all required parameters are provided
        for (String annotatedParam : annotatedParams.keySet()) {
            if (!runParams.containsKey(annotatedParam)) {
                throw new IllegalArgumentException("Missing required parameter: " + annotatedParam);
            }
        }
    }

    private static void validateParameterType(Class<?> fieldType, String value, Parameter annotation) {
        try {
            if (fieldType == int.class || fieldType == Integer.class) {
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    // If parsing as int fails, try parsing as double and then cast to int
                    // This fixes an issue caused by type casting to double during parameter combination generation.
                    // But decided to leave the type casting as is as more precision can be helpful in future
                    double doubleValue = Double.parseDouble(value);
                    int intValue = (int) doubleValue;
                    // Check if the cast was lossless
                    if (intValue == doubleValue) {
                        // Use intValue
                    } else {
                        throw new IllegalArgumentException("Value " + value + " cannot be represented as an integer without loss of precision");
                    }
                }
            } else if (fieldType == double.class || fieldType == Double.class) {
                Double.parseDouble(value);
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                Boolean.parseBoolean(value);
            } else if (fieldType.isEnum()) {
                Enum.valueOf((Class<Enum>) fieldType, value);
            } else if (fieldType != String.class) {
                throw new IllegalArgumentException("Unsupported parameter type: " + fieldType.getSimpleName());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for [" + fieldType.toGenericString() + "] parameter " + annotation.name() + ": " + value);
        }
    }

    /**
     * Retrieves all parameters annotated with {@link Parameter} from the given object.
     *
     * @param strategy The strategy to retrieve parameters from.
     * @return A List of {@link ParameterInfo} objects containing parameter information.
     */
    public static List<ParameterInfo> getParameters(Strategy strategy) {
        List<ParameterInfo> parameters = new ArrayList<>();
        Class<?> clazz = strategy.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Parameter.class)) {
                Parameter param = field.getAnnotation(Parameter.class);
                field.setAccessible(true);
                String value;
                try {
                    value = field.get(strategy).toString();
                } catch (IllegalAccessException e) {
                    value = "N/A";
                }

                String type = getTypeString(field.getType());
                Class<? extends Enum<?>> enumClass = param.enumClass().length > 0 ? param.enumClass()[0] : null;

                parameters.add(new ParameterInfo(
                        param.name(),
                        param.description(),
                        value,
                        param.group(),
                        type,
                        enumClass
                ));
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

    /**
     * Sets the parameters of the given strategy using the provided map of parameter values.
     *
     * @param strategy   The strategy to set parameters for.
     * @param parameters A map of parameter names and values.
     * @throws IllegalAccessException If unable to access a field.
     */
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

    /**
     * Sets the value of a specific parameter in the given strategy.
     *
     * @param strategy The object containing the parameter to set.
     * @param field    The field representing the parameter to set.
     * @param value    The value to set the parameter to, as a string.
     * @throws IllegalAccessException If unable to access the field.
     */
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
            try {
                // This handles the case of a value like 10.00 being passed in
                // First, try parsing as an integer
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // If that fails, try parsing as a double and then convert to int
                try {
                    double doubleValue = Double.parseDouble(value);
                    return (int) Math.round(doubleValue);
                } catch (NumberFormatException e2) {
                    throw new IllegalArgumentException("Cannot convert '" + value + "' to int", e2);
                }
            }
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

    /**
     * Converts a class type to a string representation.
     */
    private static String getTypeString(Class<?> type) {
        if (type == int.class || type == Integer.class) return "int";
        if (type == boolean.class || type == Boolean.class) return "boolean";
        if (type == double.class || type == Double.class) return "double";
        if (type == String.class) return "string";
        if (type.isEnum()) return "enum";
        return type.getSimpleName();
    }

    /**
     * Information about a parameter.
     * A DTO-style class to make it easier to pass parameter information around.
     */
    @Data
    public static class ParameterInfo {
        private String name;
        private String description;
        private String value;
        private String group;
        private String type;
        private List<String> enumValues;

        public ParameterInfo(String name, String description, String value, String group, String type) {
            this(name, description, value, group, type, null);
        }

        public ParameterInfo(String name, String description, String value, String group, String type, Class<? extends Enum<?>> enumClass) {
            this.name = name;
            this.description = description;
            this.value = value;
            this.group = group;
            this.type = type;

            if (enumClass != null) {
                this.enumValues = Arrays.stream(enumClass.getEnumConstants())
                        .map(Enum::name)
                        .toList();
            }
        }
    }
}