package uk.gov.justice.laa.cwa.bulkupload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.LiteWebJarsResourceResolver;

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

}
