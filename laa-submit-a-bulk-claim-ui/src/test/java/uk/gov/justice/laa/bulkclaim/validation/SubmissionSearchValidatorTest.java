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

  @Test
  void validateShouldOnlyFlagMissingToDateWhenFromDateProvided() {
    final SubmissionsSearchForm form = new SubmissionsSearchForm(null, "01/01/2024", null);
    final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

    validator.validate(form, errors);

    assertFalse(errors.hasFieldErrors(SubmissionSearchValidator.SUBMITTED_DATE_FROM));
    assertTrue(errors.hasFieldErrors(SubmissionSearchValidator.SUBMITTED_DATE_TO));
    assertEquals(
        "search.error.date.to.requiredWithFrom",
        errors.getFieldError(SubmissionSearchValidator.SUBMITTED_DATE_TO).getCode());
  }

  @Test
  void validateShouldOnlyFlagMissingFromDateWhenToDateProvided() {
    final SubmissionsSearchForm form = new SubmissionsSearchForm(null, null, "01/01/2024");
    final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

    validator.validate(form, errors);

    assertTrue(errors.hasFieldErrors(SubmissionSearchValidator.SUBMITTED_DATE_FROM));
    assertFalse(errors.hasFieldErrors(SubmissionSearchValidator.SUBMITTED_DATE_TO));
    assertEquals(
        "search.error.date.from.requiredWithTo",
        errors.getFieldError(SubmissionSearchValidator.SUBMITTED_DATE_FROM).getCode());
  }
}
