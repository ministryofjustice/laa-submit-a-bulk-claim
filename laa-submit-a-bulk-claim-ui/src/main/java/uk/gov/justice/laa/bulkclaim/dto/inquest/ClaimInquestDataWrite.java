package uk.gov.justice.laa.bulkclaim.dto.inquest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record ClaimInquestDataWrite(
    @JsonProperty("deceased_forename") String deceasedForename,
    @JsonProperty("deceased_surname") String deceasedSurname,
    @JsonProperty("deceased_date_of_birth") LocalDate deceasedDateOfBirth,
    @JsonProperty("deceased_date_of_death") LocalDate deceasedDateOfDeath,
    @JsonProperty("coroners_inquest_reference") String coronersInquestReference,
    @JsonProperty("interested_department_codes") Set<String> interestedDepartmentCodes,
    @JsonProperty("interested_public_authorities") List<String> interestedPublicAuthorities,
    @JsonProperty("actor_user_id") String actorUserId) {}
