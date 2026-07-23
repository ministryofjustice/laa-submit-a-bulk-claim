package uk.gov.justice.laa.bulkclaim.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator.OFFICES;
import static uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator.SUBMISSION_PERIOD;
import static uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator.SUBMISSION_STATUS;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchQuery;
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

  @Nested
  @DisplayName("Validate submissionPeriod")
  class ValidateSubmissionPeriod {

    @Test
    @DisplayName("Should reject submissionPeriod when not available")
    void validateShouldNotAcceptSubmissionPeriodNotAvailable() {
      final SubmissionSearchQuery query =
          SubmissionSearchQuery.builder().submissionPeriod("APR-2025").build();
      final BindingResult errors = new BeanPropertyBindingResult(query, "submissionSearchQuery");

      when(submissionPeriodUtil.getAllPossibleSubmissionPeriods())
          .thenReturn(new LinkedHashMap<>());

      validator.validate(query, errors);

      assertTrue(errors.hasFieldErrors(SUBMISSION_PERIOD));
      assertEquals(
          "search.error.submissionPeriod.invalid",
          errors.getFieldError(SUBMISSION_PERIOD).getCode());
    }

    @Test
    @DisplayName("Should accept submissionPeriod when empty")
    void shouldAcceptSubmissionPeriodWhenEmpty() {
      final SubmissionSearchQuery query = SubmissionSearchQuery.builder().build();
      final BindingResult errors = new BeanPropertyBindingResult(query, "submissionSearchQuery");

      when(submissionPeriodUtil.getAllPossibleSubmissionPeriods())
          .thenReturn(new LinkedHashMap<>());

      validator.validate(query, errors);

      assertFalse(errors.hasFieldErrors(SUBMISSION_PERIOD));
    }

    @Test
    @DisplayName("Should accept submissionPeriod when available")
    void shouldAcceptSubmissionPeriodWhenAvailable() {
      final SubmissionSearchQuery query =
          SubmissionSearchQuery.builder().submissionPeriod("APR-2025").build();
      final BindingResult errors = new BeanPropertyBindingResult(query, "submissionSearchQuery");

      when(submissionPeriodUtil.getAllPossibleSubmissionPeriods())
          .thenReturn(Map.of("APR-2025", "April 2025"));

      validator.validate(query, errors);

      assertFalse(errors.hasFieldErrors(SUBMISSION_PERIOD));
    }
  }

  @Nested
  @DisplayName("Validate submission status")
  class ValidateSubmissionStatus {

    @ParameterizedTest
    @EnumSource(SubmissionOutcomeFilter.class)
    @DisplayName("Should have no errors when valid submission status")
    void shouldHaveNoErrorsWhenValidSubmissionStatus(SubmissionOutcomeFilter submissionStatus) {
      // Given
      SubmissionSearchQuery query =
          SubmissionSearchQuery.builder().submissionStatuses(submissionStatus).build();
      final BindingResult errors = new BeanPropertyBindingResult(query, "submissionSearchQuery");

      // When
      validator.validate(query, errors);
      // Then
      assertFalse(errors.hasFieldErrors(SUBMISSION_STATUS));
    }

    @Test
    @DisplayName("Should have errors when submission status empty")
    void shouldHaveErrorsWhenSubmissionStatusEmpty() {
      // Given
      SubmissionSearchQuery query = SubmissionSearchQuery.builder().build();
      final BindingResult errors = new BeanPropertyBindingResult(query, "submissionSearchQuery");

      // When
      validator.validate(query, errors);

      // Then
      assertTrue(errors.hasFieldErrors(SUBMISSION_STATUS));
    }
  }

  @Nested
  @DisplayName("Validate office account")
  class ValidateOfficeAccounts {

    @Test
    @DisplayName("Should have no errors when one office account")
    void shouldHaveNoErrorsWhenOneOfficeAccount() {
      // Given
      final SubmissionSearchQuery query =
          SubmissionSearchQuery.builder().offices(List.of("ABC")).build();
      final BindingResult errors = new BeanPropertyBindingResult(query, "submissionSearchQuery");

      when(submissionPeriodUtil.getAllPossibleSubmissionPeriods())
          .thenReturn(new LinkedHashMap<>());

      // When
      validator.validate(query, errors);

      // Then
      assertFalse(errors.hasFieldErrors(OFFICES));
    }

    @Test
    @DisplayName("Should have no errors when multiple office accounts")
    void shouldHaveNoErrorsWhenMultipleOfficeAccounts() {
      // Given
      final SubmissionSearchQuery query =
          SubmissionSearchQuery.builder().offices(List.of("ABC", "DEF", "GHI")).build();
      final BindingResult errors = new BeanPropertyBindingResult(query, "submissionSearchQuery");

      when(submissionPeriodUtil.getAllPossibleSubmissionPeriods())
          .thenReturn(new LinkedHashMap<>());

      // When
      validator.validate(query, errors);

      // Then
      assertFalse(errors.hasFieldErrors(OFFICES));
    }

    @Test
    @DisplayName("Should have errors when no office account added")
    void shouldHaveErrorsWhenNoOfficeAccountAdded() {
      // Given
      final SubmissionSearchQuery query =
          SubmissionSearchQuery.builder().offices(Collections.emptyList()).build();
      final BindingResult errors = new BeanPropertyBindingResult(query, "submissionSearchQuery");

      when(submissionPeriodUtil.getAllPossibleSubmissionPeriods())
          .thenReturn(new LinkedHashMap<>());

      // When
      validator.validate(query, errors);

      // Then
      assertTrue(errors.hasFieldErrors(OFFICES));
      assertEquals("search.error.offices.empty", errors.getFieldError(OFFICES).getCode());
    }
  }
}
