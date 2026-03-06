package uk.gov.justice.laa.bulkclaim.config.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.client.ExportDataClaimsRestClient;

/**
 * Configuration class for creating and configuring WebClient instances.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({ClaimsApiProperties.class})
public class WebClientConfiguration {

  /**
   * Creates a {@link uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient} bean to communicate
   * with the Claims API using a WebClient instance.
   *
   * @param properties The configuration properties required to initialize the WebClient, including
   *     the base URL and access token for the Provider Details API.
   * @return An instance of {@link uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient} for
   *     interacting with the Claims API.
   */
  @Bean
  public DataClaimsRestClient claimsApiClient(final ClaimsApiProperties properties) {
    HttpServiceProxyFactory factory = createServiceProxy(properties);

    return factory.createClient(DataClaimsRestClient.class);
  }

  /**
   * Creates a {@code DataClaimsRestClientV2} bean to communicate with version 2 of the Claims API.
   * This method initializes a REST client based on the provided configuration properties.
   *
   * @param properties The configuration properties required to initialize the REST client,
   *     including the base URL and access token for the Claims API.
   * @return An instance of {@code DataClaimsRestClientV2} for interacting with version 2 of the
   *     Claims API.
   */
  @Bean
  public DataClaimsRestClientV2 claimsApiClientV2(final ClaimsApiProperties properties) {
    HttpServiceProxyFactory factory = createServiceProxy(properties);

    return factory.createClient(DataClaimsRestClientV2.class);
  }

  /**
   * Creates an instance of {@code HttpServiceProxyFactory} configured with a {@code
   * WebClientAdapter} that is initialized from the provided {@code ClaimsApiProperties}.
   *
   * @param properties The configuration properties containing the base URL and access token
   *     required to create and configure the WebClient.
   * @return A configured {@code HttpServiceProxyFactory} instance.
   */
  private static HttpServiceProxyFactory createServiceProxy(final ClaimsApiProperties properties) {
    final WebClient webClient = createWebClient(properties);
    final WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
    return HttpServiceProxyFactory.builderFor(webClientAdapter).build();
  }

  /**
   * Creates a {@link uk.gov.justice.laa.bulkclaim.client.ExportDataClaimsRestClient} bean to
   * communicate with the Claims API using a WebClient instance. Endpoints under export do not use
   * the v1 api endpoints so have been put in their own service.
   *
   * @param properties The configuration properties required to initialize the WebClient, including
   *     the base URL and access token for the Provider Details API.
   * @return An instance of {@link uk.gov.justice.laa.bulkclaim.client.ExportDataClaimsRestClient}
   *     for interacting with the Claims API's /exports endpoints.
   */
  @Bean
  public ExportDataClaimsRestClient exportClaimsApiClient(final ClaimsApiProperties properties) {
    final WebClient webClient = createWebClient(properties);
    final WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
    HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(webClientAdapter).build();

    return factory.createClient(ExportDataClaimsRestClient.class);
  }

  /**
   * Creates a WebClient instance using the provided configuration properties.
   *
   * @param apiProperties The configuration properties for the API.
   * @return A WebClient instance.
   */
  public static WebClient createWebClient(final ApiProperties apiProperties) {
    final ExchangeStrategies strategies =
        ExchangeStrategies.builder().codecs(ClientCodecConfigurer::defaultCodecs).build();
    return WebClient.builder()
        .baseUrl(apiProperties.getUrl())
        .defaultHeader(HttpHeaders.AUTHORIZATION, apiProperties.getAccessToken())
        .exchangeStrategies(strategies)
        .build();
  }
}
