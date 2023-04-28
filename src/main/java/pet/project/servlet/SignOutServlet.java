package pet.project.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pet.project.dao.SessionDao;
import pet.project.model.Session;
import pet.project.service.CookieService;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@WebServlet(urlPatterns = "/sign-out")
public class SignOutServlet extends HttpServlet {
    private final SessionDao sessionDao = new SessionDao();
    private final CookieService cookieService = new CookieService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Cookie[] cookies = req.getCookies();
        Optional<Cookie> cookieOptional = cookieService.findCookieByName(cookies, "sessionId");

        if (cookieOptional.isEmpty()) {
            resp.sendRedirect(req.getContextPath());
            return;
        }

        UUID sessionId = UUID.fromString(cookieOptional.get().getValue());
        Optional<Session> sessionOptional = sessionDao.findById(sessionId);

        if (sessionOptional.isEmpty()) {
            resp.sendRedirect(req.getContextPath());
            return;
        }

        sessionDao.delete(sessionOptional.get());

        Cookie cookie = new Cookie("sessionId", null);
        cookie.setMaxAge(0);
        resp.addCookie(cookie);

        resp.sendRedirect(req.getContextPath());
    }
}
