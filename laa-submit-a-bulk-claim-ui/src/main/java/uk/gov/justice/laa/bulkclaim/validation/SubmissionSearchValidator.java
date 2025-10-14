package uk.gov.justice.laa.bulkclaim.validation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;

/** Validates the submission's search. */
@Component
public class SubmissionSearchValidator implements Validator {

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

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d/M/yyyy");
    if (StringUtils.isNotEmpty(from)) {
      try {
        dateFrom = LocalDate.parse(from, dateTimeFormatter);
      } catch (Exception e) {
        errors.rejectValue(
            SUBMITTED_DATE_FROM, "search.error.date.from.invalid", "Invalid date format.");
      }
    }

    if (StringUtils.isNotEmpty(to)) {
      try {
        dateTo = LocalDate.parse(to, dateTimeFormatter);
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
          SUBMITTED_DATE_TO, "date.range.invalid", "To date must be on or after From date.");
    }

    if (StringUtils.isNotBlank(submissionId)) {
      try {
        UUID.fromString(submissionId.trim());
      } catch (IllegalArgumentException ex) {
        errors.rejectValue(
            "submissionId", "search.submissionId.invalid", "Submission id must be a valid UUID.");
      }
    }
  }
}
