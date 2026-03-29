package autoservice.model.config.security;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer {
    // Этот класс заставляет Tomcat пропустить все запросы через Spring Security
}
