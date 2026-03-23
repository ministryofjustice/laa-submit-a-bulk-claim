package uk.gov.justice.laa.bulkclaim.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.laa.bulkclaim.dto.FileUploadForm;
import uk.gov.justice.laa.bulkclaim.exception.TokenProviderException;
import uk.gov.justice.laa.bulkclaim.exception.VirusCheckException;
import uk.gov.justice.laa.bulkclaim.service.VirusCheckService;

/**
 * An implementation of {@link FileFirusValidator} that disables virus validation
 * for bulk import files. Primarily used in the "github-test-runner" profile.
 *
 * @author Jamie
 */
@Profile("github-test-runner")
@Component
@Slf4j
@RequiredArgsConstructor
public class DisabledBulkImportFileVirusValidator implements FileFirusValidator {


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
    log.debug("Virus check disabled, continuing as normal");
  }
}
