package uk.gov.justice.laa.cwa.bulkupload.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Rest Client configuration for the application.
 */
@Configuration
public class RestClientConfig {

    @Value("${rest-client.connect-timeout}")
    private int connectTimeout;

    @Value("${rest-client.read-timeout}")
    private int readTimeout;

    /**
     * Creates a RestClient bean with custom timeouts.
     *
     * @return the configured RestClient instance
     */
    @Bean
    public RestClient restClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeout));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}