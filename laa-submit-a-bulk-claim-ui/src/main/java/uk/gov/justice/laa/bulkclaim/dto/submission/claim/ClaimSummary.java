package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.time.LocalDate;
import lombok.Builder;

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
