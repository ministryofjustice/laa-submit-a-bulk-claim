package uk.gov.justice.laa.bulkclaim.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Adds context values to model across controllers. * */
@Log4j2
@ControllerAdvice("uk.gov.justice.laa.bulkclaim.controller")
public class ContextModelAttribute {

  /**
   * Adds the current Url to the view model.
   *
   * @param model view model.
   * @param request current request.
   */
  @ModelAttribute("currentUrl")
  public void currentUrl(Model model, HttpServletRequest request) {
    if (!model.containsAttribute("currentUrl")) {
      String currentUrl =
          (request.getQueryString() != null && request.getQueryString().isEmpty())
              ? request.getRequestURI()
              : request.getRequestURI() + "?" + request.getQueryString();
      currentUrl = currentUrl.replaceAll("&?page=[0-9]+", "");
      model.addAttribute("currentUrl", currentUrl);
      log.info("Adding currentUrl to model: {}", model.getAttribute("currentUrl"));
    }
  }
}
