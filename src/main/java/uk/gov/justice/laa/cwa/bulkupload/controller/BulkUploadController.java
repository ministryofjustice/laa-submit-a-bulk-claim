package uk.gov.justice.laa.cwa.bulkupload.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
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


    @Value("${upload-max-file-size:10MB}")
    private String fileSizeLimit;
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
            log.error("HTTP client error fetching providers from CWA with message: {} ", e.getMessage());
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return "pages/upload-forbidden";
            } else {
                return "error";
            }
        } catch (Exception e) {
            log.error("Error fetching providers from CWA with message: {} ", e.getMessage());
            return "error";
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
        long maxFileSize = DataSize.parse(fileSizeLimit).toBytes();

        if (!StringUtils.hasText(provider)) {
            return showErrorOnUpload(model, principal, provider, "Please select a provider");
        }
        if (file.isEmpty()) {
            return showErrorOnUpload(model, principal, provider, "Please select a file to upload");
        }
        if (file.getSize() > maxFileSize) {
            return showErrorOnUpload(model, principal, provider, "File size must not exceed 10MB");
        }

        try {
            virusCheckService.checkVirus(file);
        } catch (Exception e) {
            log.error("Virus check failed with message:{}", e.getMessage());
            return showErrorOnUpload(model, principal, provider, "The file failed the virus scan. Please upload a clean file.");
        }

        try {
            CwaUploadResponseDto cwaUploadResponseDto = cwaUploadService.uploadFile(file, provider, principal.getName().toUpperCase());
            model.addAttribute("fileId", cwaUploadResponseDto.getFileId());
            model.addAttribute("provider", provider);
            log.info("CwaUploadResponseDto :: {}", cwaUploadResponseDto.getFileId());
        } catch (Exception e) {
            log.error("Failed to upload file to CWA with  message:{}", e.getMessage());
            return showErrorOnUpload(model, principal, provider, "An error occurred while uploading the file.");
        }

        return "pages/submission";
    }

    /**
     * Handles errors during file upload.
     *
     * @param model        the model to be populated with error information
     * @param principal    the authenticated user
     * @param provider     the selected provider
     * @param errorMessage the error message to display
     * @return the upload page with error information
     */
    private String showErrorOnUpload(Model model, Principal principal, String provider, String errorMessage) {
        model.addAttribute("error", errorMessage);
        providerHelper.populateProviders(model, principal);
        model.addAttribute("provider", provider);
        return "pages/upload";
    }
}