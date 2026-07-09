package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.bulkclaim.controller.BaseControllerTest;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;

class NilSubmissionOfficeControllerTest extends BaseControllerTest {

  @Mock private OidcAttributeUtils oidcAttributeUtils;
  @Mock private Model model;

  @InjectMocks private NilSubmissionOfficeController controller;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void whenFeatureFlagDisabled_all_mappings_returnsErrorView() {

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "isNilSubmissionEnabled is false"))
        .when(featureFlagsConfig)
        .checkNilSubmissionEnabled();

    NilSubmissionForm form = new NilSubmissionForm();

    assertThrows(
        ResponseStatusException.class,
        () -> controller.getNilSubmissionOffice(form, getOidcUser(), model));
    verify(model, never()).addAttribute(eq("userOffices"), any());

    assertThrows(
        ResponseStatusException.class,
        () -> controller.postNilSubmissionOffice(form, model, "OfficeA"));
    assertNull(form.getOffice());
  }

  @Test
  void whenFeatureFlagEnabled_getOffice_addsAreasAndReturnsView() {

    List<String> offices = List.of("officeA", "officeB");
    doReturn(offices).when(oidcAttributeUtils).getUserOffices(any(OidcUser.class));

    NilSubmissionForm form = new NilSubmissionForm();
    String view = controller.getNilSubmissionOffice(form, getOidcUser(), model);

    assertEquals("pages/nil-submission/office", view);
    verify(model).addAttribute("userOffices", offices);
  }

  @Test
  void whenFeatureFlagEnabled_getOffice_no_offices_return_info_message() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);
    List<String> offices = List.of();
    doReturn(offices).when(oidcAttributeUtils).getUserOffices(any(OidcUser.class));

    NilSubmissionForm form = new NilSubmissionForm();
    String view = controller.getNilSubmissionOffice(form, getOidcUser(), model);

    assertEquals("pages/nil-submission/info-message", view);
    verify(model, never()).addAttribute(eq("userOffices"), any());
  }

  @Test
  void postOffice_setsFormAndRedirects() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");

    String view = controller.postNilSubmissionOffice(form, model, "OfficeA");

    assertEquals("redirect:/nil-submission/areaoflaw", view);
    assertEquals("OfficeA", form.getOffice());
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
    assertNull(form.getOffice());
    assertNull(form.getAreaOfLaw());
    assertNull(form.getSubmissionPeriod());
    assertNull(form.getScheduleReference());
  }
}
