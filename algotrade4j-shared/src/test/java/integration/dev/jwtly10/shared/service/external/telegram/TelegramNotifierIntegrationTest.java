package integration.dev.jwtly10.shared.service.external.telegram;

import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Trade;
import dev.jwtly10.shared.service.external.telegram.TelegramNotifier;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.ZonedDateTime;

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

        telegramNotifier = new TelegramNotifier(new OkHttpClient(), botToken);
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

    @Test
    void testMessageFormatting() {
        Trade trade = new Trade(48, Instrument.NAS100USD, 38, ZonedDateTime.now(), new Number("3823.39"),
                new Number("3823.39"),
                new Number("3823.39"),
                true);
        trade.setProfit(39388.23);
        trade.setClosePrice(new Number("3823.39"));
        trade.setCloseTime(ZonedDateTime.now());

        var strategyId = "TestStrategy";


        String direction = trade.isLong() ? "Long" : "Short";
        String profitLossText = trade.getProfit() >= 0 ? "Profit" : "Loss";
        String emoji = trade.getProfit() >= 0 ? "✅" : "❌";
        String sign = trade.getProfit() >= 0 ? "+" : "-";
        String message = String.format(
                "Trade Closed: %s %s$%.2f %s · %s\n" +
                        "<b>Trade Details</b>\n" +
                        "🔹 ID: #%d\n" +
                        "📍 Dir: %s \n" +
                        "🌐 Symbol: %s \n" +
                        "📈 Entry: $%.2f\n" +
                        "📉 Close: $%.2f",
                emoji,
                sign,
                Math.abs(trade.getProfit()),
                profitLossText,
                strategyId,

                trade.getId(),
                direction,
                trade.getInstrument(),
                trade.getEntryPrice().doubleValue(),
                trade.getClosePrice().doubleValue()
        );

        var sysString = String.format("""
            [SYSTEM] 🚨
            %s""", message);

        telegramNotifier.sendNotification(testChatId, sysString, true);
    }
}