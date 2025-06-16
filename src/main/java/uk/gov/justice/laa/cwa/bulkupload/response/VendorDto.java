package uk.gov.justice.laa.cwa.bulkupload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * The DTO class for vendor information.
 */
@Data
public class VendorDto {
    @JsonProperty("VENDOR_NAME")
    String vendorName;
    @JsonProperty("VENDOR_ID")
    int vendorId;
}
