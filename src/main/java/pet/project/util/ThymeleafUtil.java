package pet.project.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.servlet.IServletWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

public class ThymeleafUtil {
    public static WebContext buildWebContext(HttpServletRequest req, HttpServletResponse resp, ServletContext servletContext) {
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        IServletWebExchange webExchange = application.buildExchange(req, resp);
        return new WebContext(webExchange);
    }

    public static TemplateEngine buildTemplateEngine(ServletContext context) {
        IWebApplication application = JakartaServletWebApplication.buildApplication(context);
        ITemplateResolver templateResolver = buildTemplateResolver(application);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        return templateEngine;
    }

    private static ITemplateResolver buildTemplateResolver(IWebApplication application) {
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);

        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");

        return templateResolver;
    }
}
