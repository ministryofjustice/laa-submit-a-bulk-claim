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
   * Add a context path to the view model.
   *
   * @param model The view model.
   */
  @ModelAttribute("contextPath")
  public void addContextPath(Model model, HttpServletRequest request) {
    log.debug("Adding contextPath to model: {}",
    model.getAttribute("contextPath"));
    if (!model.containsAttribute("contextPath")) {
      model.addAttribute("contextPath", "/");
    }

    model.addAttribute("contextPath", request.getServletPath());
  }
}
