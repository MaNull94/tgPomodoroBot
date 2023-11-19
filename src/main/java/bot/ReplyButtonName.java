package bot;

public enum ReplyButtonName {
    START_TIMER("Запустить таймер"),
    STOP_TIMER("Отменить таймер");

    private final String title;

    ReplyButtonName(String buttonName) {
        this.title = buttonName;
    }

    public String getTitle() {
        return title;
    }
}
