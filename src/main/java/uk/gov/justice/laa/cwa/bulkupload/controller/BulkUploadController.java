package uk.gov.justice.laa.cwa.bulkupload.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.laa.cwa.bulkupload.helper.ProviderHelper;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.service.CwaUploadService;
import uk.gov.justice.laa.cwa.bulkupload.service.VirusCheckService;

import java.security.Principal;

/**
 * Controller for handling the bulk upload requests.
 */
@Slf4j
@RequiredArgsConstructor
@Controller
public class BulkUploadController {

    private final VirusCheckService virusCheckService;
    private final CwaUploadService cwaUploadService;
    private final ProviderHelper providerHelper;

    /**
     * Renders the upload page.
     *
     * @return the upload page
     */
    @GetMapping("/")
    public String showUploadPage(Model model, Principal principal) {
        try {
            providerHelper.populateProviders(model, principal);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.warn("403 Forbidden when fetching providers");
                return "pages/upload-forbidden";
            } else {
                log.error("Error fetching providers", e);
                model.addAttribute("error", "An error occurred while fetching providers.");
                return "pages/upload-error";
            }
        } catch (Exception e) {
            log.error("Unexpected error fetching providers", e);
            model.addAttribute("error", "An unexpected error occurred while fetching providers.");
            return "pages/upload-error";
        }
        return "pages/upload";
    }

    /**
     * Performs a bulk uploaded for the given file.
     *
     * @param file the file to be uploaded
     * @return the upload results page
     */
    @PostMapping("/upload")
    public String performUpload(@RequestParam("fileUpload") MultipartFile file, String provider, Model model, Principal principal) {
        if (!StringUtils.hasText(provider)) {
            model.addAttribute("error", "Please select a provider");
            providerHelper.populateProviders(model, principal);
            return "pages/upload";
        }
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select a file to upload");
            providerHelper.populateProviders(model, principal);
            return "pages/upload";
        }

        try {
            virusCheckService.checkVirus(file);
            CwaUploadResponseDto cwaUploadResponseDto = cwaUploadService.uploadFile(file, provider, principal.getName().toUpperCase());
            model.addAttribute("fileId", cwaUploadResponseDto.getFileId());
            model.addAttribute("provider", provider);
            log.info("CwaUploadResponseDto :: {}", cwaUploadResponseDto.getFileId());
        } catch (Exception e) {
            log.error("Exception", e);
            model.addAttribute("error", "An error occurred while uploading the file");
            return "pages/upload";
        }

        return "pages/submission";
    }
}
