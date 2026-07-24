package uk.gov.justice.laa.bulkclaim.dto.inquest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class InquestDetailsForm {

  private String deceasedForename;
  private String deceasedSurname;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate deceasedDateOfBirth;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate deceasedDateOfDeath;

  private String coronersInquestReference;
  private Set<String> interestedDepartmentCodes = new LinkedHashSet<>();
  private List<String> interestedPublicAuthorities = emptyAuthorityInputs();

  public void clear() {
    deceasedForename = null;
    deceasedSurname = null;
    deceasedDateOfBirth = null;
    deceasedDateOfDeath = null;
    coronersInquestReference = null;
    interestedDepartmentCodes = new LinkedHashSet<>();
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
            ? new LinkedHashSet<>()
            : new LinkedHashSet<>(data.interestedDepartmentCodes());
    interestedPublicAuthorities =
        data.interestedPublicAuthorities() == null
            ? emptyAuthorityInputs()
            : new ArrayList<>(data.interestedPublicAuthorities());
    while (interestedPublicAuthorities.size() < 3) {
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
        interestedDepartmentCodes,
        interestedPublicAuthorities.stream().filter(value -> !value.isBlank()).toList(),
        actorUserId);
  }

  private static ArrayList<String> emptyAuthorityInputs() {
    return new ArrayList<>(List.of("", "", ""));
  }
}
