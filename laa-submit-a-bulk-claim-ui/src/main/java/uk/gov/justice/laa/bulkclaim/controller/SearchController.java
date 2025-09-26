package uk.gov.justice.laa.bulkclaim.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

/** Controller for handling search requests related to bulk uploads. */
@Slf4j
@RequiredArgsConstructor
@Controller
public class SearchController {

  private final DataClaimsRestClient claimsRestService;
  private final SubmissionSearchValidator submissionSearchValidator;

  public static final String SUBMISSION_SEARCH_FORM = "submissionsSearchForm";

  @InitBinder(SUBMISSION_SEARCH_FORM)
  void initSubmissionSearchValidator(WebDataBinder binder) {
    binder.addValidators(submissionSearchValidator);
  }

  /**
   * Handles rendering the search form for submissions.
   *
   * @return the search form page template
   */
  @GetMapping("/submissions/search")
  public String search(Model model) {
    if (!model.containsAttribute(SUBMISSION_SEARCH_FORM)) {
      model.addAttribute(SUBMISSION_SEARCH_FORM, new SubmissionsSearchForm(null, null, null));
    }

    return "pages/submissions-search";
  }

  /**
   * Handles the submissions search form submissions.
   *
   * @param submissionsSearchForm dto holding form values
   * @param model view model
   * @param oidcUser currently authenticated user
   * @return search results view
   */
  @PostMapping("/submissions/search")
  public String handleSearch(
      Model model,
      @Validated @ModelAttribute(SUBMISSION_SEARCH_FORM)
          SubmissionsSearchForm submissionsSearchForm,
      BindingResult bindingResult,
      @AuthenticationPrincipal OidcUser oidcUser,
      final RedirectAttributes redirectAttributes) {

    String submissionId =
        StringUtils.hasText(submissionsSearchForm.submissionId())
            ? submissionsSearchForm.submissionId().trim()
            : null;

    if (bindingResult.hasErrors()) {
      // Store errors and form object in RedirectAttributes
      redirectAttributes.addFlashAttribute(
          "org.springframework.validation.BindingResult.submissionsSearchForm", bindingResult);
      redirectAttributes.addFlashAttribute(SUBMISSION_SEARCH_FORM, submissionsSearchForm);
      return "redirect:/submissions/search";
    }

    LocalDate submittedDateFrom = null;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d/M/yyyy");
    if (StringUtils.hasText(submissionsSearchForm.submittedDateFrom())) {
      submittedDateFrom =
          LocalDate.parse(submissionsSearchForm.submittedDateFrom(), dateTimeFormatter);
    }
    LocalDate submittedDateTo = null;
    if (StringUtils.hasText(submissionsSearchForm.submittedDateTo())) {
      submittedDateTo = LocalDate.parse(submissionsSearchForm.submittedDateTo(), dateTimeFormatter);
    }

    // TODO: Enable getting office ID from OIDC
    // List<String> offices = oidcUser.getUserInfo().getClaim("provider");
    List<String> offices = List.of("1");

    try {
      SubmissionsResultSet response =
          claimsRestService
              .search(offices, submissionId, submittedDateFrom, submittedDateTo)
              .block();
      log.debug("Response from claims search: {}", response);
      redirectAttributes.addFlashAttribute("submissions", response);
      redirectAttributes.addFlashAttribute(SUBMISSION_SEARCH_FORM, submissionsSearchForm);

      return "redirect:/submissions/search/results";
    } catch (HttpClientErrorException e) {
      log.error("HTTP client error fetching submissions: {} ", e.getMessage());

      return "error";
    } catch (Exception e) {
      log.error("Error connecting to Claims API with message: {} ", e.getMessage());
      return "error";
    }
  }

  /**
   * Handles Submission page results.
   *
   * @param submissionsResults submission search results.
   * @param model view context model.
   * @param request HttpServletRequest object.
   * @return search results view.
   */
  @GetMapping("/submissions/search/results")
  public String submissionsSearchResults(
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "size", defaultValue = "10") final int size,
      @ModelAttribute(SUBMISSION_SEARCH_FORM) SubmissionsSearchForm submissionsSearchForm,
      @ModelAttribute("submissions") SubmissionsResultSet submissionsResults,
      Model model,
      HttpServletRequest request) {

    model.addAttribute(SUBMISSION_SEARCH_FORM, submissionsSearchForm);

    Page pagination = new Page();

    pagination.setNumber(
        submissionsResults != null ? submissionsResults.getNumber() : (Integer) page);
    pagination.setSize(submissionsResults != null ? submissionsResults.getSize() : (Integer) size);
    pagination.setTotalPages(
        submissionsResults != null ? submissionsResults.getTotalPages() : null);
    pagination.setTotalElements(
        submissionsResults != null ? submissionsResults.getTotalElements() : null);

    model.addAttribute("pagination", pagination);
    log.debug("Adding currentUrl to model: {}", request.getRequestURL());

    return "pages/submissions-search-results";
  }
}
