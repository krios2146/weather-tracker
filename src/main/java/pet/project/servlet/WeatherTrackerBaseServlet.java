package pet.project.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IWebContext;
import pet.project.model.Session;
import pet.project.util.ThymeleafUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public abstract class WeatherTrackerBaseServlet extends HttpServlet {
    private final ITemplateEngine templateEngine = ThymeleafUtil.getTemplateEngine();
    private IWebContext context;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (context == null) {
            log.debug("Context is null: building");
            ThymeleafUtil.buildWebContext(req, resp, getServletContext());
        }

        try {
            super.service(req, resp);
        } catch (Exception e) {
            templateEngine.process("error", context);
        }
    }

    private static Optional<Cookie> findCookieByName(Cookie[] cookies, String cookieName) throws NotFoundException {
        if (cookies == null || cookies.length < 1) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst();
    }

    private static boolean isSessionExpired(Session session) {
        LocalDateTime expiresAt = session.getExpiresAt();
        LocalDateTime currentTime = LocalDateTime.now();

        return currentTime.isAfter(expiresAt);
    }
}
