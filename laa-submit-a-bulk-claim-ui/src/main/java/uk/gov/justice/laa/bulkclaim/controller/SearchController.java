package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

/** Controller for handling search requests related to bulk uploads. */
@Slf4j
@RequiredArgsConstructor
@Controller
@SessionAttributes({SUBMISSION_ID, CLAIM_ID})
public class SearchController {

  private final DataClaimsRestClient claimsRestService;
  private final SubmissionSearchValidator submissionSearchValidator;
  private final PaginationUtil paginationUtil;
  private final OidcAttributeUtils oidcAttributeUtils;

  public static final String SUBMISSION_SEARCH_FORM = "submissionsSearchForm";
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("d/M/yyyy");
  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_PAGE_SIZE = 10;

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
  public String search(Model model, SessionStatus sessionStatus) {
    if (!model.containsAttribute(SUBMISSION_SEARCH_FORM)) {
      model.addAttribute(SUBMISSION_SEARCH_FORM, new SubmissionsSearchForm(null, null, null));
    }
    sessionStatus.setComplete();
    return "pages/submissions-search";
  }

  /**
   * Handles the submissions search form submissions.
   *
   * @param submissionsSearchForm dto holding form values
   * @param bindingResult binding results for validation errors
   * @param redirectAttributes redirect attributes for flash scoped values
   * @return redirect to search results when successful or back to the form if validation fails
   */
  @PostMapping("/submissions/search")
  public String handleSearch(
      @Validated @ModelAttribute(SUBMISSION_SEARCH_FORM)
          SubmissionsSearchForm submissionsSearchForm,
      BindingResult bindingResult,
      final RedirectAttributes redirectAttributes) {

    String submissionId = trimToNull(submissionsSearchForm.submissionId());

    if (bindingResult.hasErrors()) {
      // Store errors and form object in RedirectAttributes
      redirectAttributes.addFlashAttribute(
          "org.springframework.validation.BindingResult.submissionsSearchForm", bindingResult);
      redirectAttributes.addFlashAttribute(SUBMISSION_SEARCH_FORM, submissionsSearchForm);
      return "redirect:/submissions/search";
    }

    String submittedDateFrom = trimToNull(submissionsSearchForm.submittedDateFrom());
    String submittedDateTo = trimToNull(submissionsSearchForm.submittedDateTo());

    UriComponentsBuilder redirectUrl =
        UriComponentsBuilder.fromPath("/submissions/search/results")
            .queryParam("page", DEFAULT_PAGE);

    if (submissionId != null) {
      redirectUrl.queryParam("submissionId", submissionId);
    }
    if (submittedDateFrom != null) {
      redirectUrl.queryParam("submittedDateFrom", submittedDateFrom);
    }
    if (submittedDateTo != null) {
      redirectUrl.queryParam("submittedDateTo", submittedDateTo);
    }

    return "redirect:" + redirectUrl.build().toUriString();
  }

  /**
   * Handles Submission page results.
   *
   * @param page requested page number
   * @param submissionId submission id filter
   * @param submittedDateFrom submitted date from filter
   * @param submittedDateTo submitted date to filter
   * @param model view context model
   * @param oidcUser authenticated user
   * @param sessionStatus session status for clearing session attributes
   * @param session http session for storing results
   * @return search results view
   */
  @GetMapping("/submissions/search/results")
  public String submissionsSearchResults(
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "submissionId", required = false) String submissionId,
      @RequestParam(value = "submittedDateFrom", required = false) String submittedDateFrom,
      @RequestParam(value = "submittedDateTo", required = false) String submittedDateTo,
      Model model,
      @AuthenticationPrincipal OidcUser oidcUser,
      SessionStatus sessionStatus,
      HttpSession session) {

    sessionStatus.setComplete();

    SubmissionsSearchForm submissionsSearchForm =
        new SubmissionsSearchForm(
            trimToNull(submissionId), trimToNull(submittedDateFrom), trimToNull(submittedDateTo));
    model.addAttribute(SUBMISSION_SEARCH_FORM, submissionsSearchForm);

    LocalDate submittedDateFromParsed = parseDate(submissionsSearchForm.submittedDateFrom());
    LocalDate submittedDateToParsed = parseDate(submissionsSearchForm.submittedDateTo());

    List<String> offices = oidcAttributeUtils.getUserOffices(oidcUser);

    try {
      SubmissionsResultSet submissionsResults =
          claimsRestService
              .search(
                  offices,
                  submissionsSearchForm.submissionId(),
                  submittedDateFromParsed,
                  submittedDateToParsed,
                  page,
                  DEFAULT_PAGE_SIZE)
              .block();

      Page pagination =
          paginationUtil.fromSubmissionsResultSet(submissionsResults, page, DEFAULT_PAGE_SIZE);
      model.addAttribute("pagination", pagination);
      model.addAttribute("submissions", submissionsResults);
      session.setAttribute("submissions", submissionsResults);

      return "pages/submissions-search-results";
    } catch (HttpClientErrorException e) {
      log.error("HTTP client error fetching submissions: {} ", e.getMessage());
      return "error";
    } catch (Exception e) {
      log.error("Error connecting to Claims API with message: {} ", e.getMessage());
      return "error";
    }
  }

  private LocalDate parseDate(String date) {
    if (!StringUtils.hasText(date)) {
      return null;
    }
    try {
      return LocalDate.parse(date.trim(), DATE_TIME_FORMATTER);
    } catch (DateTimeParseException exception) {
      log.warn("Unable to parse submitted date '{}': {}", date, exception.getMessage());
      return null;
    }
  }

  private String trimToNull(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }
}
