package uk.gov.justice.laa.bulkclaim.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@SessionAttributes("nilSubmissionForm")
public class NilSubmissionAreaOfLawController {

    private final FeatureFlagsConfig featureFlagsConfig;

    @GetMapping("/nil-submission-areaoflaw")
    public String getPage(
            @ModelAttribute("nilSubmissionForm") NilSubmissionForm form,
            Model model) {

        if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
            return "error";
        }
        Set<String> areasOfLaw =
                Arrays.stream(AreaOfLaw.values())
                        .map(Enum::name)
                        .collect(Collectors.toSet());
        model.addAttribute("areasOfLaw", areasOfLaw);
        return "pages/nil-submission-areaoflaw";
    }

    @PostMapping("/nil-submission-areaoflaw")
    public String postPage(
            @ModelAttribute("nilSubmissionForm") NilSubmissionForm form,
            @RequestParam String areaOfLaw) {

        form.setAreaOfLaw(areaOfLaw);

        return "redirect:/nil-submission/" + form.getOffice() ;
    }
}