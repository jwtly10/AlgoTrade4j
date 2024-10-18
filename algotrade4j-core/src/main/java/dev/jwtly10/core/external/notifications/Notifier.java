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


    /**
     * Internal method used for the system to send notification
     * Allows for defining a global system chat
     *
     * @param message the message to send
     * @param isHtml  whether the message is HTML or not
     */
    void sendSysNotification(String message, boolean isHtml);

    /**
     * Internal method used for the system to send error notification
     *
     * @param message the message to send
     * @param e       the exception that caused the error
     * @param isHtml  whether the message is HTML or not
     */
    void sendSysErrorNotification(String message, Exception e, boolean isHtml);
}