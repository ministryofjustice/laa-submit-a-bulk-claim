package uk.gov.justice.laa.bulkclaim.dto.inquest;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class InquestDetailsForm implements Serializable {

  private String deceasedForename;
  private String deceasedSurname;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate deceasedDateOfBirth;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate deceasedDateOfDeath;

  private String coronersInquestReference;
  private List<String> interestedDepartmentCodes = emptyInputs();
  private List<String> interestedPublicAuthorities = emptyAuthorityInputs();

  public void clear() {
    deceasedForename = null;
    deceasedSurname = null;
    deceasedDateOfBirth = null;
    deceasedDateOfDeath = null;
    coronersInquestReference = null;
    interestedDepartmentCodes = emptyInputs();
    interestedPublicAuthorities = emptyAuthorityInputs();
  }

  public void populate(ClaimInquestData data) {
    deceasedForename = data.deceasedForename();
    deceasedSurname = data.deceasedSurname();
    deceasedDateOfBirth = data.deceasedDateOfBirth();
    deceasedDateOfDeath = data.deceasedDateOfDeath();
    coronersInquestReference = data.coronersInquestReference();
    interestedDepartmentCodes =
        data.interestedDepartmentCodes() == null
            ? emptyInputs()
            : new ArrayList<>(data.interestedDepartmentCodes());
    if (interestedDepartmentCodes.isEmpty()) {
      interestedDepartmentCodes.add("");
    }
    interestedPublicAuthorities =
        data.interestedPublicAuthorities() == null
            ? emptyAuthorityInputs()
            : new ArrayList<>(data.interestedPublicAuthorities());
    if (interestedPublicAuthorities.isEmpty()) {
      interestedPublicAuthorities.add("");
    }
  }

  public ClaimInquestDataWrite toWriteRequest(String actorUserId) {
    return new ClaimInquestDataWrite(
        deceasedForename,
        deceasedSurname,
        deceasedDateOfBirth,
        deceasedDateOfDeath,
        coronersInquestReference,
        new LinkedHashSet<>(
            interestedDepartmentCodes.stream()
                .filter(value -> value != null && !value.isBlank())
                .toList()),
        interestedPublicAuthorities.stream()
            .filter(value -> value != null && !value.isBlank())
            .toList(),
        actorUserId);
  }

  private static ArrayList<String> emptyAuthorityInputs() {
    return emptyInputs();
  }

  private static ArrayList<String> emptyInputs() {
    return new ArrayList<>(List.of(""));
  }
}
