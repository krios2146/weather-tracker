package pet.project.servlet;

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
import pet.project.exception.api.WeatherApiCallException;
import pet.project.model.Location;
import pet.project.model.Session;
import pet.project.model.User;
import pet.project.model.api.WeatherApiResponse;
import pet.project.model.dto.WeatherDto;
import pet.project.model.dto.enums.TimeOfDay;
import pet.project.model.dto.enums.WeatherCondition;
import pet.project.service.WeatherApiService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet(urlPatterns = "")
@Slf4j
public class HomeServlet extends WeatherTrackerBaseServlet {
    private final SessionDao sessionDao = new SessionDao();
    private final LocationDao locationDao = new LocationDao();
    private final WeatherApiService weatherApiService = new WeatherApiService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, CookieNotFoundException, SessionExpiredException, WeatherApiCallException {
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

        User user = session.getUser();

        log.info("Finding locations of user: " + user.getId());
        List<Location> userLocations = locationDao.findByUser(user);

        log.info("Finding current weather for user locations");
        Map<Location, WeatherDto> locationWeatherMap = new HashMap<>();

        for (Location location : userLocations) {
            WeatherApiResponse weather = weatherApiService.getWeatherForLocation(location);
            WeatherDto weatherDto = buildWeatherDto(weather);
            locationWeatherMap.put(location, weatherDto);
        }

        context.setVariable("locationWeatherMap", locationWeatherMap);
        context.setVariable("login", user.getLogin());

        log.info("Processing home page");
        templateEngine.process("home", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, CookieNotFoundException, SessionExpiredException, InvalidParameterException, LocationNotFoundException {
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

        User user = session.getUser();

        String locationParam = req.getParameter("locationId");

        if (locationParam == null || locationParam.isBlank()) {
            throw new InvalidParameterException("Parameter locationId is invalid");
        }

        Long locationId = Long.parseLong(locationParam);

        log.info("Finding location: " + locationId);
        Location location = locationDao.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException("Location: " + locationId + " is not found"));

        log.info("Deleting user: " + user.getId() + " from location: " + locationId);
        List<User> users = location.getUsers();
        users.remove(user);
        location.setUsers(users);
        locationDao.update(location);

        log.info("Deleting is successful: refreshing home page");
        resp.sendRedirect(req.getContextPath());
    }

    private static WeatherDto buildWeatherDto(WeatherApiResponse weather) {
        WeatherApiResponse.Weather weatherApiModel = weather.getWeatherList().get(0);
        return WeatherDto.builder()
                .weatherCondition(WeatherCondition.getWeatherConditionForCode(weatherApiModel.getId()))
                .timeOfDay(TimeOfDay.getTimeOfDayForTime(weather.getDate()))
                .description(weatherApiModel.getDescription())
                .temperature(weather.getMain().getTemperature())
                .temperatureFeelsLike(weather.getMain().getTemperatureFeelsLike())
                .temperatureMinimum(weather.getMain().getTemperatureMinimal())
                .temperatureMaximum(weather.getMain().getTemperatureMaximum())
                .humidity(weather.getMain().getHumidity())
                .pressure(weather.getMain().getPressure())
                .windSpeed(weather.getWind().getSpeed())
                .windDirection(weather.getWind().getDeg())
                .windGust(weather.getWind().getGust())
                .cloudiness(weather.getClouds().getCloudiness())
                .date(weather.getDate())
                .sunrise(weather.getSys().getSunriseTime())
                .sunset(weather.getSys().getSunsetTime())
                .build();
    }
}
