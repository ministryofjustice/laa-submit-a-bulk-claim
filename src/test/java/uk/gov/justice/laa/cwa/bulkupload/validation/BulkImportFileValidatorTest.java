package uk.gov.justice.laa.cwa.bulkupload.validation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.SimpleErrors;
import org.springframework.validation.Validator;
import uk.gov.justice.laa.cwa.bulkupload.dto.FileUploadForm;

@DisplayName("Bulk import file validator test")
class BulkImportFileValidatorTest {

  Validator bulkClaimFileValidator = new BulkImportFileValidator("10MB");

  @Test
  @DisplayName("Should pass validation for valid .txt files")
  void txtShouldNotHaveErrors() {
    // Given an empty file
    MockMultipartFile file =
        new MockMultipartFile("file", "test.txt", "text/plain", new byte[10 * 1024 * 1024]);
    FileUploadForm fileUploadForm = new FileUploadForm(file);
    SimpleErrors errors = new SimpleErrors(fileUploadForm);

    // When
    bulkClaimFileValidator.validate(fileUploadForm, errors);

    // Then
    assertThat(errors.hasFieldErrors("file")).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"test.xml", "test.XML"})
  @DisplayName("Should pass validation for valid .xml files")
  void shouldPassValidationForValidXmlFile(String fileName) {
    // Given
    MockMultipartFile file =
        new MockMultipartFile("file", fileName, "application/xml", new byte[10 * 1024 * 1024]);
    FileUploadForm fileUploadForm = new FileUploadForm(file);
    SimpleErrors errors = new SimpleErrors(fileUploadForm);

    // When
    bulkClaimFileValidator.validate(fileUploadForm, errors);

    // Then
    assertThat(errors.hasFieldErrors("file")).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"test.csv", "test.CSV"})
  @DisplayName("Should pass validation for valid .csv files")
  void shouldPassValidationForValidCsvFile(String fileName) {
    // Given
    MockMultipartFile file =
        new MockMultipartFile("file", fileName, "text/csv", new byte[10 * 1024 * 1024]);
    FileUploadForm fileUploadForm = new FileUploadForm(file);
    SimpleErrors errors = new SimpleErrors(fileUploadForm);

    // When
    bulkClaimFileValidator.validate(fileUploadForm, errors);

    // Then
    assertThat(errors.hasFieldErrors("file")).isFalse();
  }

  @Test
  @DisplayName("Should have errors if file is empty")
  void shouldHaveErrorsIfFileIsEmpty() {
    // Given an empty file
    MockMultipartFile file = new MockMultipartFile("file", "test.xml", "text/xml", new byte[0]);
    FileUploadForm fileUploadForm = new FileUploadForm(file);
    SimpleErrors errors = new SimpleErrors(fileUploadForm);

    // When
    bulkClaimFileValidator.validate(fileUploadForm, errors);

    // Then
    assertThat(errors.hasFieldErrors("file")).isTrue();
    assertThat(errors.getFieldErrors("file").getFirst().getCode())
        .isEqualTo("bulkImport.validation.empty");
  }

  @ParameterizedTest
  @ValueSource(strings = {"test.docx", "test.json", "test.pdf", ""})
  @DisplayName("Should have error for unsupported file extensions")
  void shouldHaveErrorsForUnsupportedFileExtensions(String fileName) {
    // Given
    MockMultipartFile file =
        new MockMultipartFile(
            "file", fileName, "application/json", "content".getBytes(StandardCharsets.UTF_8));
    FileUploadForm fileUploadForm = new FileUploadForm(file);
    SimpleErrors errors = new SimpleErrors(fileUploadForm);

    // When
    bulkClaimFileValidator.validate(fileUploadForm, errors);

    // Then
    assertThat(errors.hasFieldErrors("file")).isTrue();
    assertThat(errors.getFieldErrors("file").getFirst().getCode())
        .isEqualTo("bulkImport.validation.extension");
  }

  @ParameterizedTest
  @ValueSource(strings = {"text/xml", "application/xml", "text/plain"})
  @DisplayName("Should have error if MIME type does not match .csv extension")
  void shouldHaveErrorIfMimeDoesNotMatchCsv(String mimeType) {
    // Given
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "test.csv", mimeType, "col1,col2".getBytes(StandardCharsets.UTF_8));
    FileUploadForm fileUploadForm = new FileUploadForm(file);
    SimpleErrors errors = new SimpleErrors(fileUploadForm);

    // When
    bulkClaimFileValidator.validate(fileUploadForm, errors);

    // Then
    assertThat(errors.hasFieldErrors("file")).isTrue();
    assertThat(errors.getFieldErrors("file").getFirst().getCode())
        .isEqualTo("bulkImport.validation.mimeType");
  }

  @ParameterizedTest
  @ValueSource(strings = {"text/csv", "text/plain"})
  @DisplayName("Should have error if MIME type does not match .xml extension")
  void shouldHaveErrorIfMimeDoesNotMatchXml(String mimeType) {
    // Given
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "test.xml", mimeType, "<p></p>".getBytes(StandardCharsets.UTF_8));
    FileUploadForm fileUploadForm = new FileUploadForm(file);
    SimpleErrors errors = new SimpleErrors(fileUploadForm);

    // When
    bulkClaimFileValidator.validate(fileUploadForm, errors);

    // Then
    assertThat(errors.hasFieldErrors("file")).isTrue();
    assertThat(errors.getFieldErrors("file").getFirst().getCode())
        .isEqualTo("bulkImport.validation.mimeType");
  }

  @ParameterizedTest
  @ValueSource(strings = {"text/csv", "text/xml"})
  @DisplayName("Should have error if MIME type does not match .txt extension")
  void shouldHaveErrorIfMimeTypeDoesNotMatchTxtExtension(String mimeType) {
    // Given
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "test.txt", mimeType, "Hello".getBytes(StandardCharsets.UTF_8));
    FileUploadForm fileUploadForm = new FileUploadForm(file);
    SimpleErrors errors = new SimpleErrors(fileUploadForm);

    // When
    bulkClaimFileValidator.validate(fileUploadForm, errors);

    // Then
    assertThat(errors.hasFieldErrors("file")).isTrue();
    assertThat(errors.getFieldErrors("file").getFirst().getCode())
        .isEqualTo("bulkImport.validation.mimeType");
  }

  @Test
  @DisplayName("Should have error if file size is larger than 10MB")
  void shouldHaveErrorIfFileSizeIsLargerThan10Mb() {
    // Given
    MockMultipartFile file =
        new MockMultipartFile("file", "test.csv", "text/csv", new byte[(10 * 1024 * 1024) + 1]);
    FileUploadForm fileUploadForm = new FileUploadForm(file);
    SimpleErrors errors = new SimpleErrors(fileUploadForm);

    // When
    bulkClaimFileValidator.validate(fileUploadForm, errors);

    // Then
    assertThat(errors.hasFieldErrors("file")).isTrue();
    assertThat(errors.getFieldErrors("file").getFirst().getCode())
        .isEqualTo("bulkImport.validation.size");
  }
}
