package uk.gov.justice.laa.cwa.bulkupload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

/**
 * Rest Client configuration for the application.
 */
@Profile("!test")
@Configuration
public class RestClientConfig {
    /**
     * Rest Client.
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}