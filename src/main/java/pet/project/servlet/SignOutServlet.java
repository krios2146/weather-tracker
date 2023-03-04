package pet.project.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pet.project.dao.SessionDao;
import pet.project.model.Session;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@WebServlet(urlPatterns = "/sign-out")
public class SignOutServlet extends HttpServlet {

    private final SessionDao sessionDao = new SessionDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Cookie[] cookies = req.getCookies();
        Optional<Cookie> sessionIdCookie = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("sessionId"))
                .findFirst();
        String sessionId = sessionIdCookie.get().getValue();
        Optional<Session> session = sessionDao.findById(UUID.fromString(sessionId));
        sessionDao.delete(session.get());

        Cookie cookie = new Cookie("sessionId", null);
        cookie.setMaxAge(0);
        resp.addCookie(cookie);

        resp.sendRedirect(req.getContextPath());
    }
}
