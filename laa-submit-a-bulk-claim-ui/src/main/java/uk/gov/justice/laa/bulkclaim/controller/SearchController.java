package uk.gov.justice.laa.bulkclaim.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;
import uk.gov.justice.laa.bulkclaim.response.CwaUploadErrorResponseDto;
import uk.gov.justice.laa.bulkclaim.response.CwaUploadSummaryResponseDto;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;
import uk.gov.justice.laa.claims.model.Page;
import uk.gov.justice.laa.claims.model.SubmissionsResultSet;

/** Controller for handling search requests related to bulk uploads. */
@Slf4j
@RequiredArgsConstructor
@Controller
public class SearchController {

  private final DataClaimsRestClient claimsRestService;
  private final SubmissionSearchValidator submissionSearchValidator;

  @InitBinder("submissionsSearchForm")
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
    model.addAttribute("submissionsSearchForm", new SubmissionsSearchForm(null, null, null));

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
      @Validated @ModelAttribute("submissionsSearchForm")
          SubmissionsSearchForm submissionsSearchForm,
      BindingResult bindingResult,
      Model model,
      @AuthenticationPrincipal OidcUser oidcUser,
      final RedirectAttributes redirectAttributes) {

    String submissionId =
        StringUtils.hasText(submissionsSearchForm.submissionId())
            ? submissionsSearchForm.submissionId().trim()
            : null;

    LocalDate submittedDateFrom = submissionsSearchForm.submittedDateFrom();
    LocalDate submittedDateTo = submissionsSearchForm.submittedDateTo();

    if (bindingResult.hasErrors()) {
      model.addAttribute("errors", bindingResult.getFieldErrors());
      return "pages/submissions-search";
    }

    //  List<String> offices = oidcUser.getUserInfo().getClaim("provider");
    List<String> offices = List.of("1");

    try {
      SubmissionsResultSet response =
          claimsRestService
              .search(offices, submissionId, submittedDateFrom, submittedDateTo)
              .block();
      log.debug("Response from claims search: {}", response);
      redirectAttributes.addFlashAttribute("submissions", response);

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
      @ModelAttribute("submissions") SubmissionsResultSet submissionsResults,
      Model model,
      HttpServletRequest request) {

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

  /**
   * Handles the search form submission and retrieves upload summaries and errors.
   *
   * @param provider the selected provider
   * @param searchTerm the search term (file reference)
   * @param model the model to add attributes to
   * @param oidcUser the authenticated user principal
   * @return the name of the view to render
   */
  @PostMapping("/search")
  public String submitForm(
      String provider, String searchTerm, Model model, @AuthenticationPrincipal OidcUser oidcUser) {

    Map<String, String> errors = new LinkedHashMap<>();

    if (!StringUtils.hasText(provider)) {
      errors.put("provider", "Please select a provider");
    }

    if (!StringUtils.hasText(searchTerm) || searchTerm.length() > 10) {
      errors.put("searchTerm", "File reference must be between 1 to 10 characters long");
    }

    if (!errors.isEmpty()) {
      return handleErrors(model, oidcUser.getName(), provider, searchTerm, errors);
    }

    List<CwaUploadSummaryResponseDto> summary;
    try {
      // TODO: Get upload summary via Claims API
      summary = Collections.emptyList();
      // cwaUploadService.getUploadSummary(searchTerm, oidcUser.getName(), provider);
      model.addAttribute("summary", summary);
    } catch (Exception e) {
      log.error("Error retrieving upload summary: {}", e.getMessage());
      errors.put("search", "Search failed please try again.");
      return handleErrors(model, oidcUser.getName(), provider, searchTerm, errors);
    }

    try {
      // TODO: Get upload errors via Claims API
      List<CwaUploadErrorResponseDto> uploadErrors = Collections.emptyList();
      //    cwaUploadService.getUploadErrors(searchTerm, oidcUser.getName(), provider);
      model.addAttribute("errors", uploadErrors);
    } catch (Exception e) {
      log.error("Error retrieving upload errors: {}", e.getMessage());
      errors.put("search", "Search failed please try again.");
      return handleErrors(model, oidcUser.getName(), provider, searchTerm, errors);
    }

    return "pages/submission-results";
  }

  /**
   * Handles errors during the search process and prepares the model for rendering the upload page.
   *
   * @param model the model to add attributes to
   * @param username the authenticated user principal
   * @param provider the selected provider
   * @param searchTerm the search term (file reference)
   * @param errors a map of error messages
   * @return the name of the view to render
   */
  private String handleErrors(
      Model model,
      String username,
      String provider,
      String searchTerm,
      Map<String, String> errors) {
    model.addAttribute("errors", errors);
    if (StringUtils.hasText(provider)) {
      try {
        model.addAttribute("selectedProvider", Integer.parseInt(provider));
      } catch (NumberFormatException ignored) {
        model.addAttribute("selectedProvider", 0);
      }
    }

    if (StringUtils.hasText(searchTerm)) {
      model.addAttribute("searchTerm", searchTerm);
    }
    model.addAttribute("tab", "search");

    return "pages/upload";
  }
}
