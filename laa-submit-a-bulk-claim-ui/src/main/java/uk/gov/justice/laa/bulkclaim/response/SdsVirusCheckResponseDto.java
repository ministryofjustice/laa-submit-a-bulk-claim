package uk.gov.justice.laa.bulkclaim.response;

import lombok.Data;

/** The DTO class for SDS virus check response. */
@Data
public class SdsVirusCheckResponseDto {

  private String detail;
  private String success;
}
