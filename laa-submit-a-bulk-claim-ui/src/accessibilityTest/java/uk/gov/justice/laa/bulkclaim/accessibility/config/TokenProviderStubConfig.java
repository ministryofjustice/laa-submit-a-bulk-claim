package uk.gov.justice.laa.bulkclaim.accessibility.config;

import java.time.Instant;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import uk.gov.justice.laa.bulkclaim.provider.TokenProvider;

/**
 * Accessibility-test override for {@link TokenProvider}.
 *
 * <p>Accessibility scenarios use WireMock for Claims API interactions, so a real client-credentials
 * token exchange is unnecessary and would add network/certificate fragility. This test config
 * supplies a stable in-memory bearer token to keep browser tests deterministic.
 */
@TestConfiguration
public class TokenProviderStubConfig {

  /** Provides a deterministic bearer token used by REST clients during accessibility tests. */
  @Bean
  @Primary
  TokenProvider tokenProvider() {
    return new TokenProvider(null) {
      @Override
      public OAuth2AccessToken getTokenFromProvider() {
        return new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "accessibility-token",
            Instant.now(),
            Instant.now().plusSeconds(3600));
      }

      @Override
      public void evictToken() {
        // No-op for accessibility tests.
      }
    };
  }
}
