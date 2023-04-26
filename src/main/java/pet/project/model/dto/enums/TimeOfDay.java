package pet.project.model.dto.enums;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static java.util.Calendar.HOUR_OF_DAY;

public enum TimeOfDay {
    DAY,
    NIGHT,
    UNDEFINED;

    public static TimeOfDay getTimeOfDayForTime(String time) {
        SimpleDateFormat formatter = new SimpleDateFormat();
        Calendar calendar = new GregorianCalendar();

        try {
            Date date = formatter.parse(time);
            calendar.setTime(date);
            int currentTime = calendar.get(HOUR_OF_DAY);

            return currentTime >= 8 && currentTime <= 20 ? DAY : NIGHT;
        } catch (ParseException e) {
            return UNDEFINED;
        }
    }
}
