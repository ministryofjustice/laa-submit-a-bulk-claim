package uk.gov.justice.laa.cwa.bulkupload.controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
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

/** Controller for handling the bulk upload requests. */
@Slf4j
@RequiredArgsConstructor
@Controller
public class BulkUploadController {

  private final VirusCheckService virusCheckService;
  private final CwaUploadService cwaUploadService;
  private final ProviderHelper providerHelper;

  @Value("${upload-max-file-size:10MB}")
  private String fileSizeLimit;

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
      log.error("Error connecting to CWA with message: {} ", e.getMessage());
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
  public String performUpload(
      @RequestParam("fileUpload") MultipartFile file,
      String provider,
      Model model,
      Principal principal) {
    long maxFileSize = DataSize.parse(fileSizeLimit).toBytes();
    Map<String, String> errors = new LinkedHashMap<>();

    if (!StringUtils.hasText(provider)) {
      errors.put("provider", "Please select a provider");
    }

    if (file.isEmpty()) {
      errors.put("fileUpload", "Please select a file to upload");
    }

    if (file.getSize() > maxFileSize) {
      errors.put("fileUpload", "File size must not exceed 10MB");
    }

    try {
      if (errors.isEmpty()) {
        virusCheckService.checkVirus(file);
      }
    } catch (Exception e) {
      log.error("Virus check failed with message: {}", e.getMessage());
      errors.put("fileUpload", "The file failed the virus scan. Please upload a clean file.");
    }

    if (!errors.isEmpty()) {
      return showErrorOnUpload(model, principal, provider, errors);
    }

    try {
      CwaUploadResponseDto cwaUploadResponseDto =
          cwaUploadService.uploadFile(file, provider, principal.getName().toUpperCase());
      model.addAttribute("fileId", cwaUploadResponseDto.getFileId());
      model.addAttribute("provider", provider);
      log.info("CWA Upload response fileId: {}", cwaUploadResponseDto.getFileId());
    } catch (Exception e) {
      log.error("Failed to upload file to CWA with message: {}", e.getMessage());
      errors.put("fileUpload", "An error occurred while uploading the file.");
      return showErrorOnUpload(model, principal, provider, errors);
    }

    return "pages/submission";
  }

  /**
   * Displays the error messages on the upload page.
   *
   * @param model the model to be populated with error messages
   * @param principal the authenticated user principal
   * @param provider the selected provider
   * @param errors the map of error messages
   * @return the upload page with error messages
   */
  private String showErrorOnUpload(
      Model model, Principal principal, String provider, Map<String, String> errors) {
    model.addAttribute("errors", errors);
    providerHelper.populateProviders(model, principal);
    model.addAttribute(
        "selectedProvider", !StringUtils.hasText(provider) ? 0 : Integer.parseInt(provider));
    return "pages/upload";
  }
}
