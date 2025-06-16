package uk.gov.justice.laa.cwa.bulkupload.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadErrorResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadSummaryResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.ValidateResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.service.CwaUploadService;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Controller for handling the submission of bulk upload.
 */

@Slf4j
@RequiredArgsConstructor
@Controller
public class SubmissionController {

    @Value("${cwa-api.timeout}")
    private int cwaApiTimeout;
    private final CwaUploadService cwaUploadService;

    /**
     * Handles the submission of a file for bulk upload.
     * This method processes the file submission, validates it, and returns the results.
     *
     * @param fileId   the ID of the file to be submitted.
     * @param provider the provider to be used for the submission.
     * @param model    the model to be populated with providers and error messages.
     * @return the submission results page or an error page if validation fails.
     */
    @PostMapping("/submit")
    public String submitFile(String fileId, String provider, Model model, Principal principal) {
        // This method will handle the form submission logic
        // For now, we just log the submission and return a success view
        ValidateResponseDto validateResponseDto = null;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<ValidateResponseDto> future = executor.submit(() -> cwaUploadService.processSubmission(fileId, principal.getName().toUpperCase(),
                    provider));
            validateResponseDto = future.get(cwaApiTimeout, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // Handle timeout
            model.addAttribute("fileId", fileId);
            return "pages/submission-timeout";
        } catch (Exception e) {
            // Handle other exceptions
            model.addAttribute("error", "An error occurred while processing the submission");
            return "pages/submission-failure";
        } finally {
            executor.shutdown();
        }
        List<CwaUploadSummaryResponseDto> summary = cwaUploadService.getUploadSummary(fileId, principal.getName(), provider);
        model.addAttribute("summary", summary);
        if (validateResponseDto == null || !"success".equalsIgnoreCase(validateResponseDto.getStatus())) {
            List<CwaUploadErrorResponseDto> errors = cwaUploadService.getUploadErrors(fileId, principal.getName().toUpperCase(), provider);
            log.error("Validation failed: {}", validateResponseDto.getMessage());
            model.addAttribute("errors", errors);
        }
        return "pages/submission-results";
    }

}

