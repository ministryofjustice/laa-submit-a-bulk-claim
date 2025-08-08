package uk.gov.justice.laa.bulkclaim.controller;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ImportInProgressController {

  @GetMapping("/upload/{bulkSubmissionId}/import-in-progress")
  public String importInProgress(Model model, @PathVariable("bulkSubmissionId")
  UUID bulkSubmissionId) {
    // TODO: GET BULK SUBMISSION AS SESSION ATTRIBUTE
    return "pages/upload-in-progress";
  }

}
