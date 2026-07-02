package uk.gov.justice.laa.bulkclaim.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.getOidcUser;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;

class NilSubmissionOfficeControllerTest {

  @Mock private FeatureFlagsConfig featureFlagsConfig;
  @Mock private OidcAttributeUtils oidcAttributeUtils;
  @Mock private MessageSource messageSource;
  @Mock private Model model;

  @InjectMocks private NilSubmissionOfficeController controller;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void whenFeatureFlagDisabled_all_mappings_returnsErrorView() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(false);

    NilSubmissionForm form = new NilSubmissionForm();

    assertEquals("error", controller.getNilSubmissionOffice(form, getOidcUser(), model));
    verify(model, never()).addAttribute(eq("userOffices"), any());

    assertEquals("error", controller.postNilSubmissionOffice(form, model, "OfficeA"));
    assertNull(form.getOffice());
  }

  @Test
  void whenFeatureFlagEnabled_getOffice_addsAreasAndReturnsView() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);
    List<String> offices = List.of("officeA", "officeB");
    doReturn(offices).when(oidcAttributeUtils).getUserOffices(any(OidcUser.class));

    NilSubmissionForm form = new NilSubmissionForm();
    String view = controller.getNilSubmissionOffice(form, getOidcUser(), model);

    assertEquals("pages/nil-submission-office", view);
    verify(model).addAttribute("userOffices", offices);
  }

  @Test
  void whenFeatureFlagEnabled_getOffice_no_offices_return_info_message() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);
    List<String> offices = List.of();
    doReturn(offices).when(oidcAttributeUtils).getUserOffices(any(OidcUser.class));

    NilSubmissionForm form = new NilSubmissionForm();
    String view = controller.getNilSubmissionOffice(form, getOidcUser(), model);

    assertEquals("pages/nil-submission-info-message", view);
    verify(model, never()).addAttribute(eq("userOffices"), any());
  }

  @Test
  void postOffice_setsFormAndRedirects() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");

    String view = controller.postNilSubmissionOffice(form, model, "OfficeA");

    assertEquals("redirect:/nil-submission-areaoflaw", view);
    assertEquals("OfficeA", form.getOffice());
  }

  @Test
  void postOffice_whenOfficeIsInvalid_returnsErrorView() {
    NilSubmissionForm form = new NilSubmissionForm();

    String view_on_null = controller.postNilSubmissionOffice(form, model, null);
    assertEquals("error", view_on_null);
    assertNull(form.getOffice());

    String view_on_invalid = controller.postNilSubmissionOffice(form, model, "NOT_A_OFFICE");
    assertEquals("error", view_on_invalid);
    assertNull(form.getOffice());
  }

  @Test
  void getOffice_session_management_cleansing() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw("areaOfLaw1");
    form.setSubmissionPeriod("submissionPeriod1");
    form.setScheduleReference("scheduleReference1");

    controller.getNilSubmissionOffice(form, getOidcUser(), model);
    assertFalse(form.getOffice().isEmpty());
    assertNull(form.getAreaOfLaw());
    assertNull(form.getSubmissionPeriod());
    assertNull(form.getScheduleReference());
  }
}
