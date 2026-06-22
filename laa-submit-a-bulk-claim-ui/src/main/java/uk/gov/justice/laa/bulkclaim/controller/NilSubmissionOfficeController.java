package uk.gov.justice.laa.bulkclaim.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;

@Controller
@RequiredArgsConstructor
@SessionAttributes("nilSubmissionForm")
public class NilSubmissionOfficeController {

    private final OidcAttributeUtils oidcAttributeUtils;
    private final FeatureFlagsConfig featureFlagsConfig;

    @ModelAttribute("nilSubmissionForm")
    public NilSubmissionForm nilSubmissionForm() {
        return new NilSubmissionForm();
    }

    @GetMapping("/nil-submission")
    public String getNilSubmission(
            @AuthenticationPrincipal OidcUser oidcUser,
            Model model) {

        if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
            return "error";
        }

        model.addAttribute(
                "userOffices",
                oidcAttributeUtils.getUserOffices(oidcUser));

        return "pages/nil-submission-office";
    }

    @PostMapping("/nil-submission-office")
    public String getNilSubmissionOffice(
            @ModelAttribute("nilSubmissionForm") NilSubmissionForm form,
            Model model,
            @RequestParam String office) {

        form.setOffice(office);
        //    if (selection != null) {
      model.addAttribute("selectedOffice", office);
//    }

        return "redirect:/nil-submission-areaoflaw";
    }
}