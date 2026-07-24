package uk.gov.justice.laa.bulkclaim.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class LoggingConfig {

    private static final List<String> IGNORED_URLS =
            List.of(
                    "/actuator",
                    "/health",
                    "/assets",
                    "/javascripts",
                    "/stylesheets",
                    "/static",
                    "/public");

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        return new CommonsRequestLoggingFilter() {
            {
                setIncludePayload(false);
                setIncludeHeaders(false);
                setBeforeMessagePrefix("HTTP [");
            }

            @Override
            protected void beforeRequest(HttpServletRequest request, String message) {
                logger.info(message);
            }

            @Override
            protected void afterRequest(HttpServletRequest request, String message) {
                // Only log before each request. No need to wait to log after as we're not logging payloads
            }

            @Override
            protected boolean shouldLog(HttpServletRequest request) {
                String uri = request.getRequestURI();
                return IGNORED_URLS.stream().noneMatch(uri::startsWith);
            }
        };
    }
}
