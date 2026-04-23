package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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
 * @param uniqueClientNumber2 the unique client number for client2
 * @param outcomeCode the outcome code
 * @param caseConcludedDate the case concluded date
 * @param officeAccountNumber the office account
 * @param matterType1 the matter type 1
 * @param matterType2 the matter type 2
 * @param isVatApplicable the vat applicable flag
 * @param caseStartDate the case start date
 * @param categoryOfLaw the category of law
 * @param feeCodeDescription the fee code description
 * @param submissionDate the date submitted
 * @param clientName the client name
 * @param client2Name the client 2 name
 * @param isEscaped case escaped flag
 */
@Builder
public record ClaimSummary(
    String areaOfLaw,
    String uniqueFileNumber, /*CIVIL, CRIME*/
    String matterTypeCode, /*CRIME*/
    String feeCode, /*CIVIL, CRIME, MEDIATION*/
    String clientForename, /*CRIME*/
    String clientSurname, /*CRIME*/
    String stageReachedCode, /*CRIME*/
    String uniqueClientNumber, /*CIVIL, MEDIATION*/
    String client2Forename, /*MEDIATION*/
    String client2Surname, /*MEDIATION*/
    String uniqueClientNumber2, /*MEDIATION*/
    String outcomeCode, /*CRIME*/
    LocalDate caseConcludedDate, /*CIVIL, CRIME, MEDIATION*/
    String officeAccountNumber, /*CIVIL, CRIME, MEDIATION*/
    String matterType1, /*CIVIL, MEDIATION*/
    String matterType2, /*CIVIL, MEDIATION*/
    Boolean isVatApplicable, /*CIVIL, CRIME, MEDIATION*/
    LocalDate caseStartDate, /*CIVIL, CRIME, MEDIATION*/
    String categoryOfLaw, /*CIVIL, CRIME, MEDIATION*/
    String feeCodeDescription, /*CIVIL, CRIME, MEDIATION*/
    OffsetDateTime submissionDate, /*CIVIL, CRIME, MEDIATION*/
    String clientName, /*CIVIL, MEDIATION*/
    String client2Name, /*MEDIATION*/
    Boolean isEscaped /*CIVIL, CRIME, MEDIATION*/) {}
