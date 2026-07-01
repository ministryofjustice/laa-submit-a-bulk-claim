package uk.gov.justice.laa.bulkclaim.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;

@Controller
@RequiredArgsConstructor
@SessionAttributes("nilSubmissionForm")
public class NilSubmissionScheduleReferenceController {

  private final DataClaimsRestClient claimsRestService;
  private final FeatureFlagsConfig featureFlagsConfig;

  @GetMapping("/nil-submission-reference")
  public String getReference(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form, Model model) {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }

      String label = switch (form.getAreaOfLaw()) {
          case "LEGAL_HELP" -> "Civil submission reference";
          case "MEDIATION" -> "Mediation submission reference";
          case "CRIME_LOWER" -> "Crime schedule number";
          default -> throw new IllegalStateException("Unexpected value: " + form.getAreaOfLaw());
      };

    model.addAttribute("referenceLabel", label);

    return "pages/nil-submission-reference";
  }

  @PostMapping("/nil-submission-reference")
  public String postReference(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form,
      @RequestParam String scheduleReference) {
    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }
    form.setScheduleReference(scheduleReference);
    return "redirect:/nil-submission-summary-details";
  }
}
