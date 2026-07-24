package uk.gov.justice.laa.bulkclaim.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.gov.justice.laa.bulkclaim.dto.inquest.InquestDetailsForm;

class InquestDetailsFormValidatorTest {

  private final InquestDetailsFormValidator validator =
      new InquestDetailsFormValidator(
          "INTERESTED_GOVERNMENT_DEPARTMENT,INTERESTED_PUBLIC_AUTHORITY");

  @Test
  void blankRepeatableRowsDoNotSatisfyMandatoryFields() {
    var form = new InquestDetailsForm();
    form.setInterestedDepartmentCodes(List.of(""));
    form.setInterestedPublicAuthorities(Arrays.asList(null, " "));
    var errors = new BeanPropertyBindingResult(form, "inquestDetailsForm");

    validator.validate(form, errors);

    assertThat(errors.hasFieldErrors("interestedDepartmentCodes")).isTrue();
    assertThat(errors.hasFieldErrors("interestedPublicAuthorities")).isTrue();
  }
}
