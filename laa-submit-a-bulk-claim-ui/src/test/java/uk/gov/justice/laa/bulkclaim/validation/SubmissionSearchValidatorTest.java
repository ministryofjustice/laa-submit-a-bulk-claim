package uk.gov.justice.laa.bulkclaim.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator.AREA_OF_LAW;
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
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

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
      final SubmissionsSearchForm form =
          SubmissionsSearchForm.builder().submissionPeriod("APR-2025").build();
      final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

      when(submissionPeriodUtil.getAllPossibleSubmissionPeriods())
          .thenReturn(new LinkedHashMap<>());

      validator.validate(form, errors);

      assertTrue(errors.hasFieldErrors(SUBMISSION_PERIOD));
      assertEquals(
          "search.error.submissionPeriod.invalid",
          errors.getFieldError(SUBMISSION_PERIOD).getCode());
    }

    @Test
    @DisplayName("Should accept submissionPeriod when empty")
    void shouldAcceptSubmissionPeriodWhenEmpty() {
      final SubmissionsSearchForm form = SubmissionsSearchForm.builder().build();
      final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

      when(submissionPeriodUtil.getAllPossibleSubmissionPeriods())
          .thenReturn(new LinkedHashMap<>());

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

  @Nested
  @DisplayName("Validate areaOfLaw")
  class ValidateAreaOfLaw {

    @ParameterizedTest
    @EnumSource(AreaOfLaw.class)
    @DisplayName("Should have no errors when valid areaOfLaw")
    void shouldHaveNoErrorsWhenValidAreaOfLaw(AreaOfLaw areaOfLaw) {
      // Given
      SubmissionsSearchForm form =
          SubmissionsSearchForm.builder().areaOfLaw(areaOfLaw.getValue()).build();
      final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

      // When
      validator.validate(form, errors);
      // Then
      assertFalse(errors.hasFieldErrors(AREA_OF_LAW));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should have no errors when areaOfLaw empty")
    void shouldHaveNoErrorsWhenAreaOfLawEmpty(String areaOfLaw) {
      // Given
      SubmissionsSearchForm form = SubmissionsSearchForm.builder().areaOfLaw(areaOfLaw).build();
      final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

      // When
      validator.validate(form, errors);

      // Then
      assertFalse(errors.hasFieldErrors(AREA_OF_LAW));
    }

    @Test
    @DisplayName("Should have error when invalid value")
    void shouldHaveErrorWhenInvalidValue() {
      // Given
      SubmissionsSearchForm form = SubmissionsSearchForm.builder().areaOfLaw("ABC").build();
      final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

      // When
      validator.validate(form, errors);

      // Then
      assertTrue(errors.hasFieldErrors(AREA_OF_LAW));
      assertEquals("search.error.areaOfLaw.invalid", errors.getFieldError(AREA_OF_LAW).getCode());
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
      SubmissionsSearchForm form =
          SubmissionsSearchForm.builder().submissionStatuses(submissionStatus).build();
      final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

      // When
      validator.validate(form, errors);
      // Then
      assertFalse(errors.hasFieldErrors(SUBMISSION_STATUS));
    }

    @Test
    @DisplayName("Should have errors when submission status empty")
    void shouldHaveErrorsWhenSubmissionStatusEmpty() {
      // Given
      SubmissionsSearchForm form = SubmissionsSearchForm.builder().build();
      final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

      // When
      validator.validate(form, errors);

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
      final SubmissionsSearchForm form =
          SubmissionsSearchForm.builder().offices(List.of("ABC")).build();
      final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

      when(submissionPeriodUtil.getAllPossibleSubmissionPeriods())
          .thenReturn(new LinkedHashMap<>());

      // When
      validator.validate(form, errors);

      // Then
      assertFalse(errors.hasFieldErrors(OFFICES));
    }

    @Test
    @DisplayName("Should have no errors when multiple office accounts")
    void shouldHaveNoErrorsWhenMultipleOfficeAccounts() {
      // Given
      final SubmissionsSearchForm form =
          SubmissionsSearchForm.builder().offices(List.of("ABC", "DEF", "GHI")).build();
      final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

      when(submissionPeriodUtil.getAllPossibleSubmissionPeriods())
          .thenReturn(new LinkedHashMap<>());

      // When
      validator.validate(form, errors);

      // Then
      assertFalse(errors.hasFieldErrors(OFFICES));
    }

    @Test
    @DisplayName("Should have errors when no office account added")
    void shouldHaveErrorsWhenNoOfficeAccountAdded() {
      // Given
      final SubmissionsSearchForm form =
          SubmissionsSearchForm.builder().offices(Collections.emptyList()).build();
      final BindingResult errors = new BeanPropertyBindingResult(form, "submissionsSearchForm");

      when(submissionPeriodUtil.getAllPossibleSubmissionPeriods())
          .thenReturn(new LinkedHashMap<>());

      // When
      validator.validate(form, errors);

      // Then
      assertTrue(errors.hasFieldErrors(OFFICES));
      assertEquals("search.error.offices.empty", errors.getFieldError(OFFICES).getCode());
    }
  }
}
