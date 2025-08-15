package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.config.SessionConstants.SUBMISSION_ID;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * Controller for handling viewing a submission.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({SUBMISSION_ID})
public class SubmissionDetailController {

  /**
   * Gets the submission reference, stores it in the session and redirects to the view submission.
   *
   * @param submissionReference the submission reference
   * @param httpSession the http session
   * @return the redirect to view a submission detail
   */
  @GetMapping("/submission/{submissionReference}")
  public String getSubmission(
      @PathVariable("submissionReference") UUID submissionReference, HttpSession httpSession) {
    httpSession.setAttribute(SUBMISSION_ID, submissionReference);
    return "redirect:/view-submission-detail";
  }

  /**
   * Views the submission detail page.
   *
   * @param model the spring model
   * @param submissionId the submission id in the session
   * @return the view submission detail page
   */
  @GetMapping("/view-submission-detail")
  public String getSubmissionDetail(Model model, @ModelAttribute(SUBMISSION_ID) UUID submissionId) {
    return "pages/view-submission-detail";
  }
}
