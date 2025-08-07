package uk.gov.justice.laa.bulkclaim.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ui.Model;
import uk.gov.justice.laa.bulkclaim.response.CwaVendorDto;
import uk.gov.justice.laa.bulkclaim.service.CwaUploadService;

class ProviderHelperTest {

  private static final String USERNAME = "TESTUSER";

  private CwaUploadService cwaUploadService;
  private ProviderHelper providerHelper;
  private Model model;
  private Principal principal;

  @BeforeEach
  void setUp() {
    cwaUploadService = mock(CwaUploadService.class);
    providerHelper = new ProviderHelper(cwaUploadService);
    model = mock(Model.class);
    principal = mock(Principal.class);
    when(principal.getName()).thenReturn("TestUser");
  }

  @Test
  void populateProviders_shouldAddProvidersToModel() {
    List<CwaVendorDto> providers = List.of(new CwaVendorDto());
    when(cwaUploadService.getProviders(USERNAME)).thenReturn(providers);

    providerHelper.populateProviders(model, USERNAME);

    verify(cwaUploadService).getProviders(USERNAME);
    verify(model).addAttribute("providers", Collections.emptyList());
  }

  @Test
  void populateProviders_shouldAddEmptyListIfNoProviders() {
    when(cwaUploadService.getProviders(USERNAME)).thenReturn(Collections.emptyList());

    providerHelper.populateProviders(model, USERNAME);

    ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
    verify(model).addAttribute(eq("providers"), captor.capture());
    assertThat(captor.getValue()).isEmpty();
  }

  @Test
  void constructor_shouldSetCwaUploadService() {
    assertThat(providerHelper).isNotNull();
  }
}
