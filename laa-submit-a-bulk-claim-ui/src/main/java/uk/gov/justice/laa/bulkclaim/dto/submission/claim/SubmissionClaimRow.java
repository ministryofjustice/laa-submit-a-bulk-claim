package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Holds information about a claim in a submission. Intended for a table of data.
 *
 * @param id the UUID ID for the claim
 * @param lineNumber the line number for the claim
 * @param ufn the unique file number
 * @param ucn the unique client number
 * @param client the client name
 * @param category the claim category
 * @param matter the claim matter type
 * @param concludedOrClaimedDate the date the claim was concluded or claimed
 * @param feeType the fee type
 * @param feeCode the fee code
 * @param costsDetails the costs details
 * @author Jamie Briggs
 */
public record SubmissionClaimRow(
    UUID id,
    int lineNumber,
    String ufn,
    String ucn,
    String client,
    String category,
    String matter,
    LocalDate concludedOrClaimedDate,
    String feeType,
    String feeCode,
    SubmissionClaimRowCostsDetails costsDetails) {}
