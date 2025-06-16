package uk.gov.justice.laa.cwa.bulkupload.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * Entra Id config for the application.
 */
@Profile("!test") // disable security for test profile
@Configuration
public class EntraIdConfig {

    @Value("${azure.entra-id.client-id}")
    private String clientId;

    @Value("${azure.entra-id.client-secret}")
    private String clientSecret;

    @Value("${azure.entra-id.tenant-id}")
    private String tenantId;

    @Value("${azure.entra-id.scope}")
    private String scope;

    @Value("${azure.entra-id.cloud-instance}")
    private String cloudInstance;

    /**
     * Client registration repository.
     *
     * @return the client registration repository
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(clientRegistration());
    }

    /**
     * OAuth2 authorized client service.
     *
     * @param clientRegistrationRepository the client registration repository
     * @return the OAuth2 authorized client service
     */
    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    /**
     * OAuth2 authorized client manager.
     *
     * @param clientRegistrationRepository the client registration repository
     * @param authorizedClientService the authorized client service
     * @return the OAuth2 authorized client manager
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                 OAuth2AuthorizedClientService authorizedClientService) {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager
                = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    private ClientRegistration clientRegistration() {
        return ClientRegistration
                .withRegistrationId("azure")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri(String.format("%s/%s/oauth2/v2.0/token", cloudInstance, tenantId))
                .scope(scope)
                .build();
    }
}
