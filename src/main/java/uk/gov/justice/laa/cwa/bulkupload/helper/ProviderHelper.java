package uk.gov.justice.laa.cwa.bulkupload.helper;

import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaVendorDto;
import uk.gov.justice.laa.cwa.bulkupload.service.CwaUploadService;

/** Helper class to fetch and populate providers in the model. */
@Component
public class ProviderHelper {

  private final CwaUploadService cwaUploadService;

  /**
   * Constructor for ProviderHelper.
   *
   * @param cwaUploadService the service to fetch providers.
   */
  public ProviderHelper(CwaUploadService cwaUploadService) {
    this.cwaUploadService = cwaUploadService;
  }

  /**
   * Populates the model with the list of providers.
   *
   * @param model the model to be populated.
   */
  public void populateProviders(Model model, Principal principal) {
    List<CwaVendorDto> providers = cwaUploadService.getProviders(principal.getName().toUpperCase());
    model.addAttribute("providers", providers);
  }
}
