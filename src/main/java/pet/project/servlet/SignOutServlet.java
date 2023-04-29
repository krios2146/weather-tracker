package pet.project.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import pet.project.dao.SessionDao;
import pet.project.model.Session;
import pet.project.service.CookieService;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@WebServlet(urlPatterns = "/sign-out")
@Slf4j
public class SignOutServlet extends HttpServlet {
    private final SessionDao sessionDao = new SessionDao();
    private final CookieService cookieService = new CookieService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Finding cookie with session id");
        Cookie[] cookies = req.getCookies();
        Optional<Cookie> cookieOptional = cookieService.findCookieByName(cookies, "sessionId");

        if (cookieOptional.isEmpty()) {
            log.info("Cookie is not found: redirecting to the home page");
            resp.sendRedirect(req.getContextPath());
            return;
        }

        log.info("Finding session from cookie");
        UUID sessionId = UUID.fromString(cookieOptional.get().getValue());
        Optional<Session> sessionOptional = sessionDao.findById(sessionId);

        if (sessionOptional.isEmpty()) {
            log.info("Session has expired: redirecting to the home page");
            resp.sendRedirect(req.getContextPath());
            return;
        }

        log.info("Deleting session from database");
        sessionDao.delete(sessionOptional.get());

        log.info("Deleting cookie from response");
        Cookie cookie = new Cookie("sessionId", null);
        cookie.setMaxAge(0);
        resp.addCookie(cookie);

        log.info("Sign-out is successful: redirecting to the home page");
        resp.sendRedirect(req.getContextPath());
    }
}
