package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.config.SessionConstants.BULK_SUBMISSION_ID;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * Controller which allows the user to view their submitted submissions and their errors if there
 * are any.
 *
 * @author Jamie Briggs
 */
@Controller
@SessionAttributes({BULK_SUBMISSION_ID})
public class SubmissionController {

  /**
   * Shows the view submitted submissions page.
   *
   * @param model the Spring model.
   * @param bulkSubmissionId the bulk submission id session attribute.
   * @return the view submitted submission page.
   */
  @GetMapping("/view-submission-summary")
  public String getSubmission(
      Model model, @ModelAttribute(BULK_SUBMISSION_ID) UUID bulkSubmissionId) {
    return "pages/view-submission-summary";
  }
}
