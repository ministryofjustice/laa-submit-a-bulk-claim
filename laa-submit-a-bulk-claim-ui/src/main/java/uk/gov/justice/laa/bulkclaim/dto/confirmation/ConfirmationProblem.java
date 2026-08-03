package uk.gov.justice.laa.bulkclaim.dto.confirmation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Relevant fields from the Claims API problem response for a rejected confirmation. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConfirmationProblem(List<ClaimConfirmationError> claimReports) {}
