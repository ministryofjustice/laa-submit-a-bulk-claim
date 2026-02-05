package uk.gov.justice.laa.bulkclaim.validation;

import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;

/** Validates the submission's search. */
@Component
public class SubmissionSearchValidator implements Validator {

  public static final String SUBMISSION_ID = "submissionId";
  public static final String SUBMISSION_PERIOD = "submissionPeriod";

  private final SubmissionPeriodUtil submissionPeriodUtil;

  public SubmissionSearchValidator(SubmissionPeriodUtil submissionPeriodUtil) {
    this.submissionPeriodUtil = submissionPeriodUtil;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return SubmissionsSearchForm.class.isAssignableFrom(clazz);
  }

  /** Validates the submission's search form inputs. */
  @Override
  public void validate(Object target, Errors errors) {
    SubmissionsSearchForm form = (SubmissionsSearchForm) target;

    String submissionId = form.submissionId();

    Map<String, String> availableSubmissionPeriods =
        submissionPeriodUtil.getAllPossibleSubmissionPeriods();
    if (StringUtils.isNotBlank(form.submissionPeriod())
        && !availableSubmissionPeriods.containsKey(form.submissionPeriod().toUpperCase())) {
      errors.rejectValue(
          SUBMISSION_PERIOD,
          "search.error.submissionPeriod.invalid",
          "Submission period must be one of the following: " + availableSubmissionPeriods.values());
    }

    // Validate submission ID if present
    if (StringUtils.isNotBlank(submissionId)) {
      try {
        UUID.fromString(submissionId.trim());
      } catch (IllegalArgumentException ex) {
        errors.rejectValue(
            SUBMISSION_ID,
            "search.error.submissionId.invalid",
            "Submission id must be a valid UUID.");
      }
    }
  }
}
