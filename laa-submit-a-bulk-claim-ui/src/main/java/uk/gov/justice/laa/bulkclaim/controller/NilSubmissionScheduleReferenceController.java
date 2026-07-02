package uk.gov.justice.laa.bulkclaim.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
@SessionAttributes("nilSubmissionForm")
public class NilSubmissionScheduleReferenceController {

  private final FeatureFlagsConfig featureFlagsConfig;
  private final MessageSource messageSource;
  @GetMapping("/nil-submission-reference")
  public String getReference(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form, Model model) {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }

    String label =
        switch (form.getAreaOfLaw()) {
          case "LEGAL_HELP" -> messageSource.getMessage("nilSubmission.civil.reference", null, Locale.UK);
          case "MEDIATION" -> messageSource.getMessage("nilSubmission.mediation.reference", null, Locale.UK);
          case "CRIME_LOWER" -> messageSource.getMessage("nilSubmission.crime.reference", null, Locale.UK);
          default -> throw new IllegalStateException("Unexpected value: " + form.getAreaOfLaw());
        };

      String exampleText =
              switch (form.getAreaOfLaw()) {
                  case "LEGAL_HELP" -> messageSource.getMessage("nilSubmission.civil.example", null, Locale.UK);
                  case "MEDIATION" -> messageSource.getMessage("nilSubmission.mediation.example", null, Locale.UK);
                  case "CRIME_LOWER" -> messageSource.getMessage("nilSubmission.crime.example", null, Locale.UK);
                  default -> throw new IllegalStateException("Unexpected value: " + form.getAreaOfLaw());
              };

    model.addAttribute("referenceLabel", label);
    model.addAttribute("exampleText", exampleText);

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
    form.setScheduleReference(scheduleReference);
    return "redirect:/nil-submission-summary-details";
  }
}
