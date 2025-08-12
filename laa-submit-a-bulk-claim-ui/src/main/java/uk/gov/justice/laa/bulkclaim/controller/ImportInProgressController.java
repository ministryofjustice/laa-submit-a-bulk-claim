package uk.gov.justice.laa.bulkclaim.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.justice.laa.bulkclaim.service.ClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.GetSubmission200ResponseClaimsInner;

@Controller
@RequiredArgsConstructor
public class ImportInProgressController {

  private final ClaimsRestService claimsRestService;

  @GetMapping("/upload/{bulkSubmissionId}/import-in-progress")
  public String importInProgress(Model model, @PathVariable("bulkSubmissionId")
  UUID bulkSubmissionId) {

    GetSubmission200Response block = claimsRestService.getSubmission(bulkSubmissionId).block();
    // TODO: GET BULK SUBMISSION AS SESSION ATTRIBUTE
    model.addAttribute("statuses", block.getClaims().stream().map(
        GetSubmission200ResponseClaimsInner::getStatus).toList());
    model.addAttribute("shouldRefresh", true);
    return "pages/upload-in-progress";
  }

}
