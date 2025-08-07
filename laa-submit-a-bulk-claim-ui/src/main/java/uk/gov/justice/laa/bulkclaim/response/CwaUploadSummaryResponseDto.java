package uk.gov.justice.laa.bulkclaim.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** The DTO class for CWA upload summary response. */
@Data
public class CwaUploadSummaryResponseDto {

  @JsonProperty("SUMMARY_ID")
  public int summaryId;

  @JsonProperty("LSC_ACCOUNT_NUM")
  public String lscAccountNum;

  @JsonProperty("SCHEDULE_NUM")
  public String scheduleNum;

  @JsonProperty("TOTAL_OUTCOMES")
  public int totalOutcomes;

  @JsonProperty("SUCCESSFUL_OUTCOMES")
  public int successfulOutcomes;

  @JsonProperty("PROBLEM_OUTCOMES")
  public int problemOutcomes;

  @JsonProperty("DUPLICATE_OUTCOMES")
  public int duplicateOutcomes;

  @JsonProperty("INVALID_OUTCOMES")
  public int invalidOutcomes;

  @JsonProperty("AM_BULK_FILE_ID")
  public int fileId;
}
