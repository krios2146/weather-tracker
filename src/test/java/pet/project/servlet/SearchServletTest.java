package pet.project.servlet;

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
import org.thymeleaf.context.WebContext;
import pet.project.dao.LocationDao;
import pet.project.dao.SessionDao;
import pet.project.exception.CookieNotFoundException;
import pet.project.exception.InvalidParameterException;
import pet.project.exception.SessionExpiredException;
import pet.project.exception.UnauthorizedSearchException;
import pet.project.model.Location;
import pet.project.model.Session;
import pet.project.model.User;
import pet.project.model.api.LocationApiResponse;
import pet.project.service.WeatherApiService;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    public void doGet_searchQueryIsNull_shouldThrowInvalidParameterException() {
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
    public void doGet_searchQueryIsBlank_shouldThrowInvalidParameterException() {
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

    @Test
    public void doPost_emptyCookies_shouldThrowCookieNotFoundException() {
        when(request.getCookies()).thenReturn(new Cookie[]{});

        assertThrows(
                CookieNotFoundException.class,
                () -> searchServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_sessionNotFound_shouldThrowSessionExpiredException() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("sessionId", UUID.randomUUID().toString())});

        verify(sessionDao, atMostOnce()).findById(any());
        assertThrows(
                SessionExpiredException.class,
                () -> searchServlet.doPost(request, response)
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
                () -> searchServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_nameParamIsNull_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User());
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        lenient().when(request.getParameter("longitude")).thenReturn("50");
        lenient().when(request.getParameter("latitude")).thenReturn("50");

        assertThrows(
                InvalidParameterException.class,
                () -> searchServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_nameParamIsBlank_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User());
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter("longitude")).thenReturn("50");
        when(request.getParameter("latitude")).thenReturn("50");
        when(request.getParameter("name")).thenReturn(" ");

        assertThrows(
                InvalidParameterException.class,
                () -> searchServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_latitudeParamIsNull_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User());
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        lenient().when(request.getParameter("name")).thenReturn("Location");
        lenient().when(request.getParameter("longitude")).thenReturn("50");

        assertThrows(
                InvalidParameterException.class,
                () -> searchServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_latitudeParamIsBlank_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User());
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter("name")).thenReturn("Location");
        when(request.getParameter("longitude")).thenReturn("50");
        when(request.getParameter("latitude")).thenReturn(" ");

        assertThrows(
                InvalidParameterException.class,
                () -> searchServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_longitudeParamIsNull_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User());
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        lenient().when(request.getParameter("name")).thenReturn("Location");
        lenient().when(request.getParameter("latitude")).thenReturn("50");

        assertThrows(
                InvalidParameterException.class,
                () -> searchServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_longitudeParamIsBlank_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User());
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter("name")).thenReturn("Location");
        when(request.getParameter("latitude")).thenReturn("50");
        when(request.getParameter("longitude")).thenReturn(" ");

        assertThrows(
                InvalidParameterException.class,
                () -> searchServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_longitudeParamIsNotANumber_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User());
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter("name")).thenReturn("Location");
        when(request.getParameter("latitude")).thenReturn("50");
        when(request.getParameter("longitude")).thenReturn("Text");

        assertThrows(
                InvalidParameterException.class,
                () -> searchServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_latitudeParamIsNotANumber_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User());
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter("name")).thenReturn("Location");
        when(request.getParameter("longitude")).thenReturn("50");
        when(request.getParameter("latitude")).thenReturn("Text");

        assertThrows(
                InvalidParameterException.class,
                () -> searchServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_longitudeParamIsOutOfRange_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User());
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter("name")).thenReturn("Location");
        when(request.getParameter("latitude")).thenReturn("50");
        when(request.getParameter("longitude")).thenReturn("1000");

        assertThrows(
                InvalidParameterException.class,
                () -> searchServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_latitudeParamIsOutOfRange_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User());
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter("name")).thenReturn("Location");
        when(request.getParameter("longitude")).thenReturn("50");
        when(request.getParameter("latitude")).thenReturn("1000");

        assertThrows(
                InvalidParameterException.class,
                () -> searchServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_locationIsFound_shouldAddUserToLocation() throws Exception {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        User newUser = new User();
        User oldUser = new User();
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(newUser);
        Location location = new Location();
        location.setUsers(new ArrayList<>(List.of(oldUser)));
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter("name")).thenReturn("Location");
        when(request.getParameter("longitude")).thenReturn("50");
        when(request.getParameter("latitude")).thenReturn("50");
        when(locationDao.findByCoordinates(anyDouble(), anyDouble())).thenReturn(Optional.of(location));

        searchServlet.doPost(request, response);

        verify(locationDao, atMostOnce()).update(location);
        assertEquals(List.of(oldUser, newUser), location.getUsers());
        verify(response, atMostOnce()).sendRedirect(anyString());
    }

    @Test
    public void doPost_locationIsNotFound_shouldCreateLocation() throws Exception {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        User user = new User();
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(user);
        Location expectedLocation = new Location(
                "Location",
                List.of(user),
                50.0,
                50.0
        );
        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter("name")).thenReturn("Location");
        when(request.getParameter("longitude")).thenReturn("50");
        when(request.getParameter("latitude")).thenReturn("50");
        when(locationDao.findByCoordinates(anyDouble(), anyDouble())).thenReturn(Optional.empty());

        searchServlet.doPost(request, response);

        verify(locationDao, atMostOnce()).save(captor.capture());
        assertEquals(expectedLocation, captor.getValue());
        verify(response, atMostOnce()).sendRedirect(anyString());
    }
}