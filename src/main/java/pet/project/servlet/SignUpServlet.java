package pet.project.servlet;

import com.password4j.Hash;
import com.password4j.Password;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IWebContext;
import pet.project.dao.SessionDao;
import pet.project.dao.UserDao;
import pet.project.model.Session;
import pet.project.model.User;
import pet.project.util.ThymeleafUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@WebServlet(urlPatterns = "/sign-up")
public class SignUpServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();
    private final SessionDao sessionDao = new SessionDao();
    private final ITemplateEngine templateEngine = ThymeleafUtil.getTemplateEngine();
    private IWebContext context;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (context == null) {
            context = ThymeleafUtil.buildWebContext(req, resp, getServletContext());
        }
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        templateEngine.process("sign-up", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String login = req.getParameter("login");
        String password = req.getParameter("password");

        if (login == null || login.isBlank()) {
            throw new RuntimeException("Login is invalid");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("Password is invalid");
        }

        Hash hash = Password.hash(password).withBcrypt();

        Optional<User> userOptional = userDao.findByLogin(login);

        if (userOptional.isPresent()) {
            resp.sendRedirect(req.getContextPath() + "/sign-in");
            throw new RuntimeException("User already exists in the database");
        }

        User user = new User(login, hash.getResult());
        userDao.save(user);

        Session session = new Session(UUID.randomUUID(), user, LocalDateTime.now().plusHours(24));
        sessionDao.save(session);

        Cookie cookie = new Cookie("sessionId", session.getId().toString());
        resp.addCookie(cookie);

        resp.sendRedirect(req.getContextPath());
    }
}
