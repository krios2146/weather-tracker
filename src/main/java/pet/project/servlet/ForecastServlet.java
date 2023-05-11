package pet.project.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import pet.project.dao.LocationDao;
import pet.project.dao.SessionDao;
import pet.project.exception.CookieNotFoundException;
import pet.project.exception.InvalidParameterException;
import pet.project.exception.LocationNotFoundException;
import pet.project.exception.SessionExpiredException;
import pet.project.model.Location;
import pet.project.model.Session;
import pet.project.model.api.ForecastApiResponse;
import pet.project.model.dto.WeatherDto;
import pet.project.service.ForecastService;
import pet.project.service.WeatherApiService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@WebServlet("/forecast")
public class ForecastServlet extends WeatherTrackerBaseServlet {
    private final SessionDao sessionDao = new SessionDao();
    private final LocationDao locationDao = new LocationDao();
    private final WeatherApiService weatherApiService = new WeatherApiService();
    private final ForecastService forecastService = new ForecastService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("Finding cookie with session id");
        Cookie[] cookies = req.getCookies();
        Cookie cookie = findCookieByName(cookies, "sessionId")
                .orElseThrow(() -> new CookieNotFoundException("Cookie with session id is not found"));

        UUID sessionId = UUID.fromString(cookie.getValue());

        log.info("Finding session: " + sessionId);
        Session session = sessionDao.findById(sessionId)
                .orElseThrow(() -> new SessionExpiredException("Session: " + sessionId + " has expired"));

        if (isSessionExpired(session)) {
            throw new SessionExpiredException("Session: " + sessionId + " has expired");
        }

        String locationParam = req.getParameter("locationId");

        if (locationParam == null || locationParam.isBlank()) {
            throw new InvalidParameterException("Parameter locationId is invalid");
        }

        Long locationId = Long.parseLong(locationParam);

        log.info("Finding location: " + locationId);
        Location location = locationDao.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException("Location: " + locationId + " is not found"));

        ForecastApiResponse forecastForLocation = weatherApiService.getForecastForLocation(location);

        List<WeatherDto> hourlyForecast = forecastService.getHourlyForecast(forecastForLocation);
        List<WeatherDto> dailyForecast = forecastService.getDailyForecast(forecastForLocation);

        context.setVariable("login", session.getUser().getLogin());
        context.setVariable("hourlyForecast", hourlyForecast);
        context.setVariable("dailyForecast", dailyForecast);

        templateEngine.process("forecast", context, resp.getWriter());
    }
}
