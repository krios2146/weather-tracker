package pet.project.util;

import jakarta.servlet.ServletContext;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

public class ThymeleafUtil {
    private static final TemplateEngine TEMPLATE_ENGINE_INSTANCE = new TemplateEngine();

    private ThymeleafUtil() {

    }

    public static ITemplateEngine getTemplateEngine() {
        return TEMPLATE_ENGINE_INSTANCE;
    }

    public static void buildTemplateEngine(ServletContext context) {
        IWebApplication application = JakartaServletWebApplication.buildApplication(context);
        ITemplateResolver templateResolver = buildTemplateResolver(application);
        TEMPLATE_ENGINE_INSTANCE.setTemplateResolver(templateResolver);
    }

    private static ITemplateResolver buildTemplateResolver(IWebApplication application) {
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);

        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");

        return templateResolver;
    }
}
