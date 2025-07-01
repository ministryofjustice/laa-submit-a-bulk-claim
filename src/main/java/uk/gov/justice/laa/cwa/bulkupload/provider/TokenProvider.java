package uk.gov.justice.laa.cwa.bulkupload.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.cwa.bulkupload.exception.TokenProviderException;

/**
 * Responsible for getting access token from OAuth2 provider.
 */
@Component
@RequiredArgsConstructor
public class TokenProvider {

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    /**
     * Get SDS API access token.
     *
     * @return the access token
     */
    @Cacheable(value = "tokenCache", key = "'sdsAccessToken'")
    public OAuth2AccessToken getTokenFromProvider() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("moj-identity")
                .principal("moj-identity-client")
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new TokenProviderException("Failed to obtain SDS API access token");
        }

        return authorizedClient.getAccessToken();
    }

    @CacheEvict(value = "tokenCache", key = "'sdsAccessToken'")
    public void evictToken() {
    }
}
