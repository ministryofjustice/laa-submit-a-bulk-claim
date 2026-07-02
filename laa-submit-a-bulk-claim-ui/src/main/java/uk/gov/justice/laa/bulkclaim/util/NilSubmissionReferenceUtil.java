package uk.gov.justice.laa.bulkclaim.util;

import static uk.gov.justice.laa.bulkclaim.constants.AreaOfLawConstants.CRIME_LOWER;
import static uk.gov.justice.laa.bulkclaim.constants.AreaOfLawConstants.LEGAL_HELP;
import static uk.gov.justice.laa.bulkclaim.constants.AreaOfLawConstants.MEDIATION;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NilSubmissionReferenceUtil {
  private final MessageSource messageSource;

  public String getSubmissionReferenceByAreaOfLaw(String areaOfLaw, String messageSuffix) {
    String label =
        switch (areaOfLaw) {
          case LEGAL_HELP -> "nilSubmission.civil." + messageSuffix;
          case MEDIATION -> "nilSubmission.mediation." + messageSuffix;
          case CRIME_LOWER -> "nilSubmission.crime." + messageSuffix;
          default -> throw new IllegalStateException("Unexpected value: " + areaOfLaw);
        };
    return messageSource.getMessage(label, null, Locale.UK);
  }
}
