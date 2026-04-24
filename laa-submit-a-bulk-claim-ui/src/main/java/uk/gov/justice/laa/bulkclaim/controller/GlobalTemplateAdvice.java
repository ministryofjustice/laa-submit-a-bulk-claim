package uk.gov.justice.laa.bulkclaim.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import uk.gov.justice.laa.bulkclaim.dto.AuthenticatedUserDetails;

/**
 * A controller advice that provides global attributes to be used across the application. This class
 * exposes a model attribute which can be accessed in views.
 */
@ControllerAdvice
public class GlobalTemplateAdvice {
  @Value("${links.laa-homepage}")
  private String laaHomepageLink;

  @Value("${links.bulk-upload-amendments}")
  private String laaAmendmentsLink;

  @Value("${links.bulk-upload-guidance}")
  private String laaBulkUploadGuidanceText;

  @ModelAttribute("laaHomepageLink")
  public String laaHomepageLink() {
    return laaHomepageLink;
  }

  @ModelAttribute("laaAmendmentsLink")
  public String laaAmendmentsLink() {
    return laaAmendmentsLink;
  }

  @ModelAttribute("laaBulkUploadGuidanceText")
  public String laaBulkUploadGuidanceText() {
    return laaBulkUploadGuidanceText;
  }

  /**
   * Builds authenticated user details from authentication.
   *
   * @param authentication the authentication object
   * @return the authenticated user details
   */
  @ModelAttribute("authenticatedUser")
  public AuthenticatedUserDetails authenticatedUser(Authentication authentication) {
    return new AuthenticatedUserDetails(
        getAttribute(authentication, "preferred_username", "email"),
        getAttribute(authentication, "FIRM_NAME", "firm_name"),
        getAttribute(authentication, "FIRM_CODE", "firm_code"));
  }

  /**
   * Builds authenticated user details from authentication.
   *
   * @param authentication the authentication object
   * @return the authenticated user details
   */
  private String getAttribute(Authentication authentication, String... keys) {
    if (authentication == null || authentication.getPrincipal() == null) {
      return "";
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof OAuth2User oauthUser) {
      for (String key : keys) {
        Object value = oauthUser.getAttributes().get(key);
        if (value != null) {
          return value.toString();
        }
      }
    }

    return authentication.getName() != null ? authentication.getName() : "";
  }
}
