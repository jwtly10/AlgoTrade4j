package dev.jwtly10.core.external.notifications;

/**
 * Interface for external alerts
 * Useful for sending alerts to external services, such as message platforms like Telegram
 * // TODO: Should we refactor for telegram or other services?
 */
public interface Notifier {
    /**
     * Send a notification to a chat
     *
     * @param chatId  The chat id to send the message to
     * @param message The message to send
     * @param isHtml  Whether the message is HTML or not
     */
    void sendNotification(String chatId, String message, boolean isHtml);

    /**
     * Send an error notification to a chat
     *
     * @param chatId  The chat id to send the message to
     * @param message The message to send
     * @param e       The exception that caused the error
     * @param isHtml  Whether the message is HTML or not
     */
    void sendErrorNotification(String chatId, String message, Exception e, boolean isHtml);
}