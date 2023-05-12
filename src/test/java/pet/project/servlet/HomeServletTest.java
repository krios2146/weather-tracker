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
import pet.project.exception.CookieNotFoundException;
import pet.project.exception.InvalidParameterException;
import pet.project.exception.LocationNotFoundException;
import pet.project.exception.SessionExpiredException;
import pet.project.model.Location;
import pet.project.model.Session;
import pet.project.model.User;
import pet.project.model.api.WeatherApiResponse;
import pet.project.model.api.entity.Clouds;
import pet.project.model.api.entity.Main;
import pet.project.model.api.entity.Weather;
import pet.project.model.api.entity.Wind;
import pet.project.service.WeatherApiService;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeServletTest {
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
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private HomeServlet homeServlet;

    @BeforeEach
    public void setUp() throws Exception {
        homeServlet = new HomeServlet();

        Field sessionDaoField = homeServlet.getClass().getDeclaredField("sessionDao");
        sessionDaoField.setAccessible(true);
        sessionDaoField.set(homeServlet, sessionDao);

        Field locationDaoField = homeServlet.getClass().getDeclaredField("locationDao");
        locationDaoField.setAccessible(true);
        locationDaoField.set(homeServlet, locationDao);

        Field weatherApiField = homeServlet.getClass().getDeclaredField("weatherApiService");
        weatherApiField.setAccessible(true);
        weatherApiField.set(homeServlet, weatherApiService);

        Field contextField = homeServlet.getClass().getSuperclass().getDeclaredField("context");
        contextField.set(homeServlet, context);

        Field templateField = homeServlet.getClass().getSuperclass().getDeclaredField("templateEngine");
        templateField.set(homeServlet, templateEngine);
    }

    @Test
    public void doGet_emptyCookies_shouldThrowCookieNotFoundException() {
        when(request.getCookies()).thenReturn(new Cookie[]{});

        assertThrows(
                CookieNotFoundException.class,
                () -> homeServlet.doGet(request, response)
        );
    }

    @Test
    public void doGet_sessionNotFound_shouldThrowSessionExpiredException() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("sessionId", UUID.randomUUID().toString())});

        verify(sessionDao, atMostOnce()).findById(any());
        assertThrows(
                SessionExpiredException.class,
                () -> homeServlet.doGet(request, response)
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
                () -> homeServlet.doGet(request, response)
        );
    }

    @Test
    public void doGet_noWeatherApiCall_shouldProcessHomePage() throws Exception {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User("login", "password"));
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));

        homeServlet.doGet(request, response);

        verify(context, atMostOnce()).setVariable(eq("login"), eq("login"));
        verify(context, atMostOnce()).setVariable(eq("locationWeatherMap"), anyMap());
        verify(templateEngine, atMostOnce()).process(eq("home"), eq(context), any());
    }

    @Test
    public void doGet_weatherApiCall_shouldProcessHomePage() throws Exception {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(new User("login", "password"));
        WeatherApiResponse apiResponse = mock(WeatherApiResponse.class);
        WeatherApiResponse.Sys sys = mock(WeatherApiResponse.Sys.class);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(locationDao.findByUser(any())).thenReturn(List.of(new Location()));
        when(weatherApiService.getWeatherForLocation(any())).thenReturn(apiResponse);
        when(apiResponse.getWeatherList()).thenReturn(List.of(new Weather()));
        when(apiResponse.getMain()).thenReturn(new Main());
        when(apiResponse.getWind()).thenReturn(new Wind());
        when(apiResponse.getClouds()).thenReturn(new Clouds());
        when(apiResponse.getDate()).thenReturn(LocalDateTime.now());
        when(apiResponse.getSys()).thenReturn(sys);
        when(sys.getSunriseTime()).thenReturn(LocalDateTime.now());
        when(sys.getSunsetTime()).thenReturn(LocalDateTime.now());

        homeServlet.doGet(request, response);

        verify(context, atMostOnce()).setVariable(eq("login"), eq("login"));
        verify(context, atMostOnce()).setVariable(eq("locationWeatherMap"), anyMap());
        verify(templateEngine, atMostOnce()).process(eq("home"), eq(context), any());
    }

    @Test
    public void doPost_emptyCookies_shouldThrowCookieNotFoundException() {
        when(request.getCookies()).thenReturn(new Cookie[]{});

        assertThrows(
                CookieNotFoundException.class,
                () -> homeServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_sessionNotFound_shouldThrowSessionExpiredException() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("sessionId", UUID.randomUUID().toString())});

        verify(sessionDao, atMostOnce()).findById(any());
        assertThrows(
                SessionExpiredException.class,
                () -> homeServlet.doPost(request, response)
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
                () -> homeServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_locationIdParamIsNull_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));

        verify(locationDao, never()).findById(any());
        assertThrows(
                InvalidParameterException.class,
                () -> homeServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_locationIdParamIsBlank_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter(eq("locationId"))).thenReturn(" ");

        verify(locationDao, never()).findById(any());
        assertThrows(
                InvalidParameterException.class,
                () -> homeServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_locationIdParamIsNotANumber_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter(eq("locationId"))).thenReturn("Text");

        verify(locationDao, never()).findById(any());
        assertThrows(
                InvalidParameterException.class,
                () -> homeServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_locationIsNotFound_shouldThrowLocationNotFoundException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter(any())).thenReturn("1");

        verify(locationDao, atMostOnce()).findById(eq(1L));
        assertThrows(
                LocationNotFoundException.class,
                () -> homeServlet.doPost(request, response)
        );
    }

    @Test
    public void doPost_locationIsFound_shouldDeleteUserFromLocation() throws Exception {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        User userToPersist = new User();
        User userToDelete = new User();
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(userToDelete);
        Location location = new Location();
        location.setUsers(new ArrayList<>(asList(userToDelete, userToPersist)));
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter(any())).thenReturn("1");
        when(locationDao.findById(eq(1L))).thenReturn(Optional.of(location));

        homeServlet.doPost(request, response);

        verify(locationDao, atMostOnce()).update(location);
        assertEquals(List.of(userToPersist), location.getUsers());
    }

    @Test
    public void doPost_deletingSuccessful_shouldRedirectToHomePage() throws Exception {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        User userToPersist = new User();
        User userToDelete = new User();
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        session.setUser(userToDelete);
        Location location = new Location();
        location.setUsers(new ArrayList<>(asList(userToDelete, userToPersist)));
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter(any())).thenReturn("1");
        when(locationDao.findById(eq(1L))).thenReturn(Optional.of(location));

        homeServlet.doPost(request, response);

        verify(response, atMostOnce()).sendRedirect(request.getContextPath());
    }
}