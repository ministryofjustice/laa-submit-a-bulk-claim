package uk.gov.justice.laa.bulkclaim.validation;

import java.time.LocalDate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;

/** Validates the submission's search. */
@Component
public class SubmissionSearchValidator implements Validator {

  private static final int SEARCH_TERM_MIN_LENGTH = 2;
  private static final int SEARCH_TERM_MAX_LENGTH = 100;
  public static final String SUBMITTED_DATE_FROM = "submittedDateFrom";
  public static final String SUBMITTED_DATE_TO = "submittedDateTo";

  @Override
  public boolean supports(Class<?> clazz) {
    return SubmissionsSearchForm.class.isAssignableFrom(clazz);
  }

  /** Validates the submission's search form inputs. */
  @Override
  public void validate(Object target, Errors errors) {
    SubmissionsSearchForm form = (SubmissionsSearchForm) target;

    String submissionId = form.submissionId();
    String from = form.submittedDateFrom();
    String to = form.submittedDateTo();

    // Must provide at least one of: submissionId or date range
    if (submissionId == null && StringUtils.isNotEmpty(from) && StringUtils.isNotEmpty(to)) {
      errors.reject("search.empty", "Provide a submission id or a date range.");
      return;
    }

    // Both dates must be provided together if either is present
    if ((StringUtils.isNotEmpty(from)) != (StringUtils.isNotEmpty(to))) {
      errors.rejectValue(
          SUBMITTED_DATE_FROM, "date.range.incomplete", "Both dates must be provided together.");
      errors.rejectValue(
          SUBMITTED_DATE_TO, "date.range.incomplete", "Both dates must be provided together.");
    }

    // Check date formats
    LocalDate dateFrom = null;
    LocalDate dateTo = null;
    if (StringUtils.isNotEmpty(from)) {
      try {
        dateFrom = LocalDate.parse(from);
      } catch (Exception e) {
        errors.rejectValue(
            SUBMITTED_DATE_FROM, "search.error.date.from.invalid", "Invalid date format.");
      }
    }

    if (StringUtils.isNotEmpty(to)) {
      try {
        dateTo = LocalDate.parse(to);
      } catch (Exception e) {
        errors.rejectValue(
            SUBMITTED_DATE_TO, "search.error.date.to.invalid", "Invalid date format.");
      }
    }

    // From must not be after To
    if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
      errors.rejectValue(
          SUBMITTED_DATE_FROM, "date.range.invalid", "From date must be on or before To date.");
      errors.rejectValue(
          SUBMITTED_DATE_TO, "date.range.invalid", "From date must be on or before To date.");
    }

    // Optional conservative guard on submission id size
    if (submissionId != null && submissionId.length() > 100) {
      errors.rejectValue("submissionId", "submissionId.length", "Submission id is too long.");
    }
  }
}
