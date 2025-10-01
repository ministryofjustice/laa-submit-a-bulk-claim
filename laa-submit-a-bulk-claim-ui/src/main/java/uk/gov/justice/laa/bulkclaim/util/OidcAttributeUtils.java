package uk.gov.justice.laa.bulkclaim.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class OidcAttributeUtils {

  private static final String OFFICES_ATTR = "LAA_ACCOUNTS";

  public List<String> getUserOffices(OidcUser oidcUser) {
    return getAttributeAsList(oidcUser, OFFICES_ATTR);
  }

  private List<String> getAttributeAsList(OidcUser oidcUser, String attributeName) {
    if (oidcUser == null || attributeName == null) {
      return Collections.emptyList();
    }

    return Optional.ofNullable(oidcUser.getAttributes().get(attributeName))
        .map(
            attr -> {
              if (attr instanceof List<?>) {
                return ((List<?>) attr).stream().map(Object::toString).toList();
              } else if (attr instanceof String) {
                return List.of((String) attr);
              } else {
                return List.<String>of();
              }
            })
        .orElse(Collections.emptyList());
  }
}
