package uk.gov.justice.laa.bulkclaim.response;

import lombok.Data;

/** The DTO class for CWA submission response. */
@Data
public class CwaSubmissionResponseDto {

  private String status;
  private String message;
  private String data;
}
