package uk.gov.justice.laa.cwa.bulkupload.response;

import lombok.Data;

import java.util.List;

/**
 * The DTO class for upload response.
 */
@Data
public class ProvidersResponseDto {
    private List<VendorDto> providers;
}