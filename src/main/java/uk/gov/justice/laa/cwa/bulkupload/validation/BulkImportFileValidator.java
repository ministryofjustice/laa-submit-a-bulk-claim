package uk.gov.justice.laa.cwa.bulkupload.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.laa.cwa.bulkupload.dto.FileUploadForm;

/**
 * A validator for validating files intended for bulk submissions. This class implements the {@link
 * Validator} and checks the general properties of the file.
 *
 * <p>The supported file types and their validation rules include:
 *
 * <ul>
 *   <li>CSV files: Must have an extension ".csv" and MIME type "text/csv".
 *   <li>XML files: Must have an extension ".xml" and MIME type "text/xml" or "application/xml".
 *   <li>TXT files: Must have an extension ".txt" and MIME type "text/plain".
 * </ul>
 *
 * <p>Additionally, the validator ensures that:
 *
 * <ul>
 *   <li>The file is not empty.
 *   <li>The file size does not exceed 10MB.
 * </ul>
 *
 * @author Jamie Briggs
 */
@Component
public class BulkImportFileValidator implements Validator {

  private final String maxFileSizeReadable;
  private final long maxFileSize;

  public BulkImportFileValidator(@Value("${upload-max-file-size:10MB}") String fileSizeLimit) {
    this.maxFileSizeReadable = fileSizeLimit;
    this.maxFileSize = DataSize.parse(fileSizeLimit).toBytes();
  }

  /**
   * Checks the class type is supported by this validator.
   *
   * @return true if class is a {@link FileUploadForm}.
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return FileUploadForm.class.isAssignableFrom(clazz);
  }

  /**
   * Validates the provided {@link FileUploadForm} using the following rules:
   *
   * <p>The supported file types and their validation rules include:
   *
   * <ul>
   *   <li>CSV files: Must have an extension ".csv" and MIME type "text/csv".
   *   <li>XML files: Must have an extension ".xml" and MIME type "text/xml" or "application/xml".
   *   <li>TXT files: Must have an extension ".txt" and MIME type "text/plain".
   * </ul>
   *
   * <p>Additionally, the ensures that:
   *
   * <ul>
   *   <li>The file is not empty.
   *   <li>The file size does not exceed 10MB.
   * </ul>
   *
   * @param target the {@link FileUploadForm} object to be validated.
   * @param errors the {@link Errors} object to which validation errors are reported.
   */
  @Override
  public void validate(Object target, Errors errors) {
    FileUploadForm uploadForm = (FileUploadForm) target;
    MultipartFile file = uploadForm.file();

    // Step 1: Check if file is null or empty
    if (file.isEmpty()) {
      errors.rejectValue("file", "bulkImport.validation.empty");
    }

    String originalFilename = file.getOriginalFilename();
    String lowercaseFileName = originalFilename.toLowerCase();

    // Step 2: Validate file extension
    if (originalFilename.isEmpty()
        || !lowercaseFileName.endsWith(".csv")
            && !lowercaseFileName.endsWith(".xml")
            && !lowercaseFileName.endsWith(".txt")) {
      errors.rejectValue("file", "bulkImport.validation.extension");
    }

    // Step 3: Validate MIME Type
    String contentType = file.getContentType();
    if ((lowercaseFileName.endsWith(".csv") && !"text/csv".equals(contentType))
        || (lowercaseFileName.endsWith(".xml")
            && !("text/xml".equals(contentType) || "application/xml".equals(contentType)))
        || (lowercaseFileName.endsWith(".txt") && !("text/plain".equals(contentType)))) {
      errors.rejectValue("file", "bulkImport.validation.mimeType");
    }

    // Step 4: Validate file size
    if (file.getSize() > maxFileSize) {
      errors.rejectValue(
          "file",
          "bulkImport.validation.size",
          new String[] {maxFileSizeReadable},
          "File size too large");
    }
  }
}
