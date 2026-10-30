package uk.gov.justice.laa.payments.submit.accessibility.tests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.justice.laa.payments.submit.accessibility.helpers.AbstractAccessibilityTest;
import uk.gov.justice.laa.payments.submit.accessibility.helpers.AccessibilityAxeHelper;
import uk.gov.justice.laa.payments.submit.accessibility.pages.NilSubmissionPage;
import uk.gov.justice.laa.payments.submit.util.OidcAttributeUtils;

@DisplayName("Feature: Nil submission journey for a user with no offices")
class NilSubmissionNoOfficesAccessibilityTest extends AbstractAccessibilityTest {

  @MockitoBean private OidcAttributeUtils oidcAttributeUtils;

  @BeforeEach
  void stubNoOffices() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of());
  }

  @Test
  @DisplayName("Scenario: Info message page accessibility checks")
  void infoMessagePageAccessibilityChecks() throws IOException {
    new NilSubmissionPage(page, appUrl("")).openOffice();
    AccessibilityAxeHelper.assertAccessible(page, "nil-submission-info-message");
  }
}
