package uk.gov.justice.laa.bulkclaim.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.laa.bulkclaim.dto.FileUploadForm;
import uk.gov.justice.laa.bulkclaim.exception.TokenProviderException;
import uk.gov.justice.laa.bulkclaim.exception.VirusCheckException;
import uk.gov.justice.laa.bulkclaim.service.VirusCheckService;

@Profile("!github-test-runner")
@Component
@Slf4j
@RequiredArgsConstructor
public class BulkImportFileVirusValidator implements FileFirusValidator {

  private final VirusCheckService virusCheckService;

  @Override
  public boolean supports(Class<?> clazz) {
    return MultipartFile.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    FileUploadForm uploadForm = (FileUploadForm) target;
    MultipartFile file = uploadForm.getFile();
    try {
      virusCheckService.checkVirus(file);
    } catch (VirusCheckException e) {
      log.error("Virus check failed with message: {}", e.getMessage());
      errors.reject("bulkImport.validation.virusScanFailed");
    } catch (TokenProviderException tokenProviderException) {
      log.error("Failed to obtain SDS API access token: {}", tokenProviderException.getMessage());
      errors.reject("bulkImport.validation.uploadFailed");
    } catch (HttpClientErrorException | ResourceAccessException e) {
      log.error("Failed to perform virus check,SDS config may be incorrect");
      errors.reject("error.heading");
    }
  }
}
