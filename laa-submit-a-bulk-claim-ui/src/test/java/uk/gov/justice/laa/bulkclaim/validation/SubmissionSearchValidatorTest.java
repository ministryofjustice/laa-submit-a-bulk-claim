package uk.gov.justice.laa.bulkclaim.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;

class SubmissionSearchValidatorTest {

  private final SubmissionSearchValidator validator = new SubmissionSearchValidator();

  @Test
  void validateShouldRejectSubmissionIdWhenNotUuid() {
    final SubmissionsSearchForm form = new SubmissionsSearchForm("not-a-uuid", null, null);
    final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

    validator.validate(form, errors);

    assertTrue(errors.hasFieldErrors("submissionId"));
    assertEquals("search.submissionId.invalid", errors.getFieldError("submissionId").getCode());
  }

  @Test
  void validateShouldAcceptSubmissionIdWhenUuid() {
    final SubmissionsSearchForm form =
        new SubmissionsSearchForm("550e8400-e29b-41d4-a716-446655440000", null, null);
    final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

    validator.validate(form, errors);

    assertFalse(errors.hasErrors());
  }
}
