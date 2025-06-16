package uk.gov.justice.laa.cwa.bulkupload.response;

import lombok.Data;

/**
 * The DTO class for upload response.
 */
@Data
public class ValidateResponseDto {
    private String status;
    private String message;
    private String data;
}