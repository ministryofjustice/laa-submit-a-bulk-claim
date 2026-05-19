package uk.gov.justice.laa.bulkclaim.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.LiteWebJarsResourceResolver;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.justice.laa.bulkclaim.interceptor.CookieConsentInterceptor;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig implements WebMvcConfigurer {

    private final CookieConsentInterceptor cookieConsentInterceptor;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/")
        .resourceChain(true)
        .addResolver(new LiteWebJarsResourceResolver());
  }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(cookieConsentInterceptor);
    }

  @Bean
  RestClient restClient(RestClient.Builder builder) {
    return builder.build();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return JsonMapper.builder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build();
  }
}
