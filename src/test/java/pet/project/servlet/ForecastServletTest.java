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
import pet.project.model.api.ForecastApiResponse;
import pet.project.model.dto.WeatherDto;
import pet.project.service.ForecastService;
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
class ForecastServletTest {
    @Mock
    private SessionDao sessionDao;
    @Mock
    private LocationDao locationDao;
    @Mock
    private WeatherApiService weatherApiService;
    @Mock
    private ForecastService forecastService;

    @Mock
    private WebContext context;
    @Mock
    private ITemplateEngine templateEngine;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private ForecastServlet forecastServlet;

    @BeforeEach
    public void setUp() throws Exception {
        forecastServlet = new ForecastServlet();

        Field sessionDaoField = forecastServlet.getClass().getDeclaredField("sessionDao");
        sessionDaoField.setAccessible(true);
        sessionDaoField.set(forecastServlet, sessionDao);

        Field locationDaoField = forecastServlet.getClass().getDeclaredField("locationDao");
        locationDaoField.setAccessible(true);
        locationDaoField.set(forecastServlet, locationDao);

        Field weatherApiField = forecastServlet.getClass().getDeclaredField("weatherApiService");
        weatherApiField.setAccessible(true);
        weatherApiField.set(forecastServlet, weatherApiService);

        Field forecastServiceFiled = forecastServlet.getClass().getDeclaredField("forecastService");
        forecastServiceFiled.setAccessible(true);
        forecastServiceFiled.set(forecastServlet, forecastService);

        Field contextField = forecastServlet.getClass().getSuperclass().getDeclaredField("context");
        contextField.set(forecastServlet, context);

        Field templateField = forecastServlet.getClass().getSuperclass().getDeclaredField("templateEngine");
        templateField.set(forecastServlet, templateEngine);
    }

    @Test
    public void doGet_emptyCookies_shouldThrowCookieNotFoundException() {
        when(request.getCookies()).thenReturn(new Cookie[]{});

        assertThrows(
                CookieNotFoundException.class,
                () -> forecastServlet.doGet(request, response)
        );
    }

    @Test
    public void doGet_sessionNotFound_shouldThrowSessionExpiredException() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("sessionId", UUID.randomUUID().toString())});

        verify(sessionDao, atMostOnce()).findById(any());
        assertThrows(
                SessionExpiredException.class,
                () -> forecastServlet.doGet(request, response)
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
                () -> forecastServlet.doGet(request, response)
        );
    }

    @Test
    public void doGet_locationIdParamIsNull_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));

        verify(locationDao, never()).findById(any());
        assertThrows(
                InvalidParameterException.class,
                () -> forecastServlet.doGet(request, response)
        );
    }

    @Test
    public void doGet_locationIdParamIsBlank_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter(eq("locationId"))).thenReturn(" ");

        verify(locationDao, never()).findById(any());
        assertThrows(
                InvalidParameterException.class,
                () -> forecastServlet.doGet(request, response)
        );
    }

    @Test
    public void doGet_locationIdParamIsNotANumber_shouldThrowInvalidParameterException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter(eq("locationId"))).thenReturn("Text");

        verify(locationDao, never()).findById(any());
        assertThrows(
                InvalidParameterException.class,
                () -> forecastServlet.doGet(request, response)
        );
    }

    @Test
    public void doGet_locationIsNotFound_shouldThrowLocationNotFoundException() {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.MAX);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter(any())).thenReturn("1");

        verify(locationDao, atMostOnce()).findById(eq(1L));
        assertThrows(
                LocationNotFoundException.class,
                () -> forecastServlet.doGet(request, response)
        );
    }

    @Test
    public void doGet_locationIsFound_shouldProcessForecastPage() throws Exception {
        Cookie cookie = new Cookie("sessionId", UUID.randomUUID().toString());
        User user = new User("login", "password");
        Session session = new Session();
        session.setUser(user);
        session.setExpiresAt(LocalDateTime.MAX);
        Location location = new Location();
        location.setName("Location");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));
        when(request.getParameter(any())).thenReturn("1");
        when(locationDao.findById(anyLong())).thenReturn(Optional.of(location));
        when(weatherApiService.getForecastForLocation(any())).thenReturn(new ForecastApiResponse());
        when(forecastService.getHourlyForecast(any())).thenReturn(List.of(new WeatherDto()));
        when(forecastService.getDailyForecast(any())).thenReturn(List.of(new WeatherDto()));

        forecastServlet.doGet(request, response);

        verify(weatherApiService, atMostOnce()).getForecastForLocation(eq(location));
        verify(forecastService, atMostOnce()).getHourlyForecast(any(ForecastApiResponse.class));
        verify(forecastService, atMostOnce()).getDailyForecast(any(ForecastApiResponse.class));
        verify(context, atMostOnce()).setVariable(eq("login"), eq("login"));
        verify(context, atMostOnce()).setVariable(eq("locationName"), eq("Location"));
        verify(context, atMostOnce()).setVariable(eq("hourlyForecast"), any(List.class));
        verify(context, atMostOnce()).setVariable(eq("dailyForecast"), any(List.class));
        verify(templateEngine, atMostOnce()).process(eq("forecast"), eq(context), any());
    }
}