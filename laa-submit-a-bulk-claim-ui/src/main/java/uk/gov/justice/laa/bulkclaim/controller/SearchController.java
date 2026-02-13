package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.bulkclaim.util.ThymeleafHrefUtils.ViewRequestContext;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
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
  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final String DEFAULT_SEARCH_PAGE_SORT = "createdOn,desc";

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
  public String search(
      Model model, SessionStatus sessionStatus, @AuthenticationPrincipal OidcUser oidcUser) {
    List<String> userOffices = oidcAttributeUtils.getUserOffices(oidcUser);
    // Only submissionStatus has to be set to "All" as default to select the default radio
    // option on the frontend.
    SubmissionsSearchForm submissionsSearchForm =
        new SubmissionsSearchForm(null, null, userOffices, "All");
    if (!model.containsAttribute(SUBMISSION_SEARCH_FORM)) {
      model.addAttribute(SUBMISSION_SEARCH_FORM, submissionsSearchForm);
    }
    model.addAttribute("userOffices", userOffices);
    sessionStatus.setComplete();
    UriComponentsBuilder redirectUrl =
        UriComponentsBuilder.fromPath("/submissions/search/results")
            .queryParam("page", DEFAULT_PAGE);

    String submissionPeriod = trimToNull(submissionsSearchForm.submissionPeriod());
    if (submissionPeriod != null) {
      redirectUrl.queryParam("submissionPeriod", submissionPeriod);
    }

    String areaOfLaw = trimToNull(submissionsSearchForm.areaOfLaw());
    if (areaOfLaw != null) {
      redirectUrl.queryParam("areaOfLaw", areaOfLaw);
    }

    List<String> offices = submissionsSearchForm.offices();
    if (offices != null) {
      redirectUrl.queryParam("offices", offices);
    }

    String submissionStatus = trimToNull(submissionsSearchForm.submissionStatus());
    if (submissionStatus != null) {
      redirectUrl.queryParam("submissionStatus", submissionStatus);
    } else {
      redirectUrl.queryParam("submissionStatus", "All");
    }

    return "redirect:" + redirectUrl.build().toUriString();
  }

  /**
   * Handles the submissions search form submissions.
   *
   * @param submissionsSearchForm dto holding form values
   * @param bindingResult binding results for validation errors
   * @param model view context model
   * @return redirect to search results when successful or back to the form if validation fails
   */
  @PostMapping("/submissions/search")
  public String handleSearch(
      @AuthenticationPrincipal OidcUser oidcUser,
      @Validated @ModelAttribute(SUBMISSION_SEARCH_FORM)
          SubmissionsSearchForm submissionsSearchForm,
      BindingResult bindingResult,
      Model model) {

    if (bindingResult.hasErrors()) {
      model.addAttribute(SUBMISSION_SEARCH_FORM, submissionsSearchForm);
      model.addAttribute(BindingResult.MODEL_KEY_PREFIX + SUBMISSION_SEARCH_FORM, bindingResult);
      List<String> userOffices = oidcAttributeUtils.getUserOffices(oidcUser);
      model.addAttribute("userOffices", userOffices);

      // TODO: How do I handle this whilst using a filter?
      return "pages/submissions-search";
    }

    UriComponentsBuilder redirectUrl =
        UriComponentsBuilder.fromPath("/submissions/search/results")
            .queryParam("page", DEFAULT_PAGE);

    String submissionPeriod = trimToNull(submissionsSearchForm.submissionPeriod());
    if (submissionPeriod != null) {
      redirectUrl.queryParam("submissionPeriod", submissionPeriod);
    }

    String areaOfLaw = trimToNull(submissionsSearchForm.areaOfLaw());
    if (areaOfLaw != null) {
      redirectUrl.queryParam("areaOfLaw", areaOfLaw);
    }

    List<String> offices = submissionsSearchForm.offices();
    if (offices != null) {
      redirectUrl.queryParam("offices", offices);
    }

    String submissionStatus = trimToNull(submissionsSearchForm.submissionStatus());
    if (submissionStatus != null) {
      redirectUrl.queryParam("submissionStatus", submissionStatus);
    } else {
      redirectUrl.queryParam("submissionStatus", "All");
    }

    return "redirect:" + redirectUrl.build().toUriString();
  }

  /**
   * Handles Submission page results.
   *
   * @param page requested page number
   * @param submissionPeriod submission period filter
   * @param model view context model
   * @param oidcUser authenticated user
   * @param sessionStatus session status for clearing session attributes
   * @param session http session for storing results
   * @return search results view
   */
  @GetMapping("/submissions/search/results")
  public String submissionsSearchResults(
      HttpServletRequest httpRequest,
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "submissionPeriod", required = false) String submissionPeriod,
      @RequestParam(value = "areaOfLaw", required = false) String areaOfLaw,
      @RequestParam(value = "offices", required = false) List<String> offices,
      @RequestParam(value = "submissionStatus", required = false) String submissionStatus,
      Model model,
      @AuthenticationPrincipal OidcUser oidcUser,
      SessionStatus sessionStatus,
      HttpSession session) {

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    httpRequest.getParameterMap().forEach((k, values) -> params.put(k, List.of(values)));

    ViewRequestContext viewRequestContext =
        new ViewRequestContext(httpRequest.getServletPath(), params);
    model.addAttribute("viewRequestContext", viewRequestContext);

    sessionStatus.setComplete();

    SubmissionsSearchForm submissionsSearchForm =
        new SubmissionsSearchForm(
            trimToNull(submissionPeriod), areaOfLaw, offices, submissionStatus);
    model.addAttribute(SUBMISSION_SEARCH_FORM, submissionsSearchForm);

    List<String> userOffices = oidcAttributeUtils.getUserOffices(oidcUser);
    model.addAttribute("userOffices", userOffices);

    try {
      // Remove any offices which don't appear in request param (user has selected these offices)
      // By doing it this way, if someone were to manipulate an office as a request param, the
      // manipulated value would not be used in the search against the API.
      List<String> officesToSearchFor =
          userOffices.stream().filter(submissionsSearchForm.offices()::contains).toList();
      SubmissionsResultSet submissionsResults =
          claimsRestService
              .search(
                  officesToSearchFor,
                  submissionsSearchForm.submissionPeriod(),
                  getAreaOfLaw(submissionsSearchForm),
                  getSubmissionStatus(submissionsSearchForm),
                  page,
                  DEFAULT_PAGE_SIZE,
                  DEFAULT_SEARCH_PAGE_SORT)
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

  private static AreaOfLaw getAreaOfLaw(SubmissionsSearchForm submissionsSearchForm) {
    if (Objects.isNull(submissionsSearchForm.areaOfLaw())) {
      return null;
    }
    try {
      return AreaOfLaw.fromValue(submissionsSearchForm.areaOfLaw().replace("_", " ").toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private static List<SubmissionStatus> getSubmissionStatus(
      SubmissionsSearchForm submissionsSearchForm) {
    if (Objects.isNull(submissionsSearchForm.submissionStatus())) {
      return null;
    }
    try {
      return Arrays.asList(SubmissionStatus.fromValue(submissionsSearchForm.submissionStatus()));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private LocalDate parseDate(String date) {
    if (!StringUtils.hasText(date)) {
      return null;
    }
    try {
      return LocalDate.parse(date.trim(), DateTimeFormatter.ofPattern("d/M/yyyy"));
    } catch (DateTimeParseException exception) {
      log.warn("Unable to parse submitted date '{}': {}", date, exception.getMessage());
      return null;
    }
  }

  private String trimToNull(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }
}
