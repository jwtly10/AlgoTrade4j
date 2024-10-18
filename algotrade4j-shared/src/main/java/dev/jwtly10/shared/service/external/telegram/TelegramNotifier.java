package dev.jwtly10.shared.service.external.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.core.external.notifications.Notifier;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TelegramNotifier implements Notifier {

    @Value("${telegram.system.chat.id}")
    private String systemChatId;

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    private String botToken;

    public TelegramNotifier(OkHttpClient client, String botToken) {
        this.client = client;
        this.botToken = botToken;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Send a notification to a Telegram chat
     * If isHtml is true, the message will be sent as HTML
     * <a href="https://core.telegram.org/bots/api#html-style">HTML Supported Docs</a>
     * <p>
     * Do not use this as a wrapper for another method. It handles its own sanitization so you may sanitize the message twice by doing so.
     *
     * @param chatId  The chat id to send the message to
     * @param message The message to send
     * @param isHtml  Whether the message is HTML or not
     */
    @Override
    public void sendNotification(String chatId, String message, boolean isHtml) {
        log.info("Sending notification to chat: {}", chatId);
        send(chatId, sanitize(message), isHtml);
    }

    /**
     * Sending an error notification to a Telegram chat
     *
     * @param chatId  The chat id to send the message to
     * @param message The message to send
     * @param e       The exception that caused the error
     * @param isHtml  Whether the message is HTML or not
     */
    @Override
    public void sendErrorNotification(String chatId, String message, Exception e, boolean isHtml) {
        log.info("Sending error notification to chat: {}", chatId);
        String errorDetails = NotifierUtils.formatError(e);
        send(chatId, String.format("""
                <b>Error:</b> %s
                <pre>%s</pre>""", sanitize(message), sanitize(errorDetails)
        ), isHtml);

    }

    /**
     * Sends internal system notification to the system chat
     * This is useful for sending system-wide notifications
     *
     * @param message the message to send
     * @param isHtml  whether the message is HTML or not
     */
    @Override
    public void sendSysNotification(String message, boolean isHtml) {
        log.info("Sending system notification to chat: {}", systemChatId);
        send(systemChatId, String.format("""
                [SYSTEM]
                %s""", sanitize(message)
        ), isHtml);
    }

    /**
     * Sends internal system error notification to the system chat
     * This is useful for sending system-wide error notifications
     *
     * @param message the message to send
     * @param e       the exception that caused the error
     * @param isHtml  whether the message is HTML or not
     */
    @Override
    public void sendSysErrorNotification(String message, Exception e, boolean isHtml) {
        log.info("Sending system error notification to chat: {}", systemChatId);
        String errorDetails = NotifierUtils.formatError(e);

        send(systemChatId, String.format("""
                [SYSTEM]
                <b>Error:</b> %s
                <pre>%s</pre>""", sanitize(message), sanitize(errorDetails)
        ), isHtml);
    }


    private void send(String chatId, String message, boolean isHtml) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("chat_id", chatId);
        bodyMap.put("parse_mode", isHtml ? "HTML" : "MarkdownV2");

        if (isHtml) {
            bodyMap.put("text", message);
        } else {
            bodyMap.put("text", escapeMarkdown(message));
        }

        try {
            String jsonBody = objectMapper.writeValueAsString(bodyMap);
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
            log.debug("Telegram Req Body: {}", jsonBody);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response);
                }

                log.trace("Notification sent successfully: {}", message);
            }
        } catch (IOException e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }

    private String escapeMarkdown(String text) {
        // Escape special characters for MarkdownV2, but not asterisks, underscores, or backticks
        return text.replaceAll("([\\[\\]()~>#+=|{}.!-])", "\\\\$1");
    }

    private String sanitize(String text) {
        return Jsoup.clean(text, Safelist.none());
    }
}