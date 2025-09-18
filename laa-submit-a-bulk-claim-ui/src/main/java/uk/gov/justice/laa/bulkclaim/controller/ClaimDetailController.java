package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({SUBMISSION_ID, CLAIM_ID})
public class ClaimDetailController {

  @GetMapping("/submission/claim/{claimReference}")
  public String getClaimDetail(
      @PathVariable("claimReference") UUID claimReference, HttpSession httpSession) {
    httpSession.setAttribute(CLAIM_ID, claimReference);
    return "redirect:/view-claim-detail";
  }

  @GetMapping("/view-claim-detail")
  public String getClaimDetail(HttpSession httpSession) {
    return "pages/view-claim-detail";
  }
}
