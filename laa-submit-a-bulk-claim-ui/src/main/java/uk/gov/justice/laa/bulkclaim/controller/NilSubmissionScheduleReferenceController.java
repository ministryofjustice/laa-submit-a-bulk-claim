package uk.gov.justice.laa.bulkclaim.controller;

import com.fasterxml.uuid.Generators;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionPost;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@Controller
@RequiredArgsConstructor
@SessionAttributes("nilSubmissionForm")
public class NilSubmissionScheduleReferenceController {

  private final DataClaimsRestClient claimsRestService;
  private final FeatureFlagsConfig featureFlagsConfig;

  @GetMapping("/nil-submission-reference")
  public String getPage(@ModelAttribute("nilSubmissionForm") NilSubmissionForm form, Model model) {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }
    //        Set<String> areasOfLaw =
    //                Arrays.stream(AreaOfLaw.values())
    //                        .map(Enum::name)
    //                        .collect(Collectors.toSet());
    //        model.addAttribute("areasOfLaw", areasOfLaw);

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
  public String postPage(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form,
      @RequestParam String scheduleReference,
      @AuthenticationPrincipal OidcUser oidcUser) {

    form.setScheduleReference(scheduleReference);

    SubmissionPost submissionPost =
        SubmissionPost.builder()
            .officeAccountNumber(form.getOffice())
            .numberOfClaims(0)
            .status(SubmissionStatus.READY_FOR_VALIDATION)
            .areaOfLaw(AreaOfLaw.valueOf(form.getAreaOfLaw()))
            .legalHelpSubmissionReference(form.getScheduleReference())
            .isNilSubmission(true)
            .submissionId(Generators.timeBasedEpochGenerator().generate())
            // format of period is wrong
            .submissionPeriod(form.getSubmissionPeriod())
            .providerUserId(oidcUser.getPreferredUsername())
            .createdByUserId("Submit-a-bulk-claim")
            .build();

    System.out.println("POST: " + submissionPost);
    claimsRestService.createSubmission(submissionPost);
    return "redirect:/nil-submission-reference";
  }
}
