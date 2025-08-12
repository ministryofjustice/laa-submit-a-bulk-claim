package uk.gov.justice.laa.bulkclaim.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.service.ClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.GetSubmission200ResponseClaimsInner;

/**
 * Controller for handling the import in progress page after a user has submitted a bulk claim.
 *
 * @author Jamie Briggs
 */
@Controller
@RequiredArgsConstructor
@SessionAttributes("bulkSubmissionId")
public class ImportInProgressController {

  private final ClaimsRestService claimsRestService;

  /**
   * Shows the import in progress page, and refreshes every 5 seconds. Redirects if the submission
   * is ready.
   *
   * @param model the Spring model.
   * @param bulkSubmissionId the bulk submission id session attribute.
   * @return the import in progress view or redirects to view submission.
   */
  @GetMapping("/import-in-progress")
  public String importInProgress(
      Model model, @ModelAttribute("bulkSubmissionId") UUID bulkSubmissionId) {

    GetSubmission200Response block = claimsRestService.getSubmission(bulkSubmissionId).block();

    // Check submission has claims otherwise they will be stuck in a loop on this page.
    List<GetSubmission200ResponseClaimsInner> claims = block.getClaims();
    if (claims == null || claims.isEmpty()) {
      throw new SubmitBulkClaimException(
          "No claims found for bulk submission: %s".formatted(bulkSubmissionId.toString()));
    }

    boolean fullyImported =
        claims.stream()
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
