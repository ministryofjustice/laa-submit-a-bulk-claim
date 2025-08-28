package uk.gov.justice.laa.bulkclaim.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public final class ControllerTestHelper {

  public static OidcUser getOidcUser() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", "1234567890");
    claims.put("email", "test@example.com");

    OidcIdToken oidcIdToken =
        new OidcIdToken("token123", Instant.now(), Instant.now().plusSeconds(60), claims);
    OidcUserInfo oidcUserInfo = new OidcUserInfo(claims);

    return new DefaultOidcUser(
        List.of(new SimpleGrantedAuthority("ROLE_USER")), oidcIdToken, oidcUserInfo, "email");
  }
}
