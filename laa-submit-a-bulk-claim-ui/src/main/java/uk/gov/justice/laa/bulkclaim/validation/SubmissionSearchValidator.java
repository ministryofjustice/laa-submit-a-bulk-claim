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

    if (submissionId == null && StringUtils.isNotEmpty(from) && StringUtils.isNotEmpty(to)) {
      errors.reject("search.empty", "Provide a submission id or a date range.");
      return;
    }

    // Both dates must be provided together if either is present
    boolean fromProvided = StringUtils.isNotEmpty(from);
    boolean toProvided = StringUtils.isNotEmpty(to);
    if (fromProvided ^ toProvided) {
      if (!fromProvided) {
        errors.rejectValue(
            SUBMITTED_DATE_FROM,
            "search.error.date.from.requiredWithTo",
            "Enter the submission from date when you enter a submission to date.");
      }
      if (!toProvided) {
        errors.rejectValue(
            SUBMITTED_DATE_TO,
            "search.error.date.to.requiredWithFrom",
            "Enter the submission to date when you enter a submission from date.");
      }
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

    if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
      errors.rejectValue(
          SUBMITTED_DATE_FROM, "date.range.invalid", "Submission from date must be on or before submission to date.");
      errors.rejectValue(
          SUBMITTED_DATE_TO, "date.range.invalid", "Submission to date must be on or after submission from date.");
    }

    if (StringUtils.isNotBlank(submissionId)) {
      try {
        UUID.fromString(submissionId.trim());
      } catch (IllegalArgumentException ex) {
        errors.rejectValue(
            "submissionId", "search.submissionId.invalid", "Submission id must be a valid UUID.");
      }
    LocalDate maxDate = LocalDate.now().plusDays(1);

    if (dateFrom != null && dateFrom.isBefore(maxDate)) {
      errors.rejectValue(
          SUBMITTED_DATE_FROM, "date.range.invalid", "Submission from date must be on or before today.");
    }

    if (dateTo != null && dateTo.isBefore(maxDate)) {
      errors.rejectValue(
          SUBMITTED_DATE_TO, "date.range.invalid", "Submission to date must be on or before today.");
    }

    if (submissionId != null
        && submissionId.length() >= SEARCH_TERM_MIN_LENGTH
        && submissionId.length() <= SEARCH_TERM_MAX_LENGTH) {
      errors.rejectValue("submissionId", "submissionId.length", "Submission id is too long.");
    }
  }
}
