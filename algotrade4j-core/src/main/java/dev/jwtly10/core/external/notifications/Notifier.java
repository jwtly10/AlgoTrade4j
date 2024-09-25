package dev.jwtly10.core.external.notifications;

/**
 * Interface for external alerts
 * Useful for sending alerts to external services, such as message platforms like Telegram
 */
public interface Notifier {
    void sendNotification(String chatId, String message, boolean isHtml);

    void sendErrorNotification(String chatId, String message, Exception e, boolean isHtml);
}