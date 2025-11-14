package uk.gov.justice.laa.bulkclaim.mapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageType;

@ExtendWith(SpringExtension.class)
@DisplayName("Bulk claim summary mapper test")
class BulkClaimImportSummaryMapperTest {

  private BulkClaimImportSummaryMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new BulkClaimImportSummaryMapperImpl();
  }

  @Test
  @DisplayName("Should map submission summary row")
  void shouldMapSubmissionSummaryRow() {
    // Given
    SubmissionResponse submission200Response =
        SubmissionResponse.builder()
            .submitted(OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC))
            .submissionId(UUID.fromString("ee92c4ac-0ff9-4896-8bbe-c58fa04206e3"))
            .officeAccountNumber("1234567890")
            .areaOfLaw(AreaOfLaw.LEGAL_HELP)
            .submissionPeriod("MAY-2020")
            .numberOfClaims(123)
            .build();
    // When
    List<SubmissionSummaryRow> resultList =
        mapper.toSubmissionSummaryRows(List.of(submission200Response));
    // Then
    SoftAssertions.assertSoftly(
        softly -> {
          SubmissionSummaryRow result = resultList.getFirst();
          softly
              .assertThat(result.submitted())
              .isEqualTo(OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC));
          softly
              .assertThat(result.submissionReference())
              .isEqualTo(UUID.fromString("ee92c4ac-0ff9-4896-8bbe-c58fa04206e3"));
          softly.assertThat(result.officeAccount()).isEqualTo("1234567890");
          softly.assertThat(result.areaOfLaw()).isEqualTo("LEGAL HELP");
          softly.assertThat(result.submissionPeriod()).isEqualTo("2020-05-01");
          softly.assertThat(result.totalClaims()).isEqualTo(123);
        });
  }

  @Test
  @DisplayName("Should map submission summary claim errors when primary client name is present")
  void shouldMapSubmissionSummaryClaimErrorsWithPrimaryClient() {
    UUID submissionId = UUID.fromString("ee92c4ac-0ff9-4896-8bbe-c58fa04206e3");

    ValidationMessageBase errors =
        new ValidationMessageBase()
            .type(ValidationMessageType.ERROR)
            .submissionId(submissionId)
            .displayMessage("This is an error!");

    ClaimResponse claimResponse = new ClaimResponse();
    claimResponse.setUniqueFileNumber("F123");
    claimResponse.setUniqueClientNumber("C123");
    claimResponse.setClientForename("First");
    claimResponse.setClientSurname("Last");

    MessageRow result = mapper.toSubmissionSummaryClaimMessage(errors, claimResponse);

    SoftAssertions.assertSoftly(
        softly -> {
          softly.assertThat(result.submissionReference()).isEqualTo(submissionId);
          softly.assertThat(result.ufn()).isEqualTo("F123");
          softly.assertThat(result.ucn()).isEqualTo("C123");
          softly.assertThat(result.client()).isEqualTo("First Last");
          softly.assertThat(result.clientForename()).isEqualTo("First");
          softly.assertThat(result.clientSurname()).isEqualTo("Last");
          softly.assertThat(result.client2Forename()).isNull();
          softly.assertThat(result.client2Surname()).isNull();
          softly.assertThat(result.client2Ucn()).isNull();
          softly.assertThat(result.crimeMatterTypeCode()).isNull();
          softly.assertThat(result.message()).isEqualTo("This is an error!");
          softly.assertThat(result.type()).isEqualTo("ERROR");
        });
  }

  @Test
  @DisplayName("Should map submission summary claim warnings when primary client name is present")
  void shouldMapSubmissionSummaryClaimWarningsWithPrimaryClient() {
    UUID submissionId = UUID.fromString("ee92c4ac-0ff9-4896-8bbe-c58fa04206e3");

    ValidationMessageBase errors =
        new ValidationMessageBase()
            .type(ValidationMessageType.WARNING)
            .submissionId(submissionId)
            .displayMessage("This is an error!");

    ClaimResponse claimResponse = new ClaimResponse();
    claimResponse.setUniqueFileNumber("F123");
    claimResponse.setUniqueClientNumber("C123");
    claimResponse.setClientForename("First");
    claimResponse.setClientSurname("Last");

    MessageRow result = mapper.toSubmissionSummaryClaimMessage(errors, claimResponse);

    SoftAssertions.assertSoftly(
        softly -> {
          softly.assertThat(result.submissionReference()).isEqualTo(submissionId);
          softly.assertThat(result.ufn()).isEqualTo("F123");
          softly.assertThat(result.ucn()).isEqualTo("C123");
          softly.assertThat(result.client()).isEqualTo("First Last");
          softly.assertThat(result.clientForename()).isEqualTo("First");
          softly.assertThat(result.clientSurname()).isEqualTo("Last");
          softly.assertThat(result.client2Forename()).isNull();
          softly.assertThat(result.client2Surname()).isNull();
          softly.assertThat(result.client2Ucn()).isNull();
          softly.assertThat(result.crimeMatterTypeCode()).isNull();
          softly.assertThat(result.message()).isEqualTo("This is an error!");
          softly.assertThat(result.type()).isEqualTo("WARNING");
        });
  }

  @Test
  @DisplayName("Should fallback to secondary client when primary client name is blank")
  void shouldFallbackToSecondaryClient() {
    ValidationMessageBase errors =
        new ValidationMessageBase().submissionId(UUID.randomUUID()).displayMessage("Error!");

    ClaimResponse claimResponse = new ClaimResponse();
    claimResponse.setClientForename("");
    claimResponse.setClientSurname("");
    claimResponse.setClient2Forename("Second");
    claimResponse.setClient2Surname("Client");

    MessageRow result = mapper.toSubmissionSummaryClaimMessage(errors, claimResponse);

    SoftAssertions.assertSoftly(
        softly -> {
          softly.assertThat(result.client()).isEqualTo("Second Client");
          softly.assertThat(result.clientForename()).isEmpty();
          softly.assertThat(result.clientSurname()).isEmpty();
          softly.assertThat(result.client2Forename()).isEqualTo("Second");
          softly.assertThat(result.client2Surname()).isEqualTo("Client");
          softly.assertThat(result.client2Ucn()).isNull();
          softly.assertThat(result.crimeMatterTypeCode()).isNull();
          softly.assertThat(result.message()).isEqualTo("Error!");
        });
  }

  @Test
  @DisplayName("Should return null client name when no names are provided")
  void shouldReturnNullClientNameWhenNoNames() {
    ValidationMessageBase errors =
        new ValidationMessageBase().submissionId(UUID.randomUUID()).displayMessage("Error!");

    ClaimResponse claimResponse = new ClaimResponse();

    MessageRow result = mapper.toSubmissionSummaryClaimMessage(errors, claimResponse);

    SoftAssertions.assertSoftly(
        softly -> {
          softly.assertThat(result.client()).isNull();
          softly.assertThat(result.clientForename()).isNull();
          softly.assertThat(result.clientSurname()).isNull();
          softly.assertThat(result.client2Forename()).isNull();
          softly.assertThat(result.client2Surname()).isNull();
          softly.assertThat(result.client2Ucn()).isNull();
          softly.assertThat(result.crimeMatterTypeCode()).isNull();
          softly.assertThat(result.message()).isEqualTo("Error!");
        });
  }
}
