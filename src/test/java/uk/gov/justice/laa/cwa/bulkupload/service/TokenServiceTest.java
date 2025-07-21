package uk.gov.justice.laa.cwa.bulkupload.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import uk.gov.justice.laa.cwa.bulkupload.provider.TokenProvider;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

  @Mock TokenProvider tokenProvider;

  @Mock OAuth2AccessToken accessToken;

  @InjectMocks TokenService tokenService;

  @Test
  void shouldGetSdsAccessToken() {
    when(tokenProvider.getTokenFromProvider()).thenReturn(accessToken);
    when(accessToken.getTokenValue()).thenReturn("access_token");
    when(accessToken.getExpiresAt()).thenReturn(Instant.now().plusSeconds(600));

    String result = tokenService.getSdsAccessToken();

    assertThat(result).isEqualTo("access_token");

    verify(tokenProvider, times(1)).getTokenFromProvider();
    verify(tokenProvider, never()).evictToken();
  }

  @Test
  void shouldGetSdsAccessTokenAndRefreshToken() {
    when(tokenProvider.getTokenFromProvider()).thenReturn(accessToken);
    when(accessToken.getTokenValue()).thenReturn("access_token");
    when(accessToken.getExpiresAt()).thenReturn(Instant.now().minusSeconds(600));

    String result = tokenService.getSdsAccessToken();

    assertThat(result).isEqualTo("access_token");

    verify(tokenProvider, times(2)).getTokenFromProvider();
    verify(tokenProvider).evictToken();
  }
}
