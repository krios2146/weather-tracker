package pet.project.servlet;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import pet.project.dao.LocationDao;
import pet.project.dao.SessionDao;
import pet.project.exception.InvalidParameterException;
import pet.project.exception.SessionExpiredException;
import pet.project.exception.UnauthorizedSearchException;
import pet.project.model.Session;
import pet.project.model.User;
import pet.project.model.api.LocationApiResponse;
import pet.project.service.WeatherApiService;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServletTest {
    @Mock
    private SessionDao sessionDao;
    @Mock
    private LocationDao locationDao;
    @Mock
    private WeatherApiService weatherApiService;

    @Mock
    private WebContext context;
    @Mock
    private ITemplateEngine templateEngine;

    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpServletRequest request;

    private SearchServlet searchServlet;

    @BeforeEach
    public void setUp() throws Exception {
        searchServlet = new SearchServlet();

        Field sessionDaoField = searchServlet.getClass().getDeclaredField("sessionDao");
        sessionDaoField.setAccessible(true);
        sessionDaoField.set(searchServlet, sessionDao);

        Field locationDaoField = searchServlet.getClass().getDeclaredField("locationDao");
        locationDaoField.setAccessible(true);
        locationDaoField.set(searchServlet, locationDao);

        Field weatherApiField = searchServlet.getClass().getDeclaredField("weatherApiService");
        weatherApiField.setAccessible(true);
        weatherApiField.set(searchServlet, weatherApiService);

        Field contextField = searchServlet.getClass().getSuperclass().getDeclaredField("context");
        contextField.set(searchServlet, context);

        Field templateField = searchServlet.getClass().getSuperclass().getDeclaredField("templateEngine");
        templateField.set(searchServlet, templateEngine);
    }

    @Test
    public void doGet_emptyCookies_shouldThrowUnauthorizedSearchException() {
        when(request.getCookies()).thenReturn(new Cookie[]{});

        assertThrows(
                UnauthorizedSearchException.class,
                () -> searchServlet.doGet(request, response)
        );
    }

    @Test
    public void doGet_sessionNotFound_shouldThrowSessionExpiredException() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("sessionId", UUID.randomUUID().toString())});

        verify(sessionDao, atMostOnce()).findById(any());
        assertThrows(
                SessionExpiredException.class,
                () -> searchServlet.doGet(request, response)
        );
    }

    @Test
    public void doGet_sessionExpired_shouldThrowSessionExpiredException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MIN);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));

        assertThrows(
                SessionExpiredException.class,
                () -> searchServlet.doGet(request, response)
        );
    }

    @Test
    public void doGet_searchQueryNull_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User());
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));

        assertThrows(
                InvalidParameterException.class,
                () -> searchServlet.doGet(request, response)
        );
    }

    @Test
    public void doGet_searchQueryBlank_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User());
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter("q")).thenReturn(" ");

        assertThrows(
                InvalidParameterException.class,
                () -> searchServlet.doGet(request, response)
        );
    }

    @Test
    public void doGet_geocodingApiCall_shouldProcessSearchPage() throws Exception {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User("login", "password"));
        List<LocationApiResponse> apiResponse = List.of(new LocationApiResponse());
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter("q")).thenReturn("City");
        when(weatherApiService.getLocationsByName(any())).thenReturn(apiResponse);

        searchServlet.doGet(request, response);

        verify(weatherApiService, atMostOnce()).getLocationsByName(eq("City"));
        verify(context, atMostOnce()).setVariable(eq("login"), eq("login"));
        verify(context, atMostOnce()).setVariable(eq("foundLocations"), eq(apiResponse));
        verify(templateEngine, atMostOnce()).process(eq("search"), eq(context), any());
    }
}