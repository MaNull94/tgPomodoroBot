package markup;

import bot.ReplyButtonName;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

public class Markup {
    public static ReplyKeyboardMarkup getDefaultReplyMarkup() {
        final KeyboardRow keyboardButtons = new KeyboardRow();
        keyboardButtons.add(ReplyButtonName.START_TIMER.getTitle());

        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(List.of(keyboardButtons));
        return replyKeyboardMarkup;
    }


    public static ReplyKeyboardMarkup cancelTimerReplyKeyboardMarkup() {
        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        final KeyboardRow keyboardButtons = new KeyboardRow();
        keyboardButtons.add(ReplyButtonName.STOP_TIMER.getTitle());
        replyKeyboardMarkup.setKeyboard(List.of(keyboardButtons));
        return replyKeyboardMarkup;
    }

}
