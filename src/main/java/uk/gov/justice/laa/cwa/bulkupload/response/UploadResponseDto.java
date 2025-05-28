package uk.gov.justice.laa.cwa.bulkupload.response;

/**
 * The DTO class for upload response.
 */
public class UploadResponseDto {
    private String detail;
    private String success;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}