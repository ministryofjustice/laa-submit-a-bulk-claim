package uk.gov.justice.laa.cwa.bulkupload.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.justice.laa.cwa.bulkupload.helper.ProviderHelper;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadErrorResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadSummaryResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.service.CwaUploadService;

/** Controller for handling search requests related to bulk uploads. */
@Slf4j
@RequiredArgsConstructor
@Controller
public class SearchController {

  private final CwaUploadService cwaUploadService;
  private final ProviderHelper providerHelper;

  /**
   * Handles the search form submission and retrieves upload summaries and errors.
   *
   * @param provider the selected provider
   * @param searchTerm the search term (file reference)
   * @param model the model to add attributes to
   * @param oidcUser the authenticated user principal
   * @return the name of the view to render
   */
  @PostMapping("/search")
  public String submitForm(
      String provider, String searchTerm, Model model, @AuthenticationPrincipal OidcUser oidcUser) {

    Map<String, String> errors = new LinkedHashMap<>();

    if (!StringUtils.hasText(provider)) {
      errors.put("provider", "Please select a provider");
    }

    if (!StringUtils.hasText(searchTerm) || searchTerm.length() > 10) {
      errors.put("searchTerm", "File reference must be between 1 to 10 characters long");
    }

    if (!errors.isEmpty()) {
      return handleErrors(model, oidcUser.getName(), provider, searchTerm, errors);
    }

    List<CwaUploadSummaryResponseDto> summary;
    try {
      summary = cwaUploadService.getUploadSummary(searchTerm, oidcUser.getName(), provider);
      model.addAttribute("summary", summary);
    } catch (Exception e) {
      log.error("Error retrieving upload summary: {}", e.getMessage());
      errors.put("search", "Search failed please try again.");
      return handleErrors(model, oidcUser.getName(), provider, searchTerm, errors);
    }

    try {
      List<CwaUploadErrorResponseDto> uploadErrors =
          cwaUploadService.getUploadErrors(searchTerm, oidcUser.getName(), provider);
      model.addAttribute("errors", uploadErrors);
    } catch (Exception e) {
      log.error("Error retrieving upload errors: {}", e.getMessage());
      errors.put("search", "Search failed please try again.");
      return handleErrors(model, oidcUser.getName(), provider, searchTerm, errors);
    }

    return "pages/submission-results";
  }

  /**
   * Handles errors during the search process and prepares the model for rendering the upload page.
   *
   * @param model the model to add attributes to
   * @param username the authenticated user principal
   * @param provider the selected provider
   * @param searchTerm the search term (file reference)
   * @param errors a map of error messages
   * @return the name of the view to render
   */
  private String handleErrors(
      Model model,
      String username,
      String provider,
      String searchTerm,
      Map<String, String> errors) {
    model.addAttribute("errors", errors);
    if (StringUtils.hasText(provider)) {
      try {
        model.addAttribute("selectedProvider", Integer.parseInt(provider));
      } catch (NumberFormatException ignored) {
        model.addAttribute("selectedProvider", 0);
      }
    }

    if (StringUtils.hasText(searchTerm)) {
      model.addAttribute("searchTerm", searchTerm);
    }
    providerHelper.populateProviders(model, username);
    model.addAttribute("tab", "search");

    return "pages/upload";
  }
}
