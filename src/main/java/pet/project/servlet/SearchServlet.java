package pet.project.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.IServletWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import pet.project.WeatherApiService;
import pet.project.model.api.ApiLocation;
import pet.project.util.TemplateEngineUtil;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = "/search")
public class SearchServlet extends HttpServlet {

    private final WeatherApiService weatherApiService = new WeatherApiService();
    private final ITemplateEngine templateEngine = TemplateEngineUtil.getInstance();
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
        // TODO: Validation of query
        // TODO: Validation of user (authenticated or not)
        String searchQuery = req.getParameter("q");

        // TODO: try-catch looks ugly (?)
        try {
            List<ApiLocation> foundLocations = weatherApiService.getLocationsByName(searchQuery);
            context.setVariable("foundLocations", foundLocations);
            templateEngine.process("search", context, resp.getWriter());
        } catch (InterruptedException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            templateEngine.process("error", context, resp.getWriter());
            throw new RuntimeException("Issues with geocoding api call");
        }
    }

    private WebContext buildWebContext(HttpServletRequest req, HttpServletResponse resp) {
        ServletContext servletContext = this.getServletContext();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        IServletWebExchange webExchange = application.buildExchange(req, resp);
        return new WebContext(webExchange);
    }
}
