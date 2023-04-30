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
import pet.project.model.api.LocationApiResponse;
import pet.project.service.WeatherApiService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(urlPatterns = "/search")
@Slf4j
public class SearchServlet extends WeatherTrackerBaseServlet {
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

        String searchQuery = req.getParameter("q");

        if (searchQuery == null || searchQuery.isBlank()) {
            log.warn("Search query is invalid: processing error page");
            templateEngine.process("error", context);
            return;
        }

        try {
            log.info("Calling openweather geocoding API");
            List<LocationApiResponse> foundLocations = weatherApiService.getLocationsByName(searchQuery);

            context.setVariable("login", user.getLogin());
            context.setVariable("foundLocations", foundLocations);

            log.info("Processing search page");
            templateEngine.process("search", context, resp.getWriter());

        } catch (InterruptedException e) {
            log.warn("Issues with geocoding api call");
            templateEngine.process("error", context, resp.getWriter());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        String name = req.getParameter("name");
        if (name == null || name.isBlank()) {
            log.warn("Parameter name is invalid");
            templateEngine.process("error", context);
            return;
        }

        String latitudeParam = req.getParameter("latitude");
        if (latitudeParam == null || latitudeParam.isBlank()) {
            log.warn("Parameter latitude is invalid");
            templateEngine.process("error", context);
            return;
        }

        String longitudeParam = req.getParameter("longitude");
        if (longitudeParam == null || longitudeParam.isBlank()) {
            log.warn("Parameter longitude is invalid");
            templateEngine.process("error", context);
            return;
        }

        Double latitude = Double.valueOf(latitudeParam);
        Double longitude = Double.valueOf(longitudeParam);

        log.info("Finding location by coordinates");
        Optional<Location> locationOptional = locationDao.findByCoordinates(latitude, longitude);

        if (locationOptional.isPresent()) {
            log.info("Location is found: adding current user to location users");
            Location location = locationOptional.get();

            List<User> users = location.getUsers();
            users.add(user);
            location.setUsers(users);

            locationDao.update(location);
        } else {
            log.info("Location is not found: creating new location");
            Location location = new Location(
                    name,
                    List.of(user),
                    latitude,
                    longitude
            );
            locationDao.save(location);
        }

        log.info("Adding location to tracked list is successful: redirecting to the home page");
        resp.sendRedirect(req.getContextPath());
    }
}
