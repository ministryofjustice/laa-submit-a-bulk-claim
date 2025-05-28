package uk.gov.justice.laa.cwa.bulkupload.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.cwa.bulkupload.provider.TokenProvider;

import java.time.Instant;

/**
 * Service class for getting an access token.
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenProvider tokenProvider;

    /**
     * Get the SDS API access token.
     *
     * @return the access token
     */
    public String getSdsAccessToken() {
        OAuth2AccessToken accessToken = tokenProvider.getTokenFromProvider();

        if (isValidToken(accessToken)) {
            return accessToken.getTokenValue();
        }

        // Evict token and get new token
        tokenProvider.evictToken();
        return tokenProvider.getTokenFromProvider().getTokenValue();
    }

    private boolean isValidToken(OAuth2AccessToken accessToken) {
        return accessToken != null && accessToken.getExpiresAt() != null
                && accessToken.getExpiresAt().isAfter(Instant.now());
    }

}
