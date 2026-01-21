package uk.gov.justice.laa.bulkclaim.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Error;
import uk.gov.justice.laa.bulkclaim.exception.TokenProviderException;

@ExtendWith(MockitoExtension.class)
class TokenProviderTest {

  @Mock OAuth2AuthorizedClientManager authorizedClientManager;

  @Mock OAuth2AuthorizedClient authorizedClient;

  @Mock OAuth2AccessToken accessToken;
  @Captor ArgumentCaptor<OAuth2AuthorizeRequest> auth2AuthorizeRequestCaptor;

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

  @DisplayName("should throw exception when SDS API access token is invalid")
  @Test
  void shouldThrowExceptionWhenAccessTokenIsInvalid() {
    when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
        .thenThrow(
            new ClientAuthorizationException(
                new OAuth2Error("unauthorized_client"), "invalid token"));
    assertThrows(TokenProviderException.class, () -> tokenProvider.getTokenFromProvider());
    verify(authorizedClientManager).authorize(auth2AuthorizeRequestCaptor.capture());
    assertThat(auth2AuthorizeRequestCaptor.getValue().getClientRegistrationId())
        .isEqualTo("moj-identity");
    assertThat(auth2AuthorizeRequestCaptor.getValue().getPrincipal().getName())
        .isEqualTo("moj-identity-client");
    verify(authorizedClient, never()).getAccessToken();
  }
}
