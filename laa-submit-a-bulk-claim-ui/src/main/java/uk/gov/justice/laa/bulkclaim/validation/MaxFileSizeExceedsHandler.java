package uk.gov.justice.laa.bulkclaim.validation;

import org.apache.tomcat.util.http.InvalidParameterException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import uk.gov.justice.laa.bulkclaim.controller.BulkImportController;
import uk.gov.justice.laa.bulkclaim.dto.FileUploadForm;
import uk.gov.justice.laa.bulkclaim.metrics.BulkClaimMetricService;

/** Handles MaxUploadSizeExceededException thrown when a file exceeds the maximum upload size. */
@ControllerAdvice
public class MaxFileSizeExceedsHandler {

  private final BulkClaimMetricService bulkClaimMetricService;
  private final String maxFileSizeReadable;

  public MaxFileSizeExceedsHandler(
      BulkClaimMetricService bulkClaimMetricService,
      @Value("${upload-max-file-size:10MB}") String maxFileSizeReadable) {
    this.bulkClaimMetricService = bulkClaimMetricService;
    this.maxFileSizeReadable = maxFileSizeReadable;
  }

  /**
   * Handles MaxUploadSizeExceededException and returns the upload page with an error message.
   *
   * @param ex the exception
   * @param model the model to be populated with data
   * @return the upload page
   */
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public String handleMaxSizeException(MaxUploadSizeExceededException ex, Model model) {
    return buildErrorResponse(ex, model);
  }

  @ExceptionHandler(InvalidParameterException.class)
  public String handleTomcatMaxSizeException(InvalidParameterException ex, Model model) {
    Throwable cause = ex.getCause();
    if (!(cause instanceof SizeLimitExceededException)) {
      throw ex;
    }
    return buildErrorResponse(ex, model);
  }

  private String buildErrorResponse(Exception ex, Model model) {
    FileUploadForm fileUploadForm = new FileUploadForm(null);
    BindingResult bindingResult =
        new BeanPropertyBindingResult(
            fileUploadForm, BulkImportController.FILE_UPLOAD_FORM_MODEL_ATTR);

    bindingResult.rejectValue(
        "file",
        "bulkImport.validation.size",
        new String[] {maxFileSizeReadable},
        "File size too large. Maximum allowed is " + maxFileSizeReadable + ".");

    model.addAttribute(BulkImportController.FILE_UPLOAD_FORM_MODEL_ATTR, fileUploadForm);
    model.addAttribute(
        "org.springframework.validation.BindingResult."
            + BulkImportController.FILE_UPLOAD_FORM_MODEL_ATTR,
        bindingResult);

    recordFailedFileUploadSize(ex);

    return "pages/upload";
  }

  private void recordFailedFileUploadSize(Exception ex) {
    if (ex instanceof MaxUploadSizeExceededException maxUploadSizeExceededException) {
      bulkClaimMetricService.recordFailedFileUploadSize(maxUploadSizeExceededException);
      return;
    }
    if (ex instanceof InvalidParameterException invalidParameterException) {
      Throwable cause = invalidParameterException.getCause();
      if (cause != null) {
        String message = cause.getMessage();
        if (message != null) {
          try {
            long size = Long.parseLong(message.replaceAll(".*size \\((\\d+)\\).*", "$1"));
            bulkClaimMetricService.recordFailedFileUploadSize(
                size, "File size exceeds maximum allowed");
          } catch (NumberFormatException ignored) {
            // Fall back to not recording size if parsing fails.
          }
        }
      }
    }
  }
}
