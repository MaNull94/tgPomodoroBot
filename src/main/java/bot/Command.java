package bot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import markup.Markup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class Command {
    private String name;
    private List<String> args;

    public SendMessage prepareAnswer(Update update) {
        return switch (this.getName()) {
            case "start" -> buildStartCommandMessage(update);
            case "timer" -> buildTimerCommandMessage(update);
            default -> throw new RuntimeException("Неизвестная команда");
        };
    }

    private SendMessage buildTimerCommandMessage(Update update) {
        final String action = this.args.get(0);
        SendMessage message;
        if (action.equals("addInterval")) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            final List<Integer> intervals = List.of(5, 10, 15);
            for (Integer interval : intervals) {
                buttons.add(
                        InlineKeyboardButton.builder()
                                .text(String.valueOf(interval))
                                // TODO шифрование userId
                                .callbackData("timer_addBreak_" + update.getCallbackQuery().getMessage().getChatId() + "_" + interval)
                                .build()
                );
            }
            final InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                    .keyboard(List.of(buttons))
                    .build();

            message = SendMessage.builder()
                    .chatId(update.getCallbackQuery().getMessage().getChatId())
                    .text("Интервал указан, теперь укажи перерыв в минутах")
                    .replyToMessageId(update.getCallbackQuery().getMessage().getMessageId())
                    .replyMarkup(inlineKeyboardMarkup)
                    .build();
        } else if (action.equals("addBreak")) {

            message = SendMessage.builder()
                    .chatId(update.getCallbackQuery().getMessage().getChatId())
                    .text("Таймер запущен")
                    .replyToMessageId(update.getCallbackQuery().getMessage().getMessageId())
                    .replyMarkup(Markup.cancelTimerReplyKeyboardMarkup())
                    .build();
        } else {
            throw new RuntimeException("Invalid timer action");
        }

        return message;
    }

    private SendMessage buildStartCommandMessage(Update update) {
        final Message message = update.getMessage();
        final KeyboardRow keyboardButtons = new KeyboardRow();
        keyboardButtons.add(ReplyButtonName.START_TIMER.getTitle());
        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(
                List.of(keyboardButtons)
        );

        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyToMessageId(message.getMessageId())
                .text("Бот запущен. Жми \"Запустить таймер\"")
                .replyMarkup(replyKeyboardMarkup)
                .build();
    }
}
