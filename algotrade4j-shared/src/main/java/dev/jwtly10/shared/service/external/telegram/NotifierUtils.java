package dev.jwtly10.shared.service.external.telegram;

public class NotifierUtils {
    public static String formatError(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Error: %s - %s\n",
                e.getClass().getSimpleName(), e.getMessage()));

        StackTraceElement[] stackTrace = e.getStackTrace();
        int linesToInclude = Math.min(10, stackTrace.length); // Include up to 10 lines of stack trace

        sb.append("Stack trace:\n");
        for (int i = 0; i < linesToInclude; i++) {
            sb.append("  at ").append(stackTrace[i].toString()).append("\n");
        }

        if (stackTrace.length > linesToInclude) {
            sb.append("  ... ").append(stackTrace.length - linesToInclude).append(" more\n");
        }

        return sb.toString();
    }

}