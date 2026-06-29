package uk.gov.justice.laa.bulkclaim.controller;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
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

    Set<String> areasOfLaw =
        Arrays.stream(AreaOfLaw.values()).map(AreaOfLaw::getValue).collect(Collectors.toSet());
    System.out.println("Areas of law: " + areasOfLaw);
    model.addAttribute("areasOfLaw", areasOfLaw);
    return "pages/nil-submission-areaoflaw";
  }

  @PostMapping("/nil-submission-areaoflaw")
  public String postAreaOfLaw(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form, @RequestParam String areaOfLaw) {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }
    form.setAreaOfLaw(areaOfLaw);

    return "redirect:/nil-submission-period";
  }
}
