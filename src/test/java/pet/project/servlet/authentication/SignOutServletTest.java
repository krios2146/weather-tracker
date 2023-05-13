package pet.project.servlet.authentication;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.ITemplateEngine;
import pet.project.dao.SessionDao;
import pet.project.exception.CookieNotFoundException;
import pet.project.exception.SessionExpiredException;
import pet.project.model.Session;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignOutServletTest {
    @Mock
    private SessionDao sessionDao;

    @Mock
    private ITemplateEngine templateEngine;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private SignOutServlet signOutServlet;

    @BeforeEach
    public void setUp() throws Exception {
        signOutServlet = new SignOutServlet();

        Field sessionDaoField = signOutServlet.getClass().getDeclaredField("sessionDao");
        sessionDaoField.setAccessible(true);
        sessionDaoField.set(signOutServlet, sessionDao);

        Field templateField = signOutServlet.getClass().getSuperclass().getDeclaredField("templateEngine");
        templateField.setAccessible(true);
        templateField.set(signOutServlet, templateEngine);
    }

    @Test
    public void doPost_emptyCookies_shouldThrowCookieNotFoundException() {
        when(request.getCookies()).thenReturn(new Cookie[]{});

        assertThrows(
                CookieNotFoundException.class,
                () -> signOutServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_sessionNotFound_shouldThrowSessionExpiredException() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("sessionId", UUID.randomUUID().toString())});

        verify(sessionDao, atMostOnce()).findById(any());
        assertThrows(
                SessionExpiredException.class,
                () -> signOutServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_sessionExpired_shouldThrowSessionExpiredException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MIN);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));

        assertThrows(
                SessionExpiredException.class,
                () -> signOutServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_signOutSuccessful_shouldDeleteSession() throws Exception {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));

        signOutServlet.doPost(request, response);

        verify(sessionDao, atMostOnce()).delete(eq(session));
    }

    @Test
    public void doPost_signOutSuccessful_shouldDeleteCookie() throws Exception {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);

        signOutServlet.doPost(request, response);

        verify(response, atMostOnce()).addCookie(captor.capture());
        assertEquals(0, captor.getValue().getMaxAge());
    }
}