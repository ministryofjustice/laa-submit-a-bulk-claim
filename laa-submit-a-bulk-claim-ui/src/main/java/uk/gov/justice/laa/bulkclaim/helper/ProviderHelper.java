package uk.gov.justice.laa.bulkclaim.helper;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

/** Helper class to fetch and populate providers in the model. */
@Component
@Slf4j
public class ProviderHelper {

  /**
   * Populates the model with the list of providers.
   *
   * @param model the model to be populated.
   */
  public void populateProviders(Model model, String username) {
    // Remove this once backend api implemented
    // log.info("providers: {}", cwaUploadService.getProviders(username));
    model.addAttribute("providers", Collections.emptyList());
  }
}
