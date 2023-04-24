package pet.project.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import pet.project.util.ThymeleafUtil;

import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class WeatherTrackerFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ThymeleafUtil.buildTemplateEngine(filterConfig.getServletContext());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        servletRequest.setCharacterEncoding("UTF-8");
        servletResponse.setCharacterEncoding("UTF-8");
        servletResponse.setContentType("text/html");

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
