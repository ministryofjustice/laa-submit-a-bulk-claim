package uk.gov.justice.laa.cwa.bulkupload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** The DTO class for CWA upload error response. */
@Data
public class CwaUploadErrorResponseDto {

  @JsonProperty("SUMMARY_ID")
  public int summaryId;

  @JsonProperty("ITEM_CODE")
  public String itemCode;

  @JsonProperty("UFN")
  public String ufn;

  @JsonProperty("CLIENT_SURNAME")
  public String clientSurname;

  @JsonProperty("ERROR_TYPE")
  public String errorType;

  @JsonProperty("DESCRIPTION")
  public String description;

  @JsonProperty("AM_BULK_FILE_ID")
  public int fileId;

  @JsonProperty("AM_BULK_HEADER_ID")
  public int amBulkHeaderId;

  @JsonProperty("AM_BULK_LINE_ID")
  public int amBulkLineId;

  @JsonProperty("ERROR_ID")
  public int errorId;
}
