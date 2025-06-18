package uk.gov.justice.laa.cwa.bulkupload.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.justice.laa.cwa.bulkupload.helper.ProviderHelper;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadErrorResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadSummaryResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.service.CwaUploadService;

import java.security.Principal;
import java.util.List;

/**
 * Controller for handling the submission of bulk upload.
 */

@Slf4j
@RequiredArgsConstructor
@Controller
public class SearchController {
    private final CwaUploadService cwaUploadService;
    private final ProviderHelper providerHelper;

    /**
     * Handles the search form submission.
     * This method processes the search term and provider, retrieves the upload summary and errors,
     * and returns the results page.
     *
     * @param provider   the selected provider.
     * @param searchTerm the file reference to search.
     * @param model      the model to be populated with results.
     * @return the results page or an error page if validation fails.
     */
    @PostMapping("/search")
    public String submitForm(String provider, String searchTerm, Model model, Principal principal) {

        if (!StringUtils.hasText(provider)) {
            model.addAttribute("error", "Please select a provider");
            providerHelper.populateProviders(model, principal);
            return "pages/upload";
        }
        if (!StringUtils.hasText(searchTerm) || searchTerm.length() > 10) {
            model.addAttribute("error", "File reference must be between 1 to 10 characters long");
            providerHelper.populateProviders(model, principal);
            return "pages/upload";
        }
        List<CwaUploadSummaryResponseDto> summary;
        try {
            summary = cwaUploadService.getUploadSummary(searchTerm, principal.getName(), provider);
            model.addAttribute("summary", summary);
        } catch (Exception e) {
            log.error("Error retrieving upload summary: {}", e.getMessage());
            return handleError(model, principal, "Search failed please try again.");
        }

        try {
            List<CwaUploadErrorResponseDto> errors = cwaUploadService.getUploadErrors(searchTerm, principal.getName().toUpperCase(), provider);
            model.addAttribute("errors", errors);
        } catch (Exception e) {
            log.error("Error retrieving upload errors: {}", e.getMessage());
            return handleError(model, principal, "Search failed please try again.");
        }


        return "pages/submission-results"; // Redirect to a success page after submission
    }

    /**
     * Handles errors during search submission.
     *
     * @param model        the model to be populated with error information
     * @param principal    the authenticated user
     * @param errorMessage the error message to display
     * @return the upload page with error information
     */
    private String handleError(Model model, Principal principal, String errorMessage) {
        model.addAttribute("error", errorMessage);
        providerHelper.populateProviders(model, principal);
        return "pages/upload";
    }
}

