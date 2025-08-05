package uk.gov.justice.laa.cwa.bulkupload.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.laa.cwa.bulkupload.dto.FileUploadForm;
import uk.gov.justice.laa.cwa.bulkupload.service.VirusCheckService;

/**
 * A validator for validating files intended for bulk submissions. This class implements the {@link
 * Validator}, and performs a virus check on the file.
 *
 * @author Jamie Briggs
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BulkImportFileVirusValidator implements Validator {

  private final VirusCheckService virusCheckService;

  /**
   * Checks the class type is supported by this validator.
   *
   * @return true if class is a {@link MultipartFile}.
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return MultipartFile.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    FileUploadForm uploadForm = (FileUploadForm) target;
    MultipartFile file = uploadForm.file();
    try {

      virusCheckService.checkVirus(file);

    } catch (Exception e) {
      log.error("Virus check failed with message: {}", e.getMessage());
      errors.reject("bulkImport.validation.virusScanFailed");
    }
  }
}
