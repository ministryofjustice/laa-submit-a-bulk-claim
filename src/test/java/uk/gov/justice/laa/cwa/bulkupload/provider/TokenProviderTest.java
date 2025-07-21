package uk.gov.justice.laa.cwa.bulkupload.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import uk.gov.justice.laa.cwa.bulkupload.exception.TokenProviderException;

@ExtendWith(MockitoExtension.class)
class TokenProviderTest {

  @Mock OAuth2AuthorizedClientManager authorizedClientManager;

  @Mock OAuth2AuthorizedClient authorizedClient;

  @Mock OAuth2AccessToken accessToken;

  @InjectMocks TokenProvider tokenProvider;

  @Test
  void shouldGetTokenFromProvider() {
    when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
        .thenReturn(authorizedClient);
    when(authorizedClient.getAccessToken()).thenReturn(accessToken);

    OAuth2AccessToken result = tokenProvider.getTokenFromProvider();

    assertThat(result).isEqualTo(accessToken);
  }

  @Test
  void shouldThrowExceptionIfOauth2ClientIsNull() {

    Exception exception =
        assertThrows(TokenProviderException.class, () -> tokenProvider.getTokenFromProvider());

    assertThat(exception.getMessage()).isEqualTo("Failed to obtain SDS API access token");
  }

  @Test
  void shouldThrowExceptionIfAccessTokenIsNull() {
    when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
        .thenReturn(authorizedClient);

    Exception exception =
        assertThrows(TokenProviderException.class, () -> tokenProvider.getTokenFromProvider());

    assertThat(exception.getMessage()).isEqualTo("Failed to obtain SDS API access token");
  }
}
