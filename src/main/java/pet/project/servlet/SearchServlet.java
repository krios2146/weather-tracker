package pet.project.servlet;

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
import pet.project.model.api.LocationApiResponse;
import pet.project.service.CookieService;
import pet.project.service.WeatherApiService;
import pet.project.util.ThymeleafUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(urlPatterns = "/search")
@Slf4j
public class SearchServlet extends HttpServlet {
    private final SessionDao sessionDao = new SessionDao();
    private final LocationDao locationDao = new LocationDao();
    private final WeatherApiService weatherApiService = new WeatherApiService();
    private final CookieService cookieService = new CookieService();
    private final ITemplateEngine templateEngine = ThymeleafUtil.getTemplateEngine();
    private WebContext context;

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

        String searchQuery = req.getParameter("q");

        if (searchQuery == null || searchQuery.isBlank()) {
            log.warn("Search query is invalid");
            throw new RuntimeException("Search query is invalid");
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
            throw new RuntimeException("Issues with geocoding api call");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        String name = req.getParameter("name");
        if (name == null || name.isBlank()) {
            log.warn("Parameter name is invalid");
            throw new RuntimeException("Parameter name is invalid");
        }

        String latitudeParam = req.getParameter("latitude");
        if (latitudeParam == null || latitudeParam.isBlank()) {
            log.warn("Parameter latitude is invalid");
            throw new RuntimeException("Parameter latitude is invalid");
        }

        String longitudeParam = req.getParameter("longitude");
        if (longitudeParam == null || longitudeParam.isBlank()) {
            log.warn("Parameter longitude is invalid");
            throw new RuntimeException("Parameter longitude is invalid");
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
