package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_FORM;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionPage;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

@Controller
@RequiredArgsConstructor
@SessionAttributes(NIL_SUBMISSION_FORM)
public class NilSubmissionAreaOfLawController {

  private final FeatureFlagsConfig featureFlagsConfig;

  @GetMapping("/nil-submission/areaoflaw")
  public String getAreasOfLaw(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form, Model model) {

    featureFlagsConfig.checkNilSubmissionEnabled();

    NilSubmissionSessionManager.nilSubmissionCleanseSession(form, NilSubmissionPage.AREA_OF_LAW);

    model.addAttribute("areasOfLaw", getAreaOfLawOptions());
    return "pages/nil-submission/areaoflaw";
  }

  @PostMapping("/nil-submission/areaoflaw")
  public String postAreaOfLaw(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form,
      BindingResult bindingResult,
      Model model) {

    featureFlagsConfig.checkNilSubmissionEnabled();

    if (form.getAreaOfLaw() == null && !bindingResult.hasFieldErrors("areaOfLaw")) {
      bindingResult.rejectValue("areaOfLaw", "nilSubmission.areaOfLaw.required");
    }

    if (bindingResult.hasErrors()) {
      form.setAreaOfLaw(null);
      model.addAttribute("areasOfLaw", getAreaOfLawOptions());
      return "pages/nil-submission/areaoflaw";
    }

    return "redirect:/nil-submission/period";
  }

  private Map<AreaOfLaw, String> getAreaOfLawOptions() {
    Map<AreaOfLaw, String> options = new LinkedHashMap<>();

    options.put(AreaOfLaw.CRIME_LOWER, "areaOfLaw.CRIME_LOWER");
    options.put(AreaOfLaw.LEGAL_HELP, "areaOfLaw.LEGAL_HELP");
    options.put(AreaOfLaw.MEDIATION, "areaOfLaw.MEDIATION");

    return options;
  }
}
