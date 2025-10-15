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
 * @param clientForename the primary client's forename
 * @param clientSurname the primary client's surname
 * @param client2Forename the secondary client's forename (mediation only)
 * @param client2Surname the secondary client's surname (mediation only)
 * @param client2Ucn the secondary client's UCN (mediation only)
 * @param category the claim category
 * @param matter the claim matter type
 * @param concludedOrClaimedDate the date the claim was concluded or claimed
 * @param totalMessages the calculatedTotal messages attached to the claim
 * @param feeType the fee type
 * @param feeCode the fee code
 * @param costsDetails the costs details
 * @param escapeCase whether the claim is an escape case (Crime only)
 * @author Jamie Briggs
 */
public record SubmissionClaimRow(
    UUID id,
    int lineNumber,
    String ufn,
    String ucn,
    String client,
    String clientForename,
    String clientSurname,
    String client2Forename,
    String client2Surname,
    String client2Ucn,
    String category,
    String matter,
    LocalDate concludedOrClaimedDate,
    int totalMessages,
    String feeType,
    String feeCode,
    SubmissionClaimRowCostsDetails costsDetails,
    Boolean escapeCase) {}
