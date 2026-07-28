package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static uk.gov.justice.laa.bulkclaim.constants.NilSubmissionInfoMessageConstants.SUBMISSION_INFO_MESSAGE_PAGE_HEADING;
import static uk.gov.justice.laa.bulkclaim.constants.NilSubmissionInfoMessageConstants.SUBMISSION_INFO_MESSAGE_TEXT;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_FORM;
import static uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager.cleanseSession;
import static uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager.validateSessionState;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionPage;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;

@Controller
@RequiredArgsConstructor
@SessionAttributes(NIL_SUBMISSION_FORM)
public class NilSubmissionOfficeController {

  private final OidcAttributeUtils oidcAttributeUtils;
  private final FeatureFlagsConfig featureFlagsConfig;

  @ModelAttribute(NIL_SUBMISSION_FORM)
  public NilSubmissionForm nilSubmissionForm() {
    return new NilSubmissionForm();
  }

  @GetMapping("/nil-submission/office")
  public String getNilSubmissionOffice(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form,
      @AuthenticationPrincipal OidcUser oidcUser,
      Model model) {

    featureFlagsConfig.checkNilSubmissionEnabled();
    validateSessionState(form, NilSubmissionPage.OFFICE);
    cleanseSession(form, NilSubmissionPage.OFFICE);

    List<String> userOffices = oidcAttributeUtils.getUserOffices(oidcUser);
    if (userOffices.isEmpty()) {
      model.addAttribute(
          SUBMISSION_INFO_MESSAGE_PAGE_HEADING, "nilSubmission.noOffices.primary.heading");
      model.addAttribute(SUBMISSION_INFO_MESSAGE_TEXT, "nilSubmission.noOffices.message");
      return "pages/nil-submission/info-message";
    }
    form.setOfficeCount(userOffices.size());
    if (userOffices.size() == 1) {
      form.setOffice(userOffices.getFirst());
    }
    model.addAttribute("userOffices", userOffices);

    return "pages/nil-submission/office";
  }

  @PostMapping("/nil-submission/office")
  public String postNilSubmissionOffice(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form,
      BindingResult bindingResult,
      @AuthenticationPrincipal OidcUser oidcUser,
      Model model) {

    featureFlagsConfig.checkNilSubmissionEnabled();
    validateSessionState(form, NilSubmissionPage.OFFICE);

    List<String> userOffices = oidcAttributeUtils.getUserOffices(oidcUser);
    form.setOfficeCount(userOffices.size());

    if (!StringUtils.hasText(form.getOffice())) {
      bindingResult.rejectValue("office", "nilSubmission.office.required");
    } else if (!userOffices.contains(form.getOffice())) {
      bindingResult.rejectValue("office", "nilSubmission.office.invalid");
    }

    if (bindingResult.hasErrors()) {
      form.setOffice(null);
      model.addAttribute("userOffices", userOffices);
      return "pages/nil-submission/office";
    }

    return "redirect:/nil-submission/areaoflaw";
  }
}
