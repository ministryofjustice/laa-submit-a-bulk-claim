package uk.gov.justice.laa.cwa.bulkupload.dto;

import org.springframework.web.multipart.MultipartFile;

public record FileUploadForm(MultipartFile file) {}
