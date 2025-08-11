package uk.gov.justice.laa.bulkclaim.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ImportCompleteController {


  @GetMapping("/import-successful")
  public String importComplete(){
    return "pages/_import-successful";
  }

  @GetMapping("/import-failed")
  public String importFailed(){
    return "pages/_import-failed";
  }

  @GetMapping("/submission")
  public String viewSubmission(){
    return "pages/_submission";
  }
}
