package pet.project;

import jakarta.servlet.ServletContext;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

public class ConfiguredTemplateEngine {
    private static final TemplateEngine INSTANCE;

    static {
        INSTANCE = new TemplateEngine();
    }

    private ConfiguredTemplateEngine() {

    }

    public static ITemplateEngine getInstance() {
        return INSTANCE;
    }

    public static void buildTemplateEngine(ServletContext context) {
        IWebApplication application = JakartaServletWebApplication.buildApplication(context);
        ITemplateResolver templateResolver = buildTemplateResolver(application);
        INSTANCE.setTemplateResolver(templateResolver);
    }

    private static ITemplateResolver buildTemplateResolver(IWebApplication application) {
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);

        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/resources/templates/");
        templateResolver.setSuffix(".html");
        
        return templateResolver;
    }
}
