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
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionPage;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager;

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

    NilSubmissionSessionManager.nilSubmissionCleanseSession(
        form, NilSubmissionPage.SCHEDULE_REFERENCE);

    String label =
        switch (form.getAreaOfLaw()) {
          case "CRIME LOWER" -> "Crime schedule reference";
          case "MEDIATION" -> "Mediation schedule reference";
          case "LEGAL HELP" -> "Legal help schedule reference";
          default -> "Reference";
        };

    model.addAttribute("referenceLabel", label);

    return "pages/nil-submission-reference";
  }

  @PostMapping("/nil-submission-reference")
  public String postReference(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form,
      @RequestParam String scheduleReference) {

    form.setScheduleReference(scheduleReference);
    return "redirect:/nil-submission-summary-details";
  }
}
