package pet.project.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import pet.project.service.SessionService;

import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.*;

@WebListener
public class SessionSchedulerContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        SessionService sessionService = new SessionService();

        Calendar calendar = Calendar.getInstance();

        calendar.set(HOUR_OF_DAY, 0);
        calendar.set(MINUTE, 0);
        calendar.set(SECOND, 0);

        Date time = calendar.getTime();

        // Every 24 hours (millis)
        long interval = 1000 * 60 * 60 * 24;

        sessionService.scheduleSessionDeletion(time, interval);
    }
}
