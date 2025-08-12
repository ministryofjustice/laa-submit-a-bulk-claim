package uk.gov.justice.laa.bulkclaim.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.service.ClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.GetSubmission200ResponseClaimsInner;

@Controller
@RequiredArgsConstructor
@SessionAttributes("bulkSubmissionId")
public class ImportInProgressController {

  private final ClaimsRestService claimsRestService;

  @GetMapping("/upload/import-in-progress")
  public String importInProgress(
      Model model, @ModelAttribute("bulkSubmissionId") UUID bulkSubmissionId) {

    GetSubmission200Response block = claimsRestService.getSubmission(bulkSubmissionId).block();

    boolean fullyImported =
        block.getClaims().stream()
            .map(GetSubmission200ResponseClaimsInner::getStatus)
            .allMatch("READY"::equals);
    if (fullyImported) {
      // TODO: Redirect to imported page CCMSPUI-788
      return "redirect:/";
    }
    model.addAttribute("shouldRefresh", true);
    return "pages/upload-in-progress";
  }
}
