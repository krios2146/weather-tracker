package pet.project.servlet;

import jakarta.servlet.ServletConfig;
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
import pet.project.model.api.WeatherApiModel;
import pet.project.model.dto.WeatherDto;
import pet.project.service.CookieService;
import pet.project.service.WeatherApiService;
import pet.project.util.TemplateEngineUtil;

import java.io.IOException;
import java.util.*;

@WebServlet(urlPatterns = "")
public class HomeServlet extends HttpServlet {
    private final SessionDao sessionDao = new SessionDao();
    private final LocationDao locationDao = new LocationDao();
    private final WeatherApiService weatherApiService = new WeatherApiService();
    private final CookieService cookieService = new CookieService();
    private final ITemplateEngine templateEngine = TemplateEngineUtil.getInstance();
    private WebContext context;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (context == null) {
            context = buildWebContext(req, resp);
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

        // TODO: `sendError` is a "terminate" method + maybe use Stream API
        Map<Location, WeatherDto> locationWeatherMap = new HashMap<>();
        try {
            for (Location location : userLocations) {
                WeatherApiModel weather = weatherApiService.getWeatherForLocation(location);

                WeatherDto weatherDto = new WeatherDto(
                        weather.getId(),
                        weather.getCurrentState(),
                        weather.getDescription()
                );
                // TODO: ???
                Integer dtoId = weatherDto.getId();
                String firstNumOfId = String.valueOf(dtoId.toString().charAt(0));
                weatherDto.setId(Integer.parseInt(firstNumOfId));

                locationWeatherMap.put(location, weatherDto);
            }
        } catch (InterruptedException e) {
            templateEngine.process("error", context, resp.getWriter());
            throw new RuntimeException("Issues with weather API call", e.getCause());
        }

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

        Long locationId = Long.parseLong(req.getParameter("locationId"));
        Location location = locationDao.findById(locationId).orElseThrow();
        List<User> userList = location.getUsers();
        userList.remove(user);
        location.setUsers(userList);
        locationDao.update(location);
        resp.sendRedirect(req.getContextPath());
    }

    private WebContext buildWebContext(HttpServletRequest req, HttpServletResponse resp) {
        ServletContext servletContext = this.getServletContext();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        IServletWebExchange webExchange = application.buildExchange(req, resp);
        return new WebContext(webExchange);
    }
}
