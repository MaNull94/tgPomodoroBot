package pomodoro;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;

@Getter @Setter
@NoArgsConstructor
public class PomodoroTimer {
    private Duration focus;
    private Duration _break;
}
