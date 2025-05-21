package uk.gov.justice.laa.cwa.bulkupload.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class BulkUploadController {

    @GetMapping("/")
    public String showUploadPage() {
        return "pages/upload";
    }

    @PostMapping("/upload")
    public String performUpload(@RequestParam("fileUpload") MultipartFile file) {
        if (file.isEmpty()) {
            return "pages/upload-failure";
        }
        return "pages/upload-success";
    }
}
