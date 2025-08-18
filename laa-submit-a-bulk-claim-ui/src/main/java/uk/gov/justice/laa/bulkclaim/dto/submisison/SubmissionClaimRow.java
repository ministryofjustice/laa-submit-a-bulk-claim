package uk.gov.justice.laa.bulkclaim.dto.submisison;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Holds information about a claim in a submission. Intended for a table of data.
 *
 * @param claimNumber unique identifier for the claim
 * @param ufn the unique file number
 * @param ucn the unique client number
 * @param client the client name
 * @param category the claim category
 * @param matter the claim matter type
 * @param concludedOrClaimedDate the date the claim was concluded or claimed
 * @param feeType the fee type
 * @param feeCode the fee code
 * @param claimValue the claim value
 * @author Jamie Briggs
 */
public record SubmissionClaimRow(
    int claimNumber,
    String ufn,
    String ucn,
    String client,
    String category,
    String matter,
    LocalDate concludedOrClaimedDate,
    String feeType,
    String feeCode,
    BigDecimal claimValue) {}
