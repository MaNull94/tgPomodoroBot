package bot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import markup.Markup;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pomodoro.PomodoroTimer;

import java.time.Duration;
import java.util.*;

@Getter
@Slf4j
public class PomodoroBot extends TelegramLongPollingBot {
    private final String botName;
    private final String COMMAND_PREFIX = "/";
    private final Map<Long, PomodoroTimer> timers = new HashMap<>();
    private Set<String> replyButtonNames;


    public PomodoroBot(String botName, String botToken) {
        super(botToken);
        this.botName = botName;
        setup();
    }

    private void setup() {
        final ReplyButtonName[] values = ReplyButtonName.values();
        Set<String> replyButtonNames = new HashSet<>();
        for (ReplyButtonName value : values) {
            replyButtonNames.add(value.getTitle());
        }
        this.replyButtonNames = replyButtonNames;
    }

    @Override
    public void onUpdateReceived(Update update) {
        final Message message = update.getMessage();
        try {
            if (update.getCallbackQuery() != null || (
                    message != null && isCommand(message.getText())
            )){
                if (update.getCallbackQuery() != null) {
                    log.info("пришла команда из колбэка");
                    handleUpdateWithCallback(update);
                } else {
                    log.info("пришла команда из сообщения");
                    handleCommand(update);
                }
            } else if (message != null) {
                log.info("пришел текст");
                handleUpdateWithText(update);
            } else {
                throw new RuntimeException("НИПАНЯТНА");
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private void handleUpdateWithCallback(Update update) throws TelegramApiException {
        final CallbackQuery callbackQuery = update.getCallbackQuery();
        Command command = getCommandFromCallbackData(callbackQuery.getData());
        final boolean isExecutedSuccessful = executeCallbackCommand(command);
        log.debug("timers -> {}", this.timers);
        log.debug("AGAGAGA");
        if (!isExecutedSuccessful) {
            replyToInvalidUpdate(update);
            return;
        }
        final SendMessage answer = command.prepareAnswer(update);
        execute(answer);
    }

    private void replyToInvalidUpdate(Update update) throws TelegramApiException {
        Message message = update.getMessage() == null ? update.getCallbackQuery().getMessage() : update.getMessage();

        final SendMessage sendMessage = SendMessage.builder()
                .chatId(message.getChatId())
                .text("Упс. Что-то пошло не так. Го всё заново")
                .replyMarkup(Markup.getDefaultReplyMarkup())
                .build();
        execute(sendMessage);
    }

    private boolean executeCallbackCommand(Command command) {
        return switch (command.getName()) {
            case "timer" -> executeTimerCommand(command);
            default -> throw new RuntimeException("Исполнить команду с коллбэком не получится");
        };
    }

    private boolean executeTimerCommand(Command command) {
        final String action = command.getArgs().get(0);
        return switch (action) {
            case "addInterval" -> {
                // TODO перевести в метод. чтобы потом можно было зашифровать
                final String userId = command.getArgs().get(1);
                final String interval = command.getArgs().get(2);
                this.setTimer(userId, interval);
                yield true;
            }
            case "addBreak" -> {
                final String userId = command.getArgs().get(1);
                final String _break = command.getArgs().get(2);
                yield this.setBreak(userId, _break);
            }
            default -> false;
        };
    }

    private boolean setBreak(String userId, String _break) {
        final PomodoroTimer pomodoroTimer = this.timers.get(Long.parseLong(userId));
        if (pomodoroTimer == null) {
            return false;
        }

        pomodoroTimer.set_break(Duration.ofMinutes(Long.parseLong(_break)));
        return true;
    }

    private void setTimer(String userId, String interval) {
        final PomodoroTimer pomodoroTimer = this.timers.getOrDefault(Long.parseLong(userId), new PomodoroTimer());
        pomodoroTimer.setFocus(Duration.ofMinutes(Long.parseLong(interval)));
        this.timers.put(Long.parseLong(userId), pomodoroTimer);
    }

    private Command getCommandFromCallbackData(String data) {
        final String[] split = data.split("_");
        List<String> args = new ArrayList<>();
        for (int i = 1; i < split.length; i++) {
            args.add(split[i]);
        }
        final String commandName = split[0];

        return new Command(commandName, args);
    }

    private void handleCommand(Update update) throws TelegramApiException {
        Command command = parseCommand(update.getMessage().getText());
        final List<String> availableCommands = List.of("start");
        if (!availableCommands.contains(command.getName())) {
            sendListOfCommands(update);
            return;
        }
        SendMessage answer = command.prepareAnswer(update);
        execute(answer);
    }


    private void sendListOfCommands(Update update) throws TelegramApiException {
        reply(update, getListOfCommands());
    }

    private void reply(Update update, String messageText) throws TelegramApiException {
        final SendMessage sendMessage = new SendMessage();
        final Long chatId = update.getMessage().getChatId();
        final Integer messageId = update.getMessage().getMessageId();
        sendMessage.setText(messageText);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(messageId);
        execute(sendMessage);
    }

    private String getListOfCommands() {
        return "Список команд";
    }

    private Command parseCommand(String text) {
        final String[] split = text.split(" ");
        List<String> args = new ArrayList<>();
        for (int i = 1; i < split.length; i++) {
            args.add(split[i]);
        }
        final String commandName = split[0].substring(COMMAND_PREFIX.length());

        return new Command(commandName, args);
    }

    private boolean isCommand(String messageText) {
        return messageText.startsWith(COMMAND_PREFIX);
    }

    private void handleUpdateWithText(Update update) throws TelegramApiException {
        final String messageText = update.getMessage().getText();
        if (this.replyButtonNames.contains(messageText)) {
            handleReplyButton(update, messageText);
            return;
        }
        log.info("Пришел текст когда не ждали");
        log.debug("update --> {}", update);
    }

    private void handleReplyButton(Update update, String messageText) throws TelegramApiException {
        switch (messageText) {
            case "Запустить таймер" -> setupTimer(update);
            case "Отменить таймер" -> stopTimer(update);
            default -> throw new RuntimeException("Неизвестная кнопка");
        }

    }

    private void stopTimer(Update update) throws TelegramApiException {
        //TODO должен быть шифрован
        final Long chatId = update.getMessage().getChatId();
        final PomodoroTimer removedTimer = this.timers.remove(chatId);
        if (removedTimer == null) {
            replyToInvalidUpdate(update);
            return;
        }

        final SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text("Таймер остановлен")
                .replyMarkup(Markup.getDefaultReplyMarkup()).build();
        execute(sendMessage);
    }

    private void setupTimer(Update update) throws TelegramApiException {
        final Long userId = getUserId(update);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        final List<Integer> intervals = List.of(15, 30, 60, 90);
        for (Integer interval : intervals) {
            buttons.add(
                    InlineKeyboardButton.builder()
                            .text(String.valueOf(interval))
                            .callbackData("timer_addInterval_" + userId + "_" + interval)
                            .build()
            );
        }
        final InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(List.of(buttons))
                .build();
        final SendMessage sendMessage = SendMessage.builder()
                .text("Шаг 1. Укажи интервал работы (в минутах)")
                .replyMarkup(inlineKeyboardMarkup)
                .chatId(userId)
                .build();
        execute(sendMessage);
    }

    private Long getUserId(Update update) {
        return update.getMessage().getChatId();
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
