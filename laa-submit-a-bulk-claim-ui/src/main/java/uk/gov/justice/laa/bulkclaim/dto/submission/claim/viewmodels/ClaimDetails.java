package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

import java.time.OffsetDateTime;
import lombok.Data;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

@Data
public abstract class ClaimDetails {

  // Page header / summary fields
  private String clientForename;
  private String clientSurname;
  private String officeCode;
  private OffsetDateTime dateSubmitted;
  private AreaOfLaw areaOfLaw;
  private String feeCode;
  private String feeCodeDescription;
  private String uniqueFileNumber;
  private String matterTypeCode;
  private String caseStartDate;
  private String caseConcludedDate;
  private Boolean escapeCase;

  // Values
  private ClaimField fixedFee;
  private ClaimField profitCosts;
  private ClaimField disbursements;
  private ClaimField disbursementsVat;
  private ClaimField vat;

  // Total allowed value
  private ClaimField totalVat;
  private ClaimField totalIncludingVat;

  public String clientName() {
    return "%s %s".formatted(clientForename, clientSurname);
  }
}
