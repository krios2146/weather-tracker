package pet.project.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IWebContext;
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
import java.util.UUID;

@WebServlet(name = "RegistrationServlet", urlPatterns = "/registration")
public class RegistrationServlet extends HttpServlet {

    private final ITemplateEngine templateEngine = TemplateEngineUtil.getInstance();
    private IWebContext context;
    private final UserDao userDao = new UserDao();
    private final SessionDao sessionDao = new SessionDao();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (context == null) {
            context = buildWebContext(req, resp);
        }
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        templateEngine.process("registration", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        User user = new User(username, password);
        userDao.save(user);

        Session session = new Session(UUID.randomUUID(), user, LocalDateTime.now().plusHours(24));
        sessionDao.save(session);

        Cookie cookie = new Cookie("sessionId", session.getId().toString());
        resp.addCookie(cookie);
    }

    private IWebContext buildWebContext(HttpServletRequest req, HttpServletResponse resp) {
        ServletContext servletContext = this.getServletContext();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        IServletWebExchange webExchange = application.buildExchange(req, resp);
        return new WebContext(webExchange);
    }
}
