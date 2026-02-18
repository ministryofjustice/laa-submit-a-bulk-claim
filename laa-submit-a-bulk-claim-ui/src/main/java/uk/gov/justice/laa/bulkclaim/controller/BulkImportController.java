package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tools.jackson.databind.ObjectMapper;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.FileUploadForm;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileValidator;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileVirusValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.CreateBulkSubmission201Response;
import uk.gov.laa.springboot.exception.ApplicationException;
import uk.gov.laa.springboot.metrics.aspect.annotations.HistogramMetric;
import uk.gov.laa.springboot.metrics.aspect.annotations.ValueCaptureStrategy;

/** Controller for handling the bulk upload requests. */
@Slf4j
@RequiredArgsConstructor
@Controller
public class BulkImportController {

  public static final String FILE_UPLOAD_FORM_MODEL_ATTR = "fileUploadForm";

  private final BulkImportFileValidator bulkImportFileValidator;
  private final BulkImportFileVirusValidator bulkImportFileVirusValidator;
  private final DataClaimsRestClient dataClaimsRestClient;
  private final OidcAttributeUtils oidcAttributeUtils;
  private final ObjectMapper objectMapper;

  /**
   * Renders the upload page.
   *
   * @param model the model to be populated with data
   * @return the upload page
   */
  @GetMapping("/upload")
  public String showUploadPage(Model model, SessionStatus sessionStatus) {

    // Clear the session due to new submission
    sessionStatus.setComplete();

    // Always ensure there's a form object in the model if not already present
    if (!model.containsAttribute(FILE_UPLOAD_FORM_MODEL_ATTR)) {
      model.addAttribute(FILE_UPLOAD_FORM_MODEL_ATTR, new FileUploadForm());
    }

    return "pages/upload";
  }

  /**
   * Performs a bulk upload for the given file.
   *
   * @param fileUploadForm the file to be uploaded
   * @param oidcUser the authenticated user principal
   * @return the submission page
   */
  @PostMapping("/upload")
  public String performUpload(
      @ModelAttribute(FILE_UPLOAD_FORM_MODEL_ATTR) FileUploadForm fileUploadForm,
      BindingResult bindingResult,
      @AuthenticationPrincipal OidcUser oidcUser,
      Model model,
      RedirectAttributes redirectAttributes) {

    bulkImportFileValidator.validate(fileUploadForm, bindingResult);
    if (bindingResult.hasErrors()) {
      return showErrorOnUpload(fileUploadForm, bindingResult, model);
    }

    bulkImportFileVirusValidator.validate(fileUploadForm, bindingResult);
    if (bindingResult.hasErrors()) {
      return showErrorOnUpload(fileUploadForm, bindingResult, model);
    }

    try {
      ResponseEntity<CreateBulkSubmission201Response> responseEntity =
          dataClaimsRestClient
              .upload(
                  fileUploadForm.getFile(),
                  oidcUser.getPreferredUsername(),
                  oidcAttributeUtils.getUserOffices(oidcUser))
              .block();
      CreateBulkSubmission201Response bulkSubmissionResponse = responseEntity.getBody();
      log.info(
          "Claims API Upload response bulk submission UUID: {}",
          bulkSubmissionResponse.getBulkSubmissionId());
      redirectAttributes.addFlashAttribute(
          SUBMISSION_ID, bulkSubmissionResponse.getSubmissionIds().getFirst());

      double fileSize = uploadFile(fileUploadForm, oidcUser, redirectAttributes);
      log.info("File uploaded successfully, size: {} bytes", fileSize);
      return "redirect:/upload-is-being-checked";
    } catch (WebClientResponseException e) {
      try {
        ApplicationException error =
            objectMapper.readValue(e.getResponseBodyAsString(), ApplicationException.class);

        log.error("API upload failed: {}", error.getErrorMessage());
        bindingResult.rejectValue("file", "api.error", error.getErrorMessage());
      } catch (Exception parseEx) {
        log.error("Failed to upload file to Claims API with message: {}", e.getMessage());
        bindingResult.reject("bulkImport.validation.uploadFailed");
      }

      return showErrorOnUpload(fileUploadForm, bindingResult, model);

    } catch (Exception e) {
      log.error("Failed to upload file to Claims API with message: {}", e.getMessage());
      bindingResult.reject("bulkImport.validation.uploadFailed");
      return showErrorOnUpload(fileUploadForm, bindingResult, model);
    }
  }

  /**
   * Redirects back to the upload page with the errors.
   *
   * @param bindingResult binding result of errors
   * @return redirect back to upload page
   */
  private String showErrorOnUpload(
      FileUploadForm fileUploadForm, BindingResult bindingResult, Model model) {

    model.addAttribute(FILE_UPLOAD_FORM_MODEL_ATTR, fileUploadForm);
    model.addAttribute(BindingResult.MODEL_KEY_PREFIX + FILE_UPLOAD_FORM_MODEL_ATTR, bindingResult);
    return "pages/upload";
  }

  @HistogramMetric(
      valueStrategy = ValueCaptureStrategy.RETURN_VALUE,
      metricName = "file_size_bytes",
      hintText = "Size of uploaded bulk claim file in bytes which was submitted by the user")
  private double uploadFile(
      FileUploadForm fileUploadForm, OidcUser oidcUser, RedirectAttributes redirectAttributes) {
    ResponseEntity<CreateBulkSubmission201Response> responseEntity =
        dataClaimsRestClient
            .upload(
                fileUploadForm.getFile(),
                oidcUser.getPreferredUsername(),
                oidcAttributeUtils.getUserOffices(oidcUser))
            .block();
    CreateBulkSubmission201Response bulkSubmissionResponse = responseEntity.getBody();
    log.info(
        "Claims API Upload response bulk submission UUID: {}",
        bulkSubmissionResponse.getBulkSubmissionId());
    redirectAttributes.addFlashAttribute(
        SUBMISSION_ID, bulkSubmissionResponse.getSubmissionIds().getFirst());

    return fileUploadForm.getFile().getSize();
  }
}
