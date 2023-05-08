package pet.project.servlet.authentication;

import com.password4j.Hash;
import com.password4j.Password;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import pet.project.dao.SessionDao;
import pet.project.dao.UserDao;
import pet.project.exception.InvalidParameterException;
import pet.project.model.Session;
import pet.project.model.User;
import pet.project.servlet.WeatherTrackerBaseServlet;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@WebServlet("/sign-up")
public class SignUpServlet extends WeatherTrackerBaseServlet {
    private final UserDao userDao = new UserDao();
    private final SessionDao sessionDao = new SessionDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Processing sign-up page");
        templateEngine.process("sign-up", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvalidParameterException {
        String login = req.getParameter("login");
        String password = req.getParameter("password");

        if (login == null || login.isBlank()) {
            throw new InvalidParameterException("Parameter login is invalid");
        }
        if (password == null || password.isBlank()) {
            throw new InvalidParameterException("Parameter login is invalid");
        }

        Hash hash = Password.hash(password).withBcrypt();

        log.info("Saving new user to the database");
        User user = new User(login, hash.getResult());
        userDao.save(user);

        log.info("Creating new session");
        Session session = new Session(UUID.randomUUID(), user, LocalDateTime.now().plusHours(24));
        sessionDao.save(session);

        log.info("Adding cookie with session: " + session.getId() + " to the response");
        Cookie cookie = new Cookie("sessionId", session.getId().toString());
        resp.addCookie(cookie);

        log.info("Registration is successful: redirecting to the home page");
        resp.sendRedirect(req.getContextPath());
    }
}
