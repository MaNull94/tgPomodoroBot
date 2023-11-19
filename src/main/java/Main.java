import bot.PomodoroBot;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
public class Main {
    public static void main(String[] args) {
        final PomodoroBot pomodoroBot = new PomodoroBot(
                "pomodoro4j_bot",
                "6611525169:AAHMZR9Pej314aJly_FQvv1rVWtFbfgJsWs"
        );

        try {
            final TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(pomodoroBot);
            log.info("start");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
