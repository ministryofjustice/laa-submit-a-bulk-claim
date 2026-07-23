package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter.SUCCEEDED;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Objects;
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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionSearchResultRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchQuery;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchSortField;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.util.PaginationLinksBuilder;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@Slf4j
@RequiredArgsConstructor
@Controller
@SessionAttributes({SUBMISSION_ID, CLAIM_ID})
public class SearchController {

  private final DataClaimsRestClient claimsRestService;
  private final SubmissionSearchValidator submissionSearchValidator;
  private final PaginationUtil paginationUtil;
  private final OidcAttributeUtils oidcAttributeUtils;
  private final SubmissionPeriodUtil submissionPeriodUtil;
  private final PaginationLinksBuilder paginationLinksBuilder;

  public static final String SUBMISSION_SEARCH_QUERY = "submissionSearchQuery";

  @InitBinder(SUBMISSION_SEARCH_QUERY)
  void initSubmissionSearchValidator(WebDataBinder binder) {
    binder.addValidators(submissionSearchValidator);
  }

  @GetMapping("/submissions/search")
  public String search(
      Model model, SessionStatus sessionStatus, @AuthenticationPrincipal OidcUser oidcUser) {
    List<String> userOffices = oidcAttributeUtils.getUserOffices(oidcUser);
    if (!model.containsAttribute(SUBMISSION_SEARCH_QUERY)) {
      // Only submissionStatuses has to be set to "All" as default to select the default radio
      // option on the frontend.
      model.addAttribute(
          SUBMISSION_SEARCH_QUERY,
          SubmissionSearchQuery.builder()
              .offices(userOffices)
              .submissionStatuses(SUCCEEDED)
              .build());
    }
    model.addAttribute("userOffices", userOffices);
    sessionStatus.setComplete();
    return "pages/submissions-search";
  }

  @PostMapping("/submissions/search")
  public String handleSearch(
      @AuthenticationPrincipal OidcUser oidcUser,
      @Validated @ModelAttribute(SUBMISSION_SEARCH_QUERY)
          SubmissionSearchQuery submissionSearchQuery,
      BindingResult bindingResult,
      Model model) {

    if (bindingResult.hasErrors()) {
      model.addAttribute(SUBMISSION_SEARCH_QUERY, submissionSearchQuery);
      model.addAttribute(BindingResult.MODEL_KEY_PREFIX + SUBMISSION_SEARCH_QUERY, bindingResult);
      List<String> userOffices = oidcAttributeUtils.getUserOffices(oidcUser);
      model.addAttribute("userOffices", userOffices);

      return "pages/submissions-search";
    }

    return "redirect:" + SubmissionSearchQuery.from(submissionSearchQuery).getRedirectUrl();
  }

  /**
   * Handles Submission page results. Also handles global search when user clicks on column name on
   * the search results screen.
   */
  @GetMapping("/submissions/search/results")
  public String submissionsSearchResults(
      @Validated @ModelAttribute(SUBMISSION_SEARCH_QUERY)
          SubmissionSearchQuery submissionSearchQuery,
      Model model,
      @AuthenticationPrincipal OidcUser oidcUser,
      SessionStatus sessionStatus,
      HttpSession session) {

    sessionStatus.setComplete();

    model.addAttribute(SUBMISSION_SEARCH_QUERY, submissionSearchQuery);
    model.addAttribute("SubmissionSearchSortField", SubmissionSearchSortField.class);

    List<String> userOffices = oidcAttributeUtils.getUserOffices(oidcUser);
    model.addAttribute("userOffices", userOffices);
    // If both lists differ in size, user has changed what office to filter by and different by
    // default so show this change to the user
    model.addAttribute(
        "shouldOpenOfficeDetails",
        submissionSearchQuery.getOffices() == null
            || submissionSearchQuery.getOffices().size() != userOffices.size());

    try {
      // Remove any offices which don't appear in request param (user has selected these offices)
      // By doing it this way, if someone were to manipulate an office as a request param, the
      // manipulated value would not be used in the search against the API.
      List<String> officesToSearchFor =
          userOffices.stream().filter(submissionSearchQuery.getOffices()::contains).toList();
      SubmissionsResultSet submissionsResults =
          claimsRestService
              .search(
                  officesToSearchFor,
                  trimToNull(submissionSearchQuery.getSubmissionPeriod()),
                  submissionSearchQuery.getAreaOfLaw(),
                  getSubmissionStatus(submissionSearchQuery),
                  submissionSearchQuery.getPage(),
                  submissionSearchQuery.getSize(),
                  Objects.toString(submissionSearchQuery.getSort(), null))
              .block();

      Page pagination =
          paginationUtil.fromSubmissionsResultSet(
              submissionsResults, submissionSearchQuery.getPage(), submissionSearchQuery.getSize());
      model.addAttribute("pagination", pagination);
      model.addAttribute("submissions", submissionsResults);
      model.addAttribute("submissionRows", toSubmissionRows(submissionsResults));
      model.addAttribute(
          "searchPaginationLinks",
          paginationLinksBuilder.build(
              "/submissions/search/results",
              pagination,
              "page",
              "submissionPeriod",
              submissionSearchQuery.getSubmissionPeriod(),
              "areaOfLaw",
              submissionSearchQuery.getAreaOfLaw(),
              "offices",
              submissionSearchQuery.getOffices(),
              "submissionStatuses",
              submissionSearchQuery.getSubmissionStatuses(),
              "sort",
              submissionSearchQuery.getSort().toString()));
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

  private static List<SubmissionStatus> getSubmissionStatus(
      SubmissionSearchQuery submissionSearchQuery) {
    return Objects.isNull(submissionSearchQuery.getSubmissionStatuses())
        ? null
        : submissionSearchQuery.getSubmissionStatuses().getStatuses();
  }

  private String trimToNull(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private List<SubmissionSearchResultRow> toSubmissionRows(
      SubmissionsResultSet submissionsResults) {
    if (submissionsResults == null || submissionsResults.getContent() == null) {
      return List.of();
    }

    return submissionsResults.getContent().stream().map(this::toSubmissionRow).toList();
  }

  private SubmissionSearchResultRow toSubmissionRow(SubmissionBase submission) {
    return new SubmissionSearchResultRow(
        submission,
        submissionPeriodUtil.getSubmissionPeriod(submission),
        submissionPeriodUtil.getSortOrderFromSubmissionPeriod(submission));
  }
}
