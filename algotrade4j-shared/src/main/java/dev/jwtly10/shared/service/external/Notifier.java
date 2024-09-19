package dev.jwtly10.shared.service.external;

/**
 * Interface for external alerts
 * Useful for sending alerts to external services, such as message platforms like Telegram
 */
public interface Notifier {
    void sendNotification(String chatId, String message, boolean isHtml);
}