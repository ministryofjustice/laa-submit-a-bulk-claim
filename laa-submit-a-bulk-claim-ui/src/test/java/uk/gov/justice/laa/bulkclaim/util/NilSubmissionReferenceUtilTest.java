package uk.gov.justice.laa.bulkclaim.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.constants.AreaOfLawConstants.CRIME_LOWER;
import static uk.gov.justice.laa.bulkclaim.constants.AreaOfLawConstants.LEGAL_HELP;
import static uk.gov.justice.laa.bulkclaim.constants.AreaOfLawConstants.MEDIATION;

import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

@ExtendWith(MockitoExtension.class)
@DisplayName("Nil submission reference util tests")
class NilSubmissionReferenceUtilTest {

  @Mock private MessageSource messageSource;

  @InjectMocks private NilSubmissionReferenceUtil util;

  @Test
  void shouldReturnLegalHelpSubmissionReference() {
    String suffix = "reference";
    String expectedLabel = "nilSubmission.civil.reference";
    String expectedValue = "Civil submission reference";

    when(messageSource.getMessage(expectedLabel, null, Locale.UK)).thenReturn(expectedValue);

    String result = util.getSubmissionReferenceByAreaOfLaw(LEGAL_HELP, suffix);

    assertEquals(expectedValue, result);
    verify(messageSource).getMessage(expectedLabel, null, Locale.UK);
  }

  @Test
  void shouldReturnMediationSubmissionReference() {
    String suffix = "reference";
    String expectedLabel = "nilSubmission.mediation.reference";
    String expectedValue = "Mediation submission reference";

    when(messageSource.getMessage(expectedLabel, null, Locale.UK)).thenReturn(expectedValue);

    String result = util.getSubmissionReferenceByAreaOfLaw(MEDIATION, suffix);

    assertEquals(expectedValue, result);
    verify(messageSource).getMessage(expectedLabel, null, Locale.UK);
  }

  @Test
  void shouldReturnCrimeSubmissionReference() {
    String suffix = "reference";
    String expectedKey = "nilSubmission.crime.reference";
    String expectedValue = "Crime schedule number";

    when(messageSource.getMessage(expectedKey, null, Locale.UK)).thenReturn(expectedValue);

    String result = util.getSubmissionReferenceByAreaOfLaw(CRIME_LOWER, suffix);

    assertEquals(expectedValue, result);
    verify(messageSource).getMessage(expectedKey, null, Locale.UK);
  }

  @Test
  void shouldThrowExceptionForUnknownAreaOfLaw() {
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> util.getSubmissionReferenceByAreaOfLaw("UNKNOWN", "reference"));

    assertEquals("Unexpected value: UNKNOWN", exception.getMessage());
  }
}
