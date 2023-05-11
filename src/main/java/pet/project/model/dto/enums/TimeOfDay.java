package pet.project.model.dto.enums;

import java.time.LocalDateTime;

public enum TimeOfDay {
    DAY,
    NIGHT,
    UNDEFINED;

    public static TimeOfDay getTimeOfDayForTime(LocalDateTime time) {
        if (time == null) {
            return UNDEFINED;
        }
        int currentTime = time.getHour();
        return currentTime >= 8 && currentTime <= 20 ? DAY : NIGHT;
    }
}
