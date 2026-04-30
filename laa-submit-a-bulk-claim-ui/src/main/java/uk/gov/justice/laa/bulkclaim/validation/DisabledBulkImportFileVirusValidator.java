package uk.gov.justice.laa.bulkclaim.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

/**
 * An implementation of {@link FileFirusValidator} that disables virus validation for bulk import
 * files. Primarily used in the "github-test-runner" profile.
 */
@Profile("github-test-runner")
@Component
@Slf4j
@RequiredArgsConstructor
public class DisabledBulkImportFileVirusValidator implements FileFirusValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return MultipartFile.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    log.debug("Virus check disabled, continuing as normal");
  }
}
