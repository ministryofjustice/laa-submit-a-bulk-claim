package uk.gov.justice.laa.bulkclaim.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

@Controller
@RequiredArgsConstructor
@SessionAttributes("nilSubmissionForm")
public class NilSubmissionAreaOfLawController {

  private final FeatureFlagsConfig featureFlagsConfig;

  @GetMapping("/nil-submission-areaoflaw")
  public String getAreasOfLaw(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form, Model model) {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }

    NilSubmissionSessionManager.nilSubmissionCleanseSession(form, NilSubmissionPage.AREA_OF_LAW);

    model.addAttribute("areasOfLaw", getAreaOfLawOptions());
    return "pages/nil-submission-areaoflaw";
  }

  @PostMapping("/nil-submission-areaoflaw")
  public String postAreaOfLaw(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form, @RequestParam String areaOfLaw) {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }
    form.setAreaOfLaw(areaOfLaw);
    System.out.println("AOL: " + form.getAreaOfLaw());
    return "redirect:/nil-submission-period";
  }

  private Map<String, String> getAreaOfLawOptions() {
    Map<String, String> options = new LinkedHashMap<>();

    options.put(AreaOfLaw.CRIME_LOWER.name(), "Crime lower");
    options.put(AreaOfLaw.LEGAL_HELP.name(), "Legal help");
    options.put(AreaOfLaw.MEDIATION.name(), "Mediation");

    return options;
  }
}
