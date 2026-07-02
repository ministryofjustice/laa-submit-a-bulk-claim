package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.NilSubmissionInfoMessageConstants.SUBMISSION_INFO_MESSAGE_PAGE_HEADING;
import static uk.gov.justice.laa.bulkclaim.constants.NilSubmissionInfoMessageConstants.SUBMISSION_INFO_MESSAGE_TEXT;

import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionPage;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;

@Controller
@RequiredArgsConstructor
@SessionAttributes("nilSubmissionForm")
public class NilSubmissionOfficeController {

  private final OidcAttributeUtils oidcAttributeUtils;
  private final FeatureFlagsConfig featureFlagsConfig;
  private final MessageSource messageSource;

  @ModelAttribute("nilSubmissionForm")
  public NilSubmissionForm nilSubmissionForm() {
    return new NilSubmissionForm();
  }

  @GetMapping("/nil-submission-office")
  public String getNilSubmissionOffice(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form,
      @AuthenticationPrincipal OidcUser oidcUser,
      Model model) {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }

    NilSubmissionSessionManager.nilSubmissionCleanseSession(form, NilSubmissionPage.OFFICE);

    List<String> userOffices = oidcAttributeUtils.getUserOffices(oidcUser);
    if (userOffices.isEmpty()) {
      model.addAttribute(
          SUBMISSION_INFO_MESSAGE_PAGE_HEADING,
          messageSource.getMessage("nilSubmission.noOffices.primary.heading", null, Locale.UK));
      model.addAttribute(
          SUBMISSION_INFO_MESSAGE_TEXT,
          messageSource.getMessage("nilSubmission.noOffices.message", null, Locale.UK));
      return "pages/nil-submission-info-message";
    }
    form.setOfficeCount(userOffices.size());
    model.addAttribute("userOffices", userOffices);

    return "pages/nil-submission-office";
  }

  @PostMapping("/nil-submission-office")
  public String postNilSubmissionOffice(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form,
      Model model,
      @RequestParam String office) {
    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }

    form.setOffice(office);
    model.addAttribute("selectedOffice", office);

    return "redirect:/nil-submission-areaoflaw";
  }
}
