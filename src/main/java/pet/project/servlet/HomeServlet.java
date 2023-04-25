package pet.project.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import pet.project.dao.LocationDao;
import pet.project.dao.SessionDao;
import pet.project.model.Location;
import pet.project.model.Session;
import pet.project.model.User;
import pet.project.model.api.WeatherApiModel;
import pet.project.model.api.WeatherApiResponse;
import pet.project.model.dto.WeatherDto;
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
            context = ThymeleafUtil.buildWebContext(req, resp, getServletContext());
        }
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Cookie[] cookies = req.getCookies();
        Optional<Cookie> cookieOptional = cookieService.findCookieByName(cookies, "sessionId");

        if (cookieOptional.isEmpty()) {
            context.clearVariables();
            templateEngine.process("home", context, resp.getWriter());
            return;
        }

        UUID sessionId = UUID.fromString(cookieOptional.get().getValue());
        Optional<Session> sessionOptional = sessionDao.findById(sessionId);

        if (sessionOptional.isEmpty()) {
            resp.sendRedirect("/sign-in");
            return;
        }

        User user = sessionOptional.get().getUser();
        List<Location> userLocations = locationDao.findByUser(user);

        Map<Location, WeatherDto> locationWeatherMap = userLocations.stream()
                .collect(Collectors.toMap(
                        location -> location,
                        this::getWeather
                ));

        context.setVariable("locationWeatherMap", locationWeatherMap);
        context.setVariable("login", user.getLogin());
        templateEngine.process("home", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Cookie[] cookies = req.getCookies();
        Optional<Cookie> cookieOptional = cookieService.findCookieByName(cookies, "sessionId");

        if (cookieOptional.isEmpty()) {
            throw new RuntimeException("Cookie is not found");
        }

        UUID sessionId = UUID.fromString(cookieOptional.get().getValue());
        Optional<Session> sessionOptional = sessionDao.findById(sessionId);

        if (sessionOptional.isEmpty()) {
            resp.sendRedirect("/sign-in");
            return;
        }

        User user = sessionOptional.get().getUser();

        String locationParam = req.getParameter("locationId");

        if (locationParam == null || locationParam.isBlank()) {
            throw new RuntimeException("Id of a location to delete is empty");
        }

        Long locationId = Long.parseLong(locationParam);

        Optional<Location> locationOptional = locationDao.findById(locationId);

        if (locationOptional.isEmpty()) {
            throw new RuntimeException("Location with given id is not found in the database");
        }

        Location location = locationOptional.get();

        List<User> users = location.getUsers();
        users.remove(user);
        location.setUsers(users);
        locationDao.update(location);

        resp.sendRedirect(req.getContextPath());
    }

    private WeatherDto getWeather(Location location) {
        try {
            WeatherApiResponse weather = weatherApiService.getWeatherForLocation(location);
            return buildWeatherDto(weather);
        } catch (Exception e) {
            throw new RuntimeException("Issues with weather API call", e);
        }
    }

    private static WeatherDto buildWeatherDto(WeatherApiResponse weather) {
        WeatherApiModel weatherApiModel = weather.getWeatherList().get(0);
        return WeatherDto.builder()
                .id(getFirstNumber(weatherApiModel.getId()))
                .currentState(weatherApiModel.getCurrentState())
                .description(weatherApiModel.getDescription())
                .temperature(weather.getTemperature())
                .temperatureFeelsLike(weather.getTemperatureFeelsLike())
                .temperatureMinimum(weather.getTemperatureMinimal())
                .temperatureMaximum(weather.getTemperatureMaximum())
                .humidity(weather.getHumidity())
                .pressure(weather.getPressure())
                .windSpeed(weather.getWind().getSpeed())
                .windDirection(weather.getWind().getDeg())
                .windGust(weather.getWind().getGust())
                .cloudiness(weather.getClouds().getCloudiness())
                .date(weather.getDate())
                .sunrise(weather.getSunriseTime())
                .sunset(weather.getSunsetTime())
                .build();
    }

    private static Integer getFirstNumber(Integer number) {
        char firstNumberChar = number.toString().charAt(0);
        String firstNumberString = String.valueOf(firstNumberChar);
        return Integer.parseInt(firstNumberString);
    }
}
