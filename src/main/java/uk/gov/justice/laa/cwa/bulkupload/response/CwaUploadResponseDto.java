package uk.gov.justice.laa.cwa.bulkupload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** The DTO class for CWA upload response. */
@Data
public class CwaUploadResponseDto {

  @JsonProperty("am_bulk_file_id")
  private String fileId;

  @JsonProperty("message")
  private String message;
}
