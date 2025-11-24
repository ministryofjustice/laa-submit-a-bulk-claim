package uk.gov.justice.laa.bulkclaim.validation;

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

  public MaxFileSizeExceedsHandler(BulkClaimMetricService bulkClaimMetricService) {
    this.bulkClaimMetricService = bulkClaimMetricService;
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

    FileUploadForm fileUploadForm = new FileUploadForm(null);
    BindingResult bindingResult =
        new BeanPropertyBindingResult(
            fileUploadForm, BulkImportController.FILE_UPLOAD_FORM_MODEL_ATTR);

    bindingResult.rejectValue(
        "file",
        "bulkImport.validation.size",
        new String[] {"10MB"},
        "File size too large. Maximum allowed is 10MB.");

    model.addAttribute(BulkImportController.FILE_UPLOAD_FORM_MODEL_ATTR, fileUploadForm);
    model.addAttribute(
        "org.springframework.validation.BindingResult."
            + BulkImportController.FILE_UPLOAD_FORM_MODEL_ATTR,
        bindingResult);

    bulkClaimMetricService.recordFailedFileUploadSize(ex);

    return "pages/upload";
  }
}
