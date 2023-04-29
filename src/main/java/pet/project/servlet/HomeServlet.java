package pet.project.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import pet.project.dao.LocationDao;
import pet.project.dao.SessionDao;
import pet.project.model.Location;
import pet.project.model.Session;
import pet.project.model.User;
import pet.project.model.api.WeatherApiResponse;
import pet.project.model.dto.WeatherDto;
import pet.project.model.dto.enums.TimeOfDay;
import pet.project.model.dto.enums.WeatherCondition;
import pet.project.service.CookieService;
import pet.project.service.WeatherApiService;
import pet.project.util.ThymeleafUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = "")
@Slf4j
public class HomeServlet extends HttpServlet {
    private final SessionDao sessionDao = new SessionDao();
    private final LocationDao locationDao = new LocationDao();
    private final WeatherApiService weatherApiService = new WeatherApiService();
    private final CookieService cookieService = new CookieService();
    private final ITemplateEngine templateEngine = ThymeleafUtil.getTemplateEngine();
    private WebContext context;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (context == null) {
            log.info("Context is null: building");
            context = ThymeleafUtil.buildWebContext(req, resp, getServletContext());
        }
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Finding cookie with session id");
        Cookie[] cookies = req.getCookies();
        Optional<Cookie> cookieOptional = cookieService.findCookieByName(cookies, "sessionId");

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
        Map<Location, WeatherDto> locationWeatherMap = userLocations.stream()
                .collect(Collectors.toMap(
                        location -> location,
                        this::getWeather
                ));

        context.setVariable("locationWeatherMap", locationWeatherMap);
        context.setVariable("login", user.getLogin());

        log.info("Processing home page");
        templateEngine.process("home", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Finding cookie with session id");
        Cookie[] cookies = req.getCookies();
        Optional<Cookie> cookieOptional = cookieService.findCookieByName(cookies, "sessionId");

        if (cookieOptional.isEmpty()) {
            log.warn("Cookie is not found");
            throw new RuntimeException("Cookie is not found");
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
            log.warn("Id of a location to delete is empty");
            throw new RuntimeException("Id of a location to delete is empty");
        }

        Long locationId = Long.parseLong(locationParam);

        log.info("Finding location");
        Optional<Location> locationOptional = locationDao.findById(locationId);

        if (locationOptional.isEmpty()) {
            log.warn("Location with given id is not found in the database");
            throw new RuntimeException("Location with given id is not found in the database");
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

    private WeatherDto getWeather(Location location) {
        try {
            log.info("Calling openweather API for a weather for the specific location");
            WeatherApiResponse weather = weatherApiService.getWeatherForLocation(location);
            return buildWeatherDto(weather);
        } catch (Exception e) {
            log.warn("Issues with weather API call");
            throw new RuntimeException("Issues with weather API call", e);
        }
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
