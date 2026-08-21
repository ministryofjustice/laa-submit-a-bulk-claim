package uk.gov.justice.laa.bulkclaim.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.BULK_SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.controller.BulkUploadBeingCheckedController;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.GetBulkSubmissionStatusById200Response;

@WebMvcTest(BulkUploadBeingCheckedController.class)
class BulkUploadBeingCheckedPageViewTest extends ViewTestBase {

  BulkUploadBeingCheckedPageViewTest() {
    this.mapping = "/upload-is-being-checked";
  }

  @Test
  void uploadBeingCheckedPageShowsExpectedContent() {
    stubBulkSubmissionStatus(BulkSubmissionStatus.READY_FOR_PARSING);

    var doc = renderDocument();

    assertPageHasTitle(doc, "Your file is being checked");
    assertPageDoesNotHaveBackLink(doc);
    assertPageHasHeading(doc, "Your file is being checked");
  }

  @Test
  void uploadBeingCheckedPageShowsRefreshMetaTag() {
    stubBulkSubmissionStatus(BulkSubmissionStatus.PARSING_COMPLETED);

    var doc = renderDocument();

    var refreshMeta = doc.selectFirst("meta[http-equiv=refresh]");
    assertThat(refreshMeta).isNotNull();
  }

  private void stubBulkSubmissionStatus(BulkSubmissionStatus status) {
    UUID bulkSubmissionId = UUID.randomUUID();
    session.setAttribute(SUBMISSION_ID, submissionId);
    session.setAttribute(BULK_SUBMISSION_ID, bulkSubmissionId);

    GetBulkSubmissionStatusById200Response bulkSubmissionStatus =
        new GetBulkSubmissionStatusById200Response();
    bulkSubmissionStatus.setStatus(status);

    when(dataClaimsRestClient.getBulkSubmissionSummary(bulkSubmissionId))
        .thenReturn(Mono.just(bulkSubmissionStatus));
  }
}
