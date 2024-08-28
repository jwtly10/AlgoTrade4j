package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.model.Number;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParameterRange {
    private String value;
    private String name;
    private String start;
    private String end;
    private String step;
    private Boolean selected;

    public void validate() throws IllegalArgumentException {
        // If we are not using the parameter for optimisation. We don't need to validate its values
        if (!selected) {
            return;
        }

        Number startNum = new Number(start);
        Number endNum = new Number(end);
        Number stepNum = new Number(step);
        // Check if these values are ridiculously big
        if (startNum.isGreaterThan(new Number(4000))) {
            throw new IllegalArgumentException("Start value cannot be more than 4000 for parameter: " + name);
        }
        if (endNum.isGreaterThan(new Number(4000))) {
            throw new IllegalArgumentException("End value cannot be more than 4000 for parameter: " + name);
        }
        if (startNum.isGreaterThan(new Number(100))) {
            throw new IllegalArgumentException("Step value cannot be more than 100 for parameter: " + name);
        }

        // Check if end is below start
        if (endNum.compareTo(startNum) < 0) {
            throw new IllegalArgumentException("End value must be greater than or equal to start value for parameter: " + name);
        }

        // Check if step is positive
        if (stepNum.compareTo(new Number("0")) <= 0) {
            throw new IllegalArgumentException("Step must be a positive number for parameter: " + name);
        }

        // Skip the following checks if start and end are the same
        if (!startNum.equals(endNum)) {
            // Check if step is larger than the range
            if (stepNum.compareTo(endNum.subtract(startNum)) > 0) {
                throw new IllegalArgumentException("Step is larger than the range for parameter: " + name);
            }

            // Check if at least one step can be taken
            Number currentValue = startNum;
            boolean canTakeStep = false;
            while (currentValue.compareTo(endNum) <= 0) {
                canTakeStep = true;
                currentValue = currentValue.add(stepNum);
            }
            if (!canTakeStep) {
                throw new IllegalArgumentException("Invalid range: no valid steps can be taken for parameter: " + name);
            }
        }
    }
}