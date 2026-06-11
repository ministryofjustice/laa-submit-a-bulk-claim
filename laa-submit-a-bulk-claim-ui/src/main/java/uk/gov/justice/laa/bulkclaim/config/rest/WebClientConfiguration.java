package uk.gov.justice.laa.bulkclaim.config.rest;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.client.ExportDataClaimsRestClient;

@Configuration
@EnableConfigurationProperties({ClaimsApiProperties.class})
@ImportHttpServices(
    types = {
      DataClaimsRestClient.class,
      DataClaimsRestClientV2.class,
      ExportDataClaimsRestClient.class
    },
    clientType = HttpServiceGroup.ClientType.WEB_CLIENT)
public class WebClientConfiguration {

  /**
   * Configures a {@code WebClientHttpServiceGroupConfigurer} for managing WebClient instances used
   * to interact with external APIs. This configuration includes setting up exchange strategies,
   * base URL, and default headers such as authentication tokens.
   *
   * @param properties The configuration properties required to initialize and configure WebClient
   *     instances. It provides the base URL and access token needed for API interactions.
   * @return An instance of {@code WebClientHttpServiceGroupConfigurer} configured with WebClient
   *     settings based on the provided properties.
   */
  @Bean
  public WebClientHttpServiceGroupConfigurer groupConfigurer(final ClaimsApiProperties properties) {
    return groups ->
        groups.forEachClient(
            (spec, webClientBuilder) -> {
              webClientBuilder.exchangeStrategies(
                  ExchangeStrategies.builder()
                      .codecs(ClientCodecConfigurer::defaultCodecs)
                      .build());
              webClientBuilder.baseUrl(properties.getUrl());
              webClientBuilder.defaultHeader(
                  HttpHeaders.AUTHORIZATION, properties.getAccessToken());
            });
  }
}
