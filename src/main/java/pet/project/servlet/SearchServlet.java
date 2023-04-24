package pet.project.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.IServletWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import pet.project.dao.LocationDao;
import pet.project.dao.SessionDao;
import pet.project.model.Location;
import pet.project.model.Session;
import pet.project.model.User;
import pet.project.model.api.LocationApiResponse;
import pet.project.service.CookieService;
import pet.project.service.WeatherApiService;
import pet.project.util.TemplateEngineUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(urlPatterns = "/search")
public class SearchServlet extends HttpServlet {
    private final WeatherApiService weatherApiService = new WeatherApiService();
    private final CookieService cookieService = new CookieService();
    private final ITemplateEngine templateEngine = TemplateEngineUtil.getInstance();
    private final SessionDao sessionDao = new SessionDao();
    private final LocationDao locationDao = new LocationDao();
    private WebContext context;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (context == null) {
            context = buildWebContext(req, resp);
        }
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
        // TODO: Validation of query
        String searchQuery = req.getParameter("q");

        // TODO: try-catch looks ugly (?)
        try {
            List<LocationApiResponse> foundLocations = weatherApiService.getLocationsByName(searchQuery);
            context.setVariable("login", user.getLogin());
            context.setVariable("foundLocations", foundLocations);
            templateEngine.process("search", context, resp.getWriter());
        } catch (InterruptedException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            templateEngine.process("error", context, resp.getWriter());
            throw new RuntimeException("Issues with geocoding api call");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

        Location location = new Location(
                req.getParameter("name"),
                List.of(user),
                Double.valueOf(req.getParameter("latitude")),
                Double.valueOf(req.getParameter("longitude"))
        );

        if (locationDao.isPresent(location)) {
            Location locationFromDatabase = findSameLocationInDatabase(location);
            List<User> userList = locationFromDatabase.getUsers();
            userList.add(user);
            locationFromDatabase.setUsers(userList);
            locationDao.update(locationFromDatabase);
        } else {
            locationDao.save(location);
        }
        resp.sendRedirect(req.getContextPath());
    }

    private Location findSameLocationInDatabase(Location location) {
        List<Location> similarNameLocations = locationDao.findByName(location.getName());
        Optional<Location> locationOptional = similarNameLocations.stream()
                .filter(l -> {
                    if (!l.getName().equals(location.getName())) return false;
                    if (!l.getLongitude().equals(location.getLongitude())) return false;
                    return l.getLatitude().equals(location.getLatitude());
                })
                .findFirst();
        return locationOptional.get();
    }

    private WebContext buildWebContext(HttpServletRequest req, HttpServletResponse resp) {
        ServletContext servletContext = this.getServletContext();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        IServletWebExchange webExchange = application.buildExchange(req, resp);
        return new WebContext(webExchange);
    }
}
