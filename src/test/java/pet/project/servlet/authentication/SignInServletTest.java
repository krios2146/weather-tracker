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
import pet.project.exception.authentication.UserNotFoundException;
import pet.project.exception.authentication.WrongPasswordException;
import pet.project.model.Session;
import pet.project.model.User;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignInServletTest {
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

    private SignInServlet signInServlet;

    @BeforeEach
    public void setUp() throws Exception {
        signInServlet = new SignInServlet();

        Field sessionDaoField = signInServlet.getClass().getDeclaredField("sessionDao");
        sessionDaoField.setAccessible(true);
        sessionDaoField.set(signInServlet, sessionDao);

        Field locationDaoField = signInServlet.getClass().getDeclaredField("userDao");
        locationDaoField.setAccessible(true);
        locationDaoField.set(signInServlet, userDao);

        Field templateField = signInServlet.getClass().getSuperclass().getDeclaredField("templateEngine");
        templateField.setAccessible(true);
        templateField.set(signInServlet, templateEngine);
    }

    @Test
    public void doGet_shouldProcessSignInPage() throws Exception {
        signInServlet.doGet(request, response);

        verify(templateEngine, atMostOnce()).process(eq("sign-in"), any(WebContext.class), any());
    }

    @Test
    public void doPost_loginParamIsNull_shouldThrowInvalidParameterException() {
        lenient().when(request.getParameter("password")).thenReturn("password");

        Assertions.assertThrows(
                InvalidParameterException.class,
                () -> signInServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_loginParamIsBlank_shouldThrowInvalidParameterException() {
        lenient().when(request.getParameter("password")).thenReturn("password");
        lenient().when(request.getParameter("login")).thenReturn(" ");

        Assertions.assertThrows(
                InvalidParameterException.class,
                () -> signInServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_passwordParamIsNull_shouldThrowInvalidParameterException() {
        lenient().when(request.getParameter("login")).thenReturn("login");

        Assertions.assertThrows(
                InvalidParameterException.class,
                () -> signInServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_passwordParamIsBlank_shouldThrowInvalidParameterException() {
        lenient().when(request.getParameter("login")).thenReturn("login");
        lenient().when(request.getParameter("password")).thenReturn(" ");

        Assertions.assertThrows(
                InvalidParameterException.class,
                () -> signInServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_userIsNotFound_shouldThrowUserNotFoundException() {
        when(request.getParameter("login")).thenReturn("login");
        when(request.getParameter("password")).thenReturn("password");

        verify(userDao, atMostOnce()).findByLogin(eq("login"));
        Assertions.assertThrows(
                UserNotFoundException.class,
                () -> signInServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_givenPasswordIsWrong_shouldThrowWrongPasswordException() {
        User user = new User("login", Password.hash("actualPassword").withBcrypt().getResult());
        when(request.getParameter("login")).thenReturn("login");
        when(request.getParameter("password")).thenReturn("givenPassword");
        when(userDao.findByLogin(any())).thenReturn(Optional.of(user));

        verify(userDao, atMostOnce()).findByLogin(eq("login"));
        Assertions.assertThrows(
                WrongPasswordException.class,
                () -> signInServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_authenticationSuccessful_shouldCreateNewSession() throws Exception {
        User user = new User("login", Password.hash("password").withBcrypt().getResult());
        Session expectedSession = new Session(user, LocalDateTime.now().plusHours(24));
        when(request.getParameter("login")).thenReturn("login");
        when(request.getParameter("password")).thenReturn("password");
        when(userDao.findByLogin(any())).thenReturn(Optional.of(user));
        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);

        signInServlet.doPost(request, response);

        verify(sessionDao, atMostOnce()).save(captor.capture());
        assertEquals(expectedSession.getUser(), captor.getValue().getUser());
        assertEquals(expectedSession.getExpiresAt().withNano(0), captor.getValue().getExpiresAt().withNano(0));
    }

    @Test
    public void doPost_authenticationSuccessful_shouldAddCookie() throws Exception {
        User user = new User("login", Password.hash("password").withBcrypt().getResult());
        when(request.getParameter("login")).thenReturn("login");
        when(request.getParameter("password")).thenReturn("password");
        when(userDao.findByLogin(any())).thenReturn(Optional.of(user));
        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        signInServlet.doPost(request, response);

        verify(sessionDao, atMostOnce()).save(sessionCaptor.capture());
        verify(response, atMostOnce()).addCookie(cookieCaptor.capture());
        String savedSessionId = sessionCaptor.getValue().getId().toString();
        String sessionIdAddedToCookie = cookieCaptor.getValue().getValue();
        assertEquals(savedSessionId, sessionIdAddedToCookie);
    }
}