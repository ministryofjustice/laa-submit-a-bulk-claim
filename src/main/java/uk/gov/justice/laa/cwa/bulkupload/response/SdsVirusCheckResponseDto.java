package uk.gov.justice.laa.cwa.bulkupload.response;

import lombok.Data;

/**
 * The DTO class for upload response.
 */
@Data
public class SdsVirusCheckResponseDto {
    private String detail;
    private String success;
}