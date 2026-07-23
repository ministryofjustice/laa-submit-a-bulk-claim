package uk.gov.justice.laa.bulkclaim.validation;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchQuery;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;

/**
 * Validator implementation for the {@code SubmissionSearchQuery} class. Perquerys validation checks
 * on the search query inputs to ensure they meet specific criteria.
 *
 * <p>This class handles the following validation scenarios:
 *
 * <ul>
 *   <li>Validates the submission period to ensure it is one of the available submission periods.
 *   <li>Validates the submission status to ensure it matches one of the predefined options.
 * </ul>
 *
 * <p>Does not handle office validation, as {@link
 * uk.gov.justice.laa.bulkclaim.controller.SearchController} already filters out offices which the
 * user is not part of.
 *
 * <p>Errors and validation failures are reported using the {@code Errors} interface.
 */
@Component
public class SubmissionSearchValidator implements Validator {

  public static final String SUBMISSION_ID = "submissionId";
  public static final String SUBMISSION_PERIOD = "submissionPeriod";
  public static final String SUBMISSION_STATUS = "submissionStatuses";
  public static final String OFFICES = "offices";

  private final SubmissionPeriodUtil submissionPeriodUtil;

  public SubmissionSearchValidator(SubmissionPeriodUtil submissionPeriodUtil) {
    this.submissionPeriodUtil = submissionPeriodUtil;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return SubmissionSearchQuery.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    SubmissionSearchQuery query = (SubmissionSearchQuery) target;

    validateSubmissionPeriod(errors, query);
    validateSubmissionStatus(errors, query);
    validateOffices(errors, query);
  }

  private void validateSubmissionPeriod(Errors errors, SubmissionSearchQuery query) {
    Map<String, String> availableSubmissionPeriods =
        submissionPeriodUtil.getAllPossibleSubmissionPeriods();
    if (StringUtils.isNotBlank(query.getSubmissionPeriod())
        && !availableSubmissionPeriods.containsKey(query.getSubmissionPeriod().toUpperCase())) {
      errors.rejectValue(
          SUBMISSION_PERIOD,
          "search.error.submissionPeriod.invalid",
          "Submission period must be one of the following: " + availableSubmissionPeriods.values());
    }
  }

  private void validateSubmissionStatus(Errors errors, SubmissionSearchQuery query) {
    if (Objects.isNull(query.getSubmissionStatuses())) {
      errors.rejectValue(
          SUBMISSION_STATUS,
          "search.error.submissionOutcome.invalid",
          "Submission status must be one of the following: "
              + Arrays.toString(SubmissionOutcomeFilter.values()));
    }
  }

  private void validateOffices(Errors errors, SubmissionSearchQuery query) {
    if (Objects.isNull(query.getOffices()) || query.getOffices().isEmpty()) {
      errors.rejectValue(
          OFFICES,
          "search.error.offices.empty",
          "Office account must be one of the following: "
              + Arrays.toString(SubmissionOutcomeFilter.values()));
    }
  }
}
