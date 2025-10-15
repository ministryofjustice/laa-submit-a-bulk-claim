package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.time.LocalDate;
import lombok.Builder;

/**
 * Holds information about a claim in a submission.
 *
 * @param areaOfLaw the area of law
 * @param uniqueFileNumber the unique file number
 * @param matterTypeCode the matter type code
 * @param feeCode the fee code
 * @param clientForename the client forename
 * @param clientSurname the client surname
 * @param stageReachedCode the stage reached code
 * @param uniqueClientNumber the unique client number
 * @param client2Forename the second client's forename
 * @param client2Surname the second client's surname
 * @param outcomeCode the outcome code
 * @param caseConcludedDate the case concluded date
 * @param isEscaped case escaped flag
 */
@Builder
public record ClaimSummary(
    String areaOfLaw,
    String matterTypeCode, /*CRIME, MEDIATION*/
    String feeCode, /*CIVIL, MEDIATION*/
    String clientForename, /*CIVIL, CRIME, MEDIATION*/
    String clientSurname, /*CIVIL, CRIME, MEDIATION*/
    String uniqueClientNumber, /*CIVIL, MEDIATION*/
    String client2Forename, /*MEDIATION*/
    String client2Surname, /*MEDIATION*/
    String uniqueClientNumber2, /*CIVIL, MEDIATION*/
    String stageReachedCode, /*CRIME*/
    String uniqueFileNumber, /*CIVIL, CRIME*/
    String outcomeCode, /*CRIME*/
    LocalDate caseConcludedDate, /*CRIME*/
    Boolean isEscaped /*CIVIL*/) {}
