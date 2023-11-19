package bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class PomodoroBot extends TelegramLongPollingBot {
    private final String botName;


    public PomodoroBot(String botName, String botToken) {
        super(botToken);
        this.botName = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        final Long chatId = update.getMessage().getChatId();

        SendMessage message = new SendMessage(); // Create a message object object
        message.setChatId(chatId);
        message.setText(update.getMessage().getText());
        try {
            this.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    @Override
    public String getBotUsername() {
        return this.botName;
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

}
