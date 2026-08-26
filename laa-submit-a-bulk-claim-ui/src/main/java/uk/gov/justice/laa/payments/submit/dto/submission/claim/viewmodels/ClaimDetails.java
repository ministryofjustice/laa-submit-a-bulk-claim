package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels;

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
  private ClaimFieldRow fixedFee;
  private ClaimFieldRow profitCosts;
  private ClaimFieldRow disbursements;
  private ClaimFieldRow disbursementsVat;
  private ClaimFieldRow vat;

  // Total allowed value
  private ClaimFieldRow totalVat;
  private ClaimFieldRow totalIncludingVat;

  public String clientName() {
    return "%s %s".formatted(clientForename, clientSurname);
  }
}
