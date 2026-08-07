package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Builder;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

@Builder
public record MediationClaimDetails(
    // Page header / summary fields
    String client1Forename,
    String client1Surname,
    String client1UniqueClientNumber,
    String client2Forename,
    String client2Surname,
    String client2UniqueClientNumber,
    String feeCode,
    String officeCode,
    OffsetDateTime dateSubmitted,
    AreaOfLaw areaOfLaw,

    // Values - Reported column
    BigDecimal reportedDisbursements,
    BigDecimal reportedDisbursementsVat,
    Boolean reportedVatApplicable,

    // Values - Initial calculated column
    BigDecimal initialCalculatedFixedFee,
    BigDecimal initialCalculatedDisbursements,
    BigDecimal initialCalculatedDisbursementsVat,
    Boolean initialCalculatedVatIndicator,

    // Total allowed value
    BigDecimal initialCalculatedTotalVat,
    BigDecimal initialCalculatedTotalIncludingVat) {}
