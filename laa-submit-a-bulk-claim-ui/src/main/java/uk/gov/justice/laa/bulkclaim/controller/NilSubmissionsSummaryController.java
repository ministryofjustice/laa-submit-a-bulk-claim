package uk.gov.justice.laa.bulkclaim.controller;

import com.fasterxml.uuid.Generators;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
public class NilSubmissionsSummaryController {
  private final DataClaimsRestClient claimsRestService;
  private final FeatureFlagsConfig featureFlagsConfig;

  @GetMapping("/nil-submission-summary-details")
  public String getSummary() {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }

    return "pages/nil-submission-summary-details";
  }

  @PostMapping("/nil-submission-summary-details")
  public String postSummary(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form,
      @AuthenticationPrincipal OidcUser oidcUser) {

    SubmissionPost submissionPost =
        SubmissionPost.builder()
            .officeAccountNumber(form.getOffice())
            .numberOfClaims(0)
            .status(SubmissionStatus.READY_FOR_VALIDATION)
            .areaOfLaw(AreaOfLaw.valueOf(form.getAreaOfLaw()))
            .legalHelpSubmissionReference(form.getScheduleReference())
            .isNilSubmission(true)
            .submissionId(Generators.timeBasedEpochGenerator().generate())
            .submissionPeriod(form.getSubmissionPeriod())
            .providerUserId(oidcUser.getPreferredUsername())
            .createdByUserId("Submit-a-bulk-claim")
            .build();

    System.out.println("form: " + submissionPost);
    claimsRestService.createSubmission(submissionPost);

    return "redirect:/view-submission-detail-accepted";
  }
}
