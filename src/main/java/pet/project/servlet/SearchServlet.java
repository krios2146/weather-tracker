package pet.project.servlet;

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
import pet.project.model.api.LocationApiResponse;
import pet.project.service.CookieService;
import pet.project.service.WeatherApiService;
import pet.project.util.ThymeleafUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(urlPatterns = "/search")
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
            resp.sendRedirect(req.getContextPath() + "/sign-in");
            return;
        }

        User user = sessionOptional.get().getUser();

        String searchQuery = req.getParameter("q");

        if (searchQuery == null || searchQuery.isBlank()) {
            throw new RuntimeException("Search query is invalid");
        }

        try {
            List<LocationApiResponse> foundLocations = weatherApiService.getLocationsByName(searchQuery);
            context.setVariable("login", user.getLogin());
            context.setVariable("foundLocations", foundLocations);
            templateEngine.process("search", context, resp.getWriter());

        } catch (InterruptedException e) {
            templateEngine.process("error", context, resp.getWriter());
            throw new RuntimeException("Issues with geocoding api call");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
            resp.sendRedirect(req.getContextPath() + "/sign-in");
            return;
        }

        User user = sessionOptional.get().getUser();

        String name = req.getParameter("name");
        if (name == null || name.isBlank()) {
            throw new RuntimeException("Parameter name is invalid");
        }

        String latitudeParam = req.getParameter("latitude");
        if (latitudeParam == null || latitudeParam.isBlank()) {
            throw new RuntimeException("Parameter latitude is invalid");
        }

        String longitudeParam = req.getParameter("longitude");
        if (longitudeParam == null || longitudeParam.isBlank()) {
            throw new RuntimeException("Parameter longitude is invalid");
        }

        Double latitude = Double.valueOf(latitudeParam);
        Double longitude = Double.valueOf(longitudeParam);

        Optional<Location> locationOptional = locationDao.findByCoordinates(latitude, longitude);

        if (locationOptional.isPresent()) {
            Location location = locationOptional.get();

            List<User> users = location.getUsers();
            users.add(user);
            location.setUsers(users);

            locationDao.update(location);
        } else {
            Location location = new Location(
                    name,
                    List.of(user),
                    latitude,
                    longitude
            );
            locationDao.save(location);
        }

        resp.sendRedirect(req.getContextPath());
    }
}
