package uk.gov.justice.laa.cwa.bulkupload.validation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.SimpleErrors;
import org.springframework.validation.Validator;
import uk.gov.justice.laa.cwa.bulkupload.service.VirusCheckService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Bulk import file virus validator test")
class BulkImportFileVirusValidatorTest {

  @Mock VirusCheckService virusCheckService;

  Validator bulkClaimFileVirusValidator;

  @BeforeEach
  void beforeEach() {
    bulkClaimFileVirusValidator = new BulkImportFileVirusValidator(virusCheckService);
  }

  @Test
  @DisplayName("Should not have errors")
  void shouldHaveNoErrors() {
    // Given an empty file
    MockMultipartFile file =
        new MockMultipartFile("file", "test.txt", "text/plain", new byte[10 * 1024 * 1024]);
    SimpleErrors errors = new SimpleErrors(file);

    // When
    bulkClaimFileVirusValidator.validate(file, errors);

    // Then
    verify(virusCheckService, times(1)).checkVirus(file);
    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  @DisplayName("Should have errors when virus check failed")
  void shouldHaveErrorsWhenVirusCheckFailed() {
    // Given an empty file
    MockMultipartFile file =
        new MockMultipartFile("file", "test.txt", "text/plain", new byte[10 * 1024 * 1024]);
    SimpleErrors errors = new SimpleErrors(file);
    doThrow(new RuntimeException("Virus check failed")).when(virusCheckService).checkVirus(file);

    // When
    bulkClaimFileVirusValidator.validate(file, errors);

    // Then
    verify(virusCheckService, times(1)).checkVirus(file);
    assertThat(errors.hasErrors()).isTrue();
    assertThat(errors.getAllErrors().getFirst().getCode())
        .isEqualTo("bulkImport.validation.virusScanFailed");
  }
}
