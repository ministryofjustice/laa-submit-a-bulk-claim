package uk.gov.justice.laa.bulkclaim.validation;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import uk.gov.justice.laa.bulkclaim.dto.inquest.InquestDetailsForm;

@Component
public class InquestDetailsFormValidator {

  public enum Field {
    DECEASED_FORENAME,
    DECEASED_SURNAME,
    DECEASED_DATE_OF_BIRTH,
    DECEASED_DATE_OF_DEATH,
    CORONERS_INQUEST_REFERENCE,
    INTERESTED_GOVERNMENT_DEPARTMENT,
    INTERESTED_PUBLIC_AUTHORITY
  }

  private final Set<Field> mandatoryFields;

  public InquestDetailsFormValidator(
      @Value("${app.inquest.mandatory-fields}") String configuredFields) {
    mandatoryFields =
        configuredFields.isBlank()
            ? EnumSet.noneOf(Field.class)
            : Arrays.stream(configuredFields.split(","))
                .map(String::trim)
                .map(Field::valueOf)
                .collect(() -> EnumSet.noneOf(Field.class), Set::add, Set::addAll);
  }

  public void validate(InquestDetailsForm form, Errors errors) {
    rejectBlank(Field.DECEASED_FORENAME, "deceasedForename", form.getDeceasedForename(), errors);
    rejectBlank(Field.DECEASED_SURNAME, "deceasedSurname", form.getDeceasedSurname(), errors);
    rejectNull(
        Field.DECEASED_DATE_OF_BIRTH, "deceasedDateOfBirth", form.getDeceasedDateOfBirth(), errors);
    rejectNull(
        Field.DECEASED_DATE_OF_DEATH, "deceasedDateOfDeath", form.getDeceasedDateOfDeath(), errors);
    rejectBlank(
        Field.CORONERS_INQUEST_REFERENCE,
        "coronersInquestReference",
        form.getCoronersInquestReference(),
        errors);
    rejectEmpty(
        Field.INTERESTED_GOVERNMENT_DEPARTMENT,
        "interestedDepartmentCodes",
        form.getInterestedDepartmentCodes(),
        errors);
    if (mandatoryFields.contains(Field.INTERESTED_PUBLIC_AUTHORITY)
        && hasNoEnteredValue(form.getInterestedPublicAuthorities())) {
      errors.rejectValue("interestedPublicAuthorities", "inquest.mandatory", "Enter a value");
    }
  }

  private void rejectBlank(Field policyField, String formField, String value, Errors errors) {
    if (mandatoryFields.contains(policyField) && (value == null || value.isBlank())) {
      errors.rejectValue(formField, "inquest.mandatory", "Enter a value");
    }
  }

  private void rejectNull(Field policyField, String formField, Object value, Errors errors) {
    if (mandatoryFields.contains(policyField) && value == null) {
      errors.rejectValue(formField, "inquest.mandatory", "Enter a value");
    }
  }

  private void rejectEmpty(
      Field policyField, String formField, java.util.Collection<String> value, Errors errors) {
    if (mandatoryFields.contains(policyField) && hasNoEnteredValue(value)) {
      errors.rejectValue(formField, "inquest.mandatory", "Enter a value");
    }
  }

  private boolean hasNoEnteredValue(java.util.Collection<String> values) {
    return values == null || values.stream().allMatch(value -> value == null || value.isBlank());
  }
}
