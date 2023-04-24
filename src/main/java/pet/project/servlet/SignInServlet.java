package pet.project.servlet;

import com.password4j.Password;
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
import pet.project.dao.SessionDao;
import pet.project.dao.UserDao;
import pet.project.model.Session;
import pet.project.model.User;
import pet.project.util.TemplateEngineUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@WebServlet(urlPatterns = "/sign-in")
public class SignInServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();
    private final SessionDao sessionDao = new SessionDao();
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
        templateEngine.process("sign-in", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String login = req.getParameter("login");
        String password = req.getParameter("password");

        Optional<User> optionalUser = userDao.findByLogin(login);
        User user = optionalUser.orElseThrow();

        String passwordFromDb = user.getPassword();

        if (!Password.check(password, passwordFromDb).withBcrypt()) {
            throw new RuntimeException("Authorization exception: wrong password");
        }

        Session session = new Session(UUID.randomUUID(), user, LocalDateTime.now().plusHours(24));
        sessionDao.save(session);

        Cookie cookie = new Cookie("sessionId", session.getId().toString());
        resp.addCookie(cookie);

        resp.sendRedirect(req.getContextPath());
    }

    private WebContext buildWebContext(HttpServletRequest req, HttpServletResponse resp) {
        ServletContext servletContext = this.getServletContext();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        IServletWebExchange webExchange = application.buildExchange(req, resp);
        return new WebContext(webExchange);
    }
}
