package uk.gov.justice.laa.bulkclaim.dto.confirmation;

import java.util.List;
import java.util.UUID;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagePatch;

/** Claim-level messages returned when a draft cannot be confirmed. */
public record ClaimConfirmationError(
    UUID claimId, List<ValidationMessagePatch> validationMessages) {}
