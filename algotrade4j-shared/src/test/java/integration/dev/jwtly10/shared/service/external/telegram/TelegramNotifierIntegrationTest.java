package integration.dev.jwtly10.shared.service.external.telegram;

import dev.jwtly10.shared.service.external.telegram.TelegramNotifier;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "TELEGRAM_BOT_TOKEN", matches = ".+")
@EnabledIfEnvironmentVariable(named = "TELEGRAM_TEST_CHAT_ID", matches = ".+")
public class TelegramNotifierIntegrationTest {

    private TelegramNotifier telegramNotifier;
    private String testChatId;

    @BeforeEach
    void setUp() {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        testChatId = System.getenv("TELEGRAM_TEST_CHAT_ID");

        assertNotNull(botToken, "TELEGRAM_BOT_TOKEN environment variable must be set");
        assertNotNull(testChatId, "TELEGRAM_TEST_CHAT_ID environment variable must be set");

        telegramNotifier = new TelegramNotifier(new OkHttpClient());
        telegramNotifier.setBotToken(botToken);
    }

    @Test
    void testSendNotificationMarkdown() {
        telegramNotifier.sendNotification(testChatId, """
                TelegramNotifierIntegrationTest::testSendNotificationMarkdown
                *This should be bold*
                _This should be italic_
                """, false);
    }

    @Test
    void testSendNotificationHtml() {
        telegramNotifier.sendNotification(testChatId, """
                TelegramNotifierIntegrationTest::testSendNotificationHtml
                <b>This should be bold</b>
                <i>This should be italic</i>
                """, true);
    }
}