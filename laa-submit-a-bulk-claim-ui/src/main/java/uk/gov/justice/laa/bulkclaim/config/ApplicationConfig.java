package uk.gov.justice.laa.bulkclaim.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.LiteWebJarsResourceResolver;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adds application config regarding webjar locations.
 *
 * @author Jamie Briggs
 */
@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

  /**
   * Configures resource handling for the application. This method maps the "/webjars/**" URL
   * pattern to the webjars resources located in the "classpath:/META-INF/resources/webjars/"
   * directory and configures a resource chain with a custom resolver.
   *
   * @param registry the ResourceHandlerRegistry to register resource handlers
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/")
        .resourceChain(true)
        .addResolver(new LiteWebJarsResourceResolver());
  }

  @Bean
  RestClient restClient(RestClient.Builder builder) {
    return builder.build();
  }

  /**
   * Creates and configures an instance of {@link ObjectMapper}. The configured {@link ObjectMapper}
   * is built to ignore unknown properties during deserialization by disabling the {@link
   * DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES} feature.
   *
   * @return a configured {@link ObjectMapper} instance
   */
  @Bean
  public ObjectMapper objectMapper() {
    return JsonMapper.builder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build();
  }
}
