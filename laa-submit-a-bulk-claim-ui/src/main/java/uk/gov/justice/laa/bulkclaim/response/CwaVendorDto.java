package uk.gov.justice.laa.bulkclaim.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** The DTO class for CWA vendor information. */
@Data
public class CwaVendorDto {

  @JsonProperty("VENDOR_NAME")
  String vendorName;

  @JsonProperty("VENDOR_ID")
  int vendorId;
}
