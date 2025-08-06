package uk.gov.justice.laa.cwa.bulkupload.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * A DTO representing a file upload form. Acts as a wrapper for the Spring {@link MultipartFile} to
 * make {@link org.springframework.validation.BindingResult} support {@link MultipartFile}.
 *
 * @param file the file to be uploaded.
 * @author Jamie Briggs
 */
public record FileUploadForm(MultipartFile file) {}
