package uk.gov.justice.laa.bulkclaim.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Represents a form for handling file uploads. This class is used to encapsulate the uploaded file.
 * Implements {@link Serializable} for serialization purposes. Note: - The {@code file} field is
 * marked as transient to avoid serialization. - The {@link MultipartFile} type is used to handle
 * the uploaded file data.
 *
 * @author Jamie Briggs
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadForm implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private transient MultipartFile file;
}
