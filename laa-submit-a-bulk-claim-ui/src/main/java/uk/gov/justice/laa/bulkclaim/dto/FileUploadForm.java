package uk.gov.justice.laa.bulkclaim.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Form backing object for bulk claim file uploads.
 *
 * @author Jamie Briggs
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadForm {

  private MultipartFile file;
}
