package uk.gov.justice.laa.bulkclaim.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

/** Utility class for extracting attributes from OIDC user details. */
@Component
public class OidcAttributeUtils {

  private static final String OFFICES_ATTR = "LAA_ACCOUNTS";

  /**
   * Retrieves the list of user offices from the OIDC user attributes.
   *
   * @param oidcUser the OIDC user object
   * @return a list of user offices, or an empty list if not found
   */
  public List<String> getUserOffices(OidcUser oidcUser) {
    return getAttributeAsList(oidcUser, OFFICES_ATTR);
  }

  /**
   * Retrieves a specific attribute from the OIDC user attributes as a list of strings.
   *
   * @param oidcUser the OIDC user object
   * @param attributeName the name of the attribute to retrieve
   * @return a list of attribute values, or an empty list if not found
   */
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
