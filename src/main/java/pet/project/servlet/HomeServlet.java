package pet.project.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import pet.project.dao.LocationDao;
import pet.project.dao.SessionDao;
import pet.project.model.Location;
import pet.project.model.Session;
import pet.project.model.User;
import pet.project.model.api.WeatherApiResponse;
import pet.project.model.dto.WeatherDto;
import pet.project.model.dto.enums.TimeOfDay;
import pet.project.model.dto.enums.WeatherCondition;
import pet.project.service.WeatherApiService;

import java.io.IOException;
import java.util.*;

@WebServlet(urlPatterns = "")
@Slf4j
public class HomeServlet extends WeatherTrackerBaseServlet {
    private final SessionDao sessionDao = new SessionDao();
    private final LocationDao locationDao = new LocationDao();
    private final WeatherApiService weatherApiService = new WeatherApiService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Finding cookie with session id");
        Cookie[] cookies = req.getCookies();
        Optional<Cookie> cookieOptional = findCookieByName(cookies, "sessionId");

        if (cookieOptional.isEmpty()) {
            log.info("Cookie is not found: processing empty home page");
            context.clearVariables();
            templateEngine.process("home", context, resp.getWriter());
            return;
        }

        log.info("Finding session from cookie");
        UUID sessionId = UUID.fromString(cookieOptional.get().getValue());
        Optional<Session> sessionOptional = sessionDao.findById(sessionId);

        if (sessionOptional.isEmpty()) {
            log.info("Session has expired: redirecting to the sign-in page");
            resp.sendRedirect(req.getContextPath() + "/sign-in");
            return;
        }

        User user = sessionOptional.get().getUser();
        log.info("Finding user locations");
        List<Location> userLocations = locationDao.findByUser(user);

        log.info("Finding current weather for user locations");
        Map<Location, WeatherDto> locationWeatherMap = new HashMap<>();
        try {
            for (Location location : userLocations) {
                WeatherApiResponse weather = weatherApiService.getWeatherForLocation(location);
                WeatherDto weatherDto = buildWeatherDto(weather);
                locationWeatherMap.put(location, weatherDto);
            }
        } catch (Exception e) {
            log.warn("Issues with weather API call");
            templateEngine.process("error", context);
            return;
        }

        context.setVariable("locationWeatherMap", locationWeatherMap);
        context.setVariable("login", user.getLogin());

        log.info("Processing home page");
        templateEngine.process("home", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Finding cookie with session id");
        Cookie[] cookies = req.getCookies();
        Optional<Cookie> cookieOptional = findCookieByName(cookies, "sessionId");

        if (cookieOptional.isEmpty()) {
            log.warn("Cookie is not found: processing error page");
            templateEngine.process("error", context);
            return;
        }

        log.info("Finding session from cookie");
        UUID sessionId = UUID.fromString(cookieOptional.get().getValue());
        Optional<Session> sessionOptional = sessionDao.findById(sessionId);

        if (sessionOptional.isEmpty()) {
            log.info("Session has expired: redirecting to the sign-in page");
            resp.sendRedirect(req.getContextPath() + "/sign-in");
            return;
        }

        User user = sessionOptional.get().getUser();

        String locationParam = req.getParameter("locationId");

        if (locationParam == null || locationParam.isBlank()) {
            log.warn("Id of a location to delete is not present: processing error page");
            templateEngine.process("error", context);
            return;
        }

        Long locationId = Long.parseLong(locationParam);

        log.info("Finding location");
        Optional<Location> locationOptional = locationDao.findById(locationId);

        if (locationOptional.isEmpty()) {
            log.warn("Location with given id is not found in the database: processing error page");
            templateEngine.process("error", context);
            return;
        }

        Location location = locationOptional.get();

        log.info("Deleting current user from location users");
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
