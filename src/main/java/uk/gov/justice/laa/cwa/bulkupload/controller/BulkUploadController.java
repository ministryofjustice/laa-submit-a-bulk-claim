package uk.gov.justice.laa.cwa.bulkupload.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for handling the bulk upload requests.
 */
@Controller
public class BulkUploadController {

    @GetMapping("/")
    public String showUploadPage() {
        return "pages/upload";
    }

    /**
     * Performs a bulk uploaded for the given file.
     *
     * @param file the file to be uploaded
     * @return the fee
     */
    @PostMapping("/upload")
    public String performUpload(@RequestParam("fileUpload") MultipartFile file) {
        if (file.isEmpty()) {
            return "pages/upload-failure";
        }
        return "pages/upload-success";
    }
}
