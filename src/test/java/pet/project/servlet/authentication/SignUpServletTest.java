package pet.project.servlet.authentication;

import com.password4j.Password;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import pet.project.dao.SessionDao;
import pet.project.dao.UserDao;
import pet.project.exception.InvalidParameterException;
import pet.project.model.Session;
import pet.project.model.User;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignUpServletTest {
    @Mock
    private UserDao userDao;
    @Mock
    private SessionDao sessionDao;

    @Mock
    private ITemplateEngine templateEngine;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private SignUpServlet signUpServlet;

    @BeforeEach
    public void setUp() throws Exception {
        signUpServlet = new SignUpServlet();

        Field sessionDaoField = signUpServlet.getClass().getDeclaredField("sessionDao");
        sessionDaoField.setAccessible(true);
        sessionDaoField.set(signUpServlet, sessionDao);

        Field locationDaoField = signUpServlet.getClass().getDeclaredField("userDao");
        locationDaoField.setAccessible(true);
        locationDaoField.set(signUpServlet, userDao);

        Field templateField = signUpServlet.getClass().getSuperclass().getDeclaredField("templateEngine");
        templateField.setAccessible(true);
        templateField.set(signUpServlet, templateEngine);
    }

    @Test
    public void doGet_shouldProcessSignInPage() throws Exception {
        signUpServlet.doGet(request, response);

        verify(templateEngine, atMostOnce()).process(eq("sign-up"), any(WebContext.class), any());
    }

    @Test
    public void doPost_loginParamIsNull_shouldThrowInvalidParameterException() {
        lenient().when(request.getParameter("password")).thenReturn("password");

        Assertions.assertThrows(
                InvalidParameterException.class,
                () -> signUpServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_loginParamIsBlank_shouldThrowInvalidParameterException() {
        lenient().when(request.getParameter("password")).thenReturn("password");
        lenient().when(request.getParameter("login")).thenReturn(" ");

        Assertions.assertThrows(
                InvalidParameterException.class,
                () -> signUpServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_passwordParamIsNull_shouldThrowInvalidParameterException() {
        lenient().when(request.getParameter("login")).thenReturn("login");

        Assertions.assertThrows(
                InvalidParameterException.class,
                () -> signUpServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_passwordParamIsBlank_shouldThrowInvalidParameterException() {
        lenient().when(request.getParameter("login")).thenReturn("login");
        lenient().when(request.getParameter("password")).thenReturn(" ");

        Assertions.assertThrows(
                InvalidParameterException.class,
                () -> signUpServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_registrationSuccessful_shouldCreateUser() throws Exception {
        when(request.getParameter("login")).thenReturn("login");
        when(request.getParameter("password")).thenReturn("password");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        signUpServlet.doPost(request, response);

        verify(userDao, atMostOnce()).save(captor.capture());
        assertTrue(Password.check("password", captor.getValue().getPassword()).withBcrypt());
        assertEquals("login", captor.getValue().getLogin());
    }

    @Test
    public void doPost_registrationSuccessful_shouldCreateSession() throws Exception {
        User user = new User("login", Password.hash("password").withBcrypt().getResult());
        Session expectedSession = new Session(user, LocalDateTime.now().plusHours(24));
        when(request.getParameter("login")).thenReturn("login");
        when(request.getParameter("password")).thenReturn("password");
        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);

        signUpServlet.doPost(request, response);

        System.out.println(Password.hash("password").withBcrypt().getResult() + "password-1");
        System.out.println(Password.hash("password").withBcrypt().getResult() + "password-2");
        System.out.println(Password.hash("password").withBcrypt().getResult() + "password-3");
        verify(sessionDao, atMostOnce()).save(captor.capture());
        assertTrue(Password.check("password", captor.getValue().getUser().getPassword()).withBcrypt());
        assertEquals(captor.getValue().getUser().getLogin(), user.getLogin());
        assertEquals(expectedSession.getExpiresAt().withNano(0), captor.getValue().getExpiresAt().withNano(0));
    }

    @Test
    public void doPost_registrationSuccessful_shouldAddCookie() throws Exception {
        when(request.getParameter("login")).thenReturn("login");
        when(request.getParameter("password")).thenReturn("password");
        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        signUpServlet.doPost(request, response);

        verify(sessionDao, atMostOnce()).save(sessionCaptor.capture());
        verify(response, atMostOnce()).addCookie(cookieCaptor.capture());
        String savedSessionId = sessionCaptor.getValue().getId().toString();
        String sessionIdAddedToCookie = cookieCaptor.getValue().getValue();
        assertEquals(savedSessionId, sessionIdAddedToCookie);
    }
}