package uk.gov.justice.laa.bulkclaim.validation;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

/**
 * Validator implementation for the {@code SubmissionsSearchForm} class. Performs validation checks
 * on the search form inputs to ensure they meet specific criteria.
 *
 * <p>This class handles the following validation scenarios:
 *
 * <ul>
 *   <li>Validates the submission period to ensure it is one of the available submission periods.
 *   <li>Validates the area of law input to ensure it matches one of the predefined options.
 *   <li>Validates the submission status to ensure it matches one of the predefined options.
 * </ul>
 *
 * <p>Does not handle office validation, as
 * {@link uk.gov.justice.laa.bulkclaim.controller.SearchController} already
 * filters out offices which the user is not part of.</p>
 *
 * <p>Errors and validation failures are reported using the {@code Errors} interface.
 *
 * @author Jamie Briggs
 */
@Component
public class SubmissionSearchValidator implements Validator {

  public static final String SUBMISSION_ID = "submissionId";
  public static final String SUBMISSION_PERIOD = "submissionPeriod";
  public static final String AREA_OF_LAW = "areaOfLaw";
  public static final String SUBMISSION_STATUS = "submissionStatus";

  private final SubmissionPeriodUtil submissionPeriodUtil;

  public SubmissionSearchValidator(SubmissionPeriodUtil submissionPeriodUtil) {
    this.submissionPeriodUtil = submissionPeriodUtil;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return SubmissionsSearchForm.class.isAssignableFrom(clazz);
  }

  /**
   * Validates the submission's search form inputs.
   */
  @Override
  public void validate(Object target, Errors errors) {
    SubmissionsSearchForm form = (SubmissionsSearchForm) target;

    validateSubmissionPeriod(errors, form);
    validateAreaOfLaw(errors, form);
    validateSubmissionStatus(errors, form);
  }

  private void validateSubmissionPeriod(Errors errors, SubmissionsSearchForm form) {
    Map<String, String> availableSubmissionPeriods =
        submissionPeriodUtil.getAllPossibleSubmissionPeriods();
    if (StringUtils.isNotBlank(form.submissionPeriod())
        && !availableSubmissionPeriods.containsKey(form.submissionPeriod().toUpperCase())) {
      errors.rejectValue(
          SUBMISSION_PERIOD,
          "search.error.submissionPeriod.invalid",
          "Submission period must be one of the following: " + availableSubmissionPeriods.values());
    }
  }

  private void validateAreaOfLaw(Errors errors, SubmissionsSearchForm form) {
    if (!StringUtils.isEmpty(form.areaOfLaw())) {
      try {
        AreaOfLaw.fromValue(form.areaOfLaw());
      } catch (IllegalArgumentException e) {
        errors.rejectValue(
            AREA_OF_LAW,
            "search.error.areaOfLaw.invalid",
            "Area of law must be one of the following: " + AreaOfLaw.values());
      }
    }
  }

  private void validateSubmissionStatus(Errors errors, SubmissionsSearchForm form) {
    if (!StringUtils.isEmpty(form.submissionStatus())) {
      try {
        SubmissionStatus.fromValue(form.submissionStatus());
      } catch (IllegalArgumentException e) {
        errors.rejectValue(
            SUBMISSION_STATUS,
            "search.error.submissionStatus.invalid",
            "Area of law must be one of the following: " + AreaOfLaw.values());
      }
    }
  }
}
