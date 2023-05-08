package pet.project.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import pet.project.exception.CookieNotFoundException;
import pet.project.exception.InvalidParameterException;
import pet.project.exception.LocationNotFoundException;
import pet.project.exception.SessionExpiredException;
import pet.project.exception.api.GeocodingApiCallException;
import pet.project.exception.api.WeatherApiCallException;
import pet.project.exception.authentication.UserExistsException;
import pet.project.exception.authentication.UserNotFoundException;
import pet.project.exception.authentication.WrongPasswordException;
import pet.project.model.Session;
import pet.project.util.ThymeleafUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public abstract class WeatherTrackerBaseServlet extends HttpServlet {
    protected final ITemplateEngine templateEngine = (ITemplateEngine) getServletContext().getAttribute("templateEngine");
    protected WebContext context;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        context = ThymeleafUtil.buildWebContext(req, resp, getServletContext());

        try {
            super.service(req, resp);

        } catch (InvalidParameterException | GeocodingApiCallException | WeatherApiCallException |
                 UserNotFoundException | WrongPasswordException | LocationNotFoundException e) {
            log.warn(e.getMessage());
            context.setVariable("error", e.getMessage());
            templateEngine.process("error", context, resp.getWriter());

        } catch (SessionExpiredException | UserExistsException e) {
            log.warn(e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/sign-in");

        } catch (CookieNotFoundException e) {
            log.warn(e.getMessage());
            context.clearVariables();
            templateEngine.process("home", context, resp.getWriter());

        } catch (Exception e) {
            log.warn(e.getMessage());
            templateEngine.process("error", context, resp.getWriter());
        }
    }

    protected static Optional<Cookie> findCookieByName(Cookie[] cookies, String cookieName) {
        if (cookies == null || cookies.length < 1) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst();
    }

    protected static boolean isSessionExpired(Session session) {
        LocalDateTime expiresAt = session.getExpiresAt();
        LocalDateTime currentTime = LocalDateTime.now();

        return currentTime.isAfter(expiresAt);
    }
}
