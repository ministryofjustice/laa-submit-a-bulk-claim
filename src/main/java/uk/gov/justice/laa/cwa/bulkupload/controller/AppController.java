package uk.gov.justice.laa.cwa.bulkupload.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {

    @GetMapping("/")
    public String upload() {
        return "upload";
    }

    @GetMapping("/success")
    public String success() {
        return "success";
    }

    @GetMapping("/failure")
    public String failure() {
        return "failure";
    }
}
