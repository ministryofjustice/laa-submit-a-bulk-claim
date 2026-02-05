package uk.gov.justice.laa.bulkclaim.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator.SUBMISSION_PERIOD;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("Submission search validator test")
class SubmissionSearchValidatorTest {

  @Mock private SubmissionPeriodUtil submissionPeriodUtil;

  private SubmissionSearchValidator validator;

  @BeforeEach
  void beforeEach() {
    validator = new SubmissionSearchValidator(submissionPeriodUtil);
  }

  @Test
  @DisplayName("Should reject submissionPeriod when not available")
  void validateShouldNotAcceptSubmissionPeriodNotAvailable() {
    final SubmissionsSearchForm form =
        SubmissionsSearchForm.builder().submissionPeriod("APR-2025").build();
    final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

    when(submissionPeriodUtil.getAllPossibleSubmissionPeriods()).thenReturn(new LinkedHashMap<>());

    validator.validate(form, errors);

    assertTrue(errors.hasFieldErrors(SUBMISSION_PERIOD));
    assertEquals(
        "search.error.submissionPeriod.invalid", errors.getFieldError(SUBMISSION_PERIOD).getCode());
  }

  @Test
  @DisplayName("Should accept submissionPeriod when empty")
  void shouldAcceptSubmissionPeriodWhenEmpty() {
    final SubmissionsSearchForm form = SubmissionsSearchForm.builder().build();
    final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

    when(submissionPeriodUtil.getAllPossibleSubmissionPeriods()).thenReturn(new LinkedHashMap<>());

    validator.validate(form, errors);

    assertFalse(errors.hasFieldErrors(SUBMISSION_PERIOD));
  }

  @Test
  @DisplayName("Should accept submissionPeriod when available")
  void shouldAcceptSubmissionPeriodWhenAvailable() {
    final SubmissionsSearchForm form =
        SubmissionsSearchForm.builder().submissionPeriod("APR-2025").build();
    final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

    when(submissionPeriodUtil.getAllPossibleSubmissionPeriods())
        .thenReturn(Map.of("APR-2025", "April 2025"));

    validator.validate(form, errors);

    assertFalse(errors.hasFieldErrors(SUBMISSION_PERIOD));
  }
}
