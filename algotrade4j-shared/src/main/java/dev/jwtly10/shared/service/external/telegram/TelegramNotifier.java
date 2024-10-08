package dev.jwtly10.shared.service.external.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.core.external.notifications.Notifier;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TelegramNotifier implements Notifier {

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
     *
     * @param chatId  The chat id to send the message to
     * @param message The message to send
     * @param isHtml  Whether the message is HTML or not
     */
    @Override
    public void sendNotification(String chatId, String message, boolean isHtml) {
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
            log.debug("Sending notification: {}", jsonBody);
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

    @Override
    public void sendErrorNotification(String chatId, String message, Exception e, boolean isHtml) {
        String errorDetails = NotifierUtils.formatError(e);
        sendNotification(chatId, String.format("""
                <b>Error:</b> %s
                <pre>%s</pre>""", message, errorDetails
        ), isHtml);

    }

    private String escapeMarkdown(String text) {
        // Escape special characters for MarkdownV2, but not asterisks, underscores, or backticks
        return text.replaceAll("([\\[\\]()~>#+=|{}.!-])", "\\\\$1");
    }
}