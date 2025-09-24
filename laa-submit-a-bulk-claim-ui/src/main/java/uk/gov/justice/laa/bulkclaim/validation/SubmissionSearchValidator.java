package uk.gov.justice.laa.bulkclaim.validation;

import java.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;

/** Validates the submission's search. */
@Component
public class SubmissionSearchValidator implements Validator {

  private static final int SEARCH_TERM_MIN_LENGTH = 2;
  private static final int SEARCH_TERM_MAX_LENGTH = 100;

  @Override
  public boolean supports(Class<?> clazz) {
    return SubmissionsSearchForm.class.isAssignableFrom(clazz);
  }

  /** Validates the submission's search form inputs. */
  @Override
  public void validate(Object target, Errors errors) {
    SubmissionsSearchForm form = (SubmissionsSearchForm) target;

    String submissionId = form.submissionId();
    LocalDate from = form.submittedDateFrom();
    LocalDate to = form.submittedDateTo();

    // Must provide at least one of: submissionId or date range
    if (submissionId == null && from == null && to == null) {
      errors.reject("search.empty", "Provide a submission id or a date range.");
      return;
    }

    // Both dates must be provided together if either is present
    if ((from == null) != (to == null)) {
      errors.rejectValue(
          "submittedDateFrom", "date.range.incomplete", "Both dates must be provided together.");
      errors.rejectValue(
          "submittedDateTo", "date.range.incomplete", "Both dates must be provided together.");
    }

    // From must not be after To
    if (from != null && to != null && from.isAfter(to)) {
      errors.rejectValue(
          "submittedDateFrom", "date.range.invalid", "From date must be on or before To date.");
      errors.rejectValue(
          "submittedDateTo", "date.range.invalid", "To date must be on or after From date.");
    }

    // Optional conservative guard on submission id size
    if (submissionId != null && submissionId.length() > 100) {
      errors.rejectValue("submissionId", "submissionId.length", "Submission id is too long.");
    }
  }
}
