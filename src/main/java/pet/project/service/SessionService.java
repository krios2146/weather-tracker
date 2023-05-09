package pet.project.service;

import pet.project.dao.SessionDao;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static java.util.Calendar.*;

public class SessionService {
    private final SessionDao sessionDao = new SessionDao();

    public void scheduleSessionDeletion(Date time, long interval) {
        if (time == null) {
            Calendar calendar = Calendar.getInstance();

            calendar.set(HOUR_OF_DAY, 0);
            calendar.set(MINUTE, 0);
            calendar.set(SECOND, 0);

            time = calendar.getTime();
        }

        if (interval <= 0) {
            interval = 1000 * 60 * 60 * 24;
        }

        SessionDeletionTask sessionDeletionTask = new SessionDeletionTask();
        Timer timer = new Timer();

        timer.schedule(sessionDeletionTask, time, interval);
    }

    private class SessionDeletionTask extends TimerTask {
        @Override
        public void run() {
            sessionDao.deleteSessionsExpiredAtTime(LocalDateTime.now());
        }
    }
}
