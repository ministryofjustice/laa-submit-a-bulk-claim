package uk.gov.justice.laa.bulkclaim.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import uk.gov.justice.laa.bulkclaim.controller.BulkImportController;
import uk.gov.justice.laa.bulkclaim.dto.FileUploadForm;

class MaxFileSizeExceedsHandlerTest {

  private final MaxFileSizeExceedsHandler handler = new MaxFileSizeExceedsHandler();

  @Test
  void handleMaxSizeExceptionPopulatesModelAndReturnsUploadView() {
    Model model = new ExtendedModelMap();

    String viewName = handler.handleMaxSizeException(new MaxUploadSizeExceededException(11), model);

    assertThat(viewName).isEqualTo("pages/upload");
    assertThat(model.getAttribute(BulkImportController.FILE_UPLOAD_FORM_MODEL_ATTR))
        .isInstanceOf(FileUploadForm.class);
    FileUploadForm fileUploadForm =
        (FileUploadForm) model.getAttribute(BulkImportController.FILE_UPLOAD_FORM_MODEL_ATTR);
    assertThat(fileUploadForm.file()).isNull();

    Object bindingResultAttribute =
        model.getAttribute(
            "org.springframework.validation.BindingResult."
                + BulkImportController.FILE_UPLOAD_FORM_MODEL_ATTR);
    assertThat(bindingResultAttribute).isInstanceOf(BindingResult.class);

    BindingResult bindingResult = (BindingResult) bindingResultAttribute;
    FieldError fieldError = bindingResult.getFieldError("file");

    assertThat(fieldError).isNotNull();
    assertThat(fieldError.getCode()).isEqualTo("bulkImport.validation.size");
    assertThat(fieldError.getDefaultMessage())
        .isEqualTo("File size too large. Maximum allowed is 10MB.");
    assertThat(fieldError.getArguments()).isNotNull();
    assertThat(fieldError.getArguments()).contains("10MB");
  }
}
