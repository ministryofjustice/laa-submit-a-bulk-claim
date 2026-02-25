package uk.gov.justice.laa.bulkclaim;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import uk.gov.justice.laa.bulkclaim.client.ExportDataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.ClaimsApiPactTestConfig;

/**
 * For this PactTest, it spins up a MockWebServer which is used to act as the API we're testing
 * against (in this case the claims API). After all the tests have run, a pact is generated based on
 * all the passing tests. This pact will be published to the Pact Broker server. The Claims API will
 * then verify itself against the generated pact to ensure it remains compatible with it's
 * consumers.
 *
 * <p>For the various {@link Pact} annotations, a scenario is created. There are multiple parts of a
 * {@link RequestResponsePact}:
 *
 * <ul>
 *   <li>Given: This explains the state of what the Claims API should be in when expecting this
 *       request. For example, if "a claim exists", then the API should make sure it has a Claim to
 *       be used for the request. Given values can be reused across multiple scenarios.
 *   <li>Upon Receiving: This value details the scenario we are testing.
 *   <li>Match Path: The path we wish to match against for the contract.
 *   <li>Match Header: The header we wish to match against (authorization key).
 *   <li>Method: The HTTP method.
 * </ul>
 *
 * @author Jamie Briggs
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"app.claims-api.url=http://localhost:1234"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1234") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /exports/submission-claims-{area-of-law} PACT tests")
public final class GetExportSubmissionPactTest extends AbstractPactTest {

  @Autowired ExportDataClaimsRestClient exportDataClaimsRestClient;

  String legalHelpCsvHeaders =
      "Providers LAA Office Number,Submission Month,Area of Law,Legal Help Submission Reference,"
          + "Fee Code,Schedule Reference,Case Reference Number,Case Start Date,Case ID,UFN,Claim "
          + "Status,Procurement Area,Access Point,Delivery Location,Client Forename,Client "
          + "Surname,Client Date of Birth,UCN,HO UCN,CLA Reference Number,CLA Exemption Code,"
          + "Gender,Ethnicity,Disability,Client Postcode,Case Concluded Date/Case Claimed Date,"
          + "Matter Type (1:2),Case Stage/Level,Advice Time,Travel Time,Waiting Time,Net Profit "
          + "Costs (Including Travel & Waiting / Excluding VAT),Net Disbursement Amount "
          + "(Excluding VAT),Net Cost of Counsel (Excluding VAT),Disbursements VAT amount,Travel "
          + "& Waiting Costs,VAT Indicator,Tolerance indicator,Legacy Case,NIAT Disbursement "
          + "Prior Authority Number,London / Non London rate,Adjourned Hearing Fee,Additional "
          + "Travel Payment,Value of Costs/Damages recovered,Meetings Attended?,Detention Travel "
          + "& Waiting Costs (Excluding VAT),JR / Form Filling (Excluding VAT),Eligible Client,"
          + "Court Location (HPCDS),CMRH Oral,CMRH Telephone,AIT Hearing Centre,Substantive "
          + "hearing?,HO Interview,Local Authority number,Client Type,Stage Reached,Outcome for "
          + "client,Type of Advice,Medical Reports Claimed,Transfer Date,Exemption Criteria "
          + "Satisfied,IRC Surgery,Surgery date,No of clients seen at surgery,No of clients "
          + "resulting in a Legal Help matter opened,Designated Accredited Representative,Postal "
          + "Application Accepted,Mental Health Tribunal reference,Exceptional Case Funding "
          + "Reference,NRM Advice,Follow on work,Calculated Fee Detail - Fee Type,Calculated Fee "
          + "Detail - Category of Law,Calculated Fee Detail - Total Amount,Calculated Fee Detail "
          + "- VAT Indicator,Calculated Fee Detail - VAT Rate Applied,Calculated Fee Detail - "
          + "Calculated VAT Amount,Calculated Fee Detail - Disbursement Amount,Calculated Fee "
          + "Detail - Requested Net Disbursement Amount,Calculated Fee Detail - Disbursement VAT "
          + "Amount,Calculated Fee Detail - Hourly Total Amount,Calculated Fee Detail - Fixed Fee"
          + " Amount,Calculated Fee Detail - Net Profit Costs Amount,Calculated Fee Detail - "
          + "Requested Net Profit Costs Amount,Calculated Fee Detail - Net Cost Of Counsel "
          + "Amount,Calculated Fee Detail - Net Travel Costs Amount,Calculated Fee Detail - Net "
          + "Waiting Costs Amount,Calculated Fee Detail - Detention Travel And Waiting Costs "
          + "Amount,Calculated Fee Detail - JR Form Filling Amount,Calculated Fee Detail - Travel"
          + " And Waiting Costs Amount,Calculated Fee Detail - Bolt On Total Fee Amount,"
          + "Calculated Fee Detail - Bolt On Adjourned Hearing Count,Calculated Fee Detail - Bolt"
          + " On Adjourned Hearing Fee,Calculated Fee Detail - Bolt On CMRH Telephone Count,"
          + "Calculated Fee Detail - Bolt On CMRH Telephone Fee,Calculated Fee Detail - Bolt On "
          + "CMRH Oral Count,Calculated Fee Detail - Bolt On CMRH Oral Fee,Calculated Fee Detail "
          + "- Bolt On Home Office Interview Count,Calculated Fee Detail - Bolt On Home Office "
          + "Interview Fee,Calculated Fee Detail - Bolt On Substantive Hearing Fee,Calculated Fee"
          + " Detail - Escape Case Flag\n";

  String crimeLowerCsvHeaders =
      "Providers LAA Office Number,Submission Month,Area of Law,Crime Lower Schedule Number,Stage"
          + " Reached (Claim Code),Fee Code,Claim Status,Client surname,Client Initial,Gender,"
          + "Ethnicity,Disability,UFN,Representation Order Date,Standard Fee Category,Outcome for"
          + " the client,Matter Type,Net Profit costs (excluding VAT),Net Disbursement amount "
          + "(excluding VAT),Net Travel costs (excluding VAT),Net Waiting costs (excluding VAT),"
          + "VAT Indicator,VAT Disbursements amount,Case concluded date,No. of suspects/ "
          + "defendants,No. of Police Station/court attendances,Police Station/ Court ID/ Prison "
          + "ID,DSCC Number,MAAT ID,Prison law Prior Approval number,Duty Solicitor,Youth Court,"
          + "Scheme ID,Calculated Fee Detail - Fee Code Description,Calculated Fee Detail - Fee "
          + "Type,Calculated Fee Detail - Category of Law,Calculated Fee Detail - Total Amount,"
          + "Calculated Fee Detail - VAT Indicator,Calculated Fee Detail - VAT Rate Applied,"
          + "Calculated Fee Detail - Calculated VAT Amount,Calculated Fee Detail - Disbursement "
          + "Amount,Calculated Fee Detail - Requested Net Disbursement Amount,Calculated Fee "
          + "Detail - Disbursement VAT Amount,Calculated Fee Detail - Hourly Total Amount,"
          + "Calculated Fee Detail - Fixed Fee Amount,Calculated Fee Detail - Net Profit Costs "
          + "Amount,Calculated Fee Detail - Requested Net Profit Costs Amount,Calculated Fee "
          + "Detail - Net Travel Costs Amount,Calculated Fee Detail - Net Waiting Costs Amount,"
          + "Calculated Fee Detail - Travel And Waiting Costs Amount,Calculated Fee Detail - "
          + "Escape Case Flag\n";

  String mediationCsvHeaders =
      "Providers LAA Office Number,Submission Month,Area of Law,Mediation Submission Reference,"
          + "Matter Type (1:2),Fee Code,Case Reference Number,Case Start Date,Claim ID,Unique "
          + "Case ID,Claim Status,Case Concluded Date,Client 1 Forename,Client 1 Surname,Client 1"
          + " Date of Birth,Client 1 UCN,Client 1 Postcode,Client 1 Gender,Client 1 Ethnicity,"
          + "Client 1 Disability,Client 1 Legally Aided?,Client 2 Forename,Client 2 Surname,"
          + "Client 2 Date of Birth,Client 2 UCN,Client 2 Postcode,Client 2 Gender,Client 2 "
          + "Ethnicity,Client 2 Disability,Client 2 Legally Aided?,Number of Mediation Sessions,"
          + "Mediation Time (mins),Outcome,Outreach Location,Referral,VAT Indicator,Net "
          + "Disbursement Amount (Excluding VAT),Disbursements VAT amount,Client 1 Postal "
          + "Application accepted,Client 2 Postal Application accepted,Schedule Reference "
          + "(Outcome),Calculated Fee Detail - Fee Code Description,Calculated Fee Detail - Fee "
          + "Type,Calculated Fee Detail - Category of Law,Calculated Fee Detail - Total Amount,"
          + "Calculated Fee Detail - VAT Indicator,Calculated Fee Detail - VAT Rate Applied,"
          + "Calculated Fee Detail - Calculated VAT Amount,Calculated Fee Detail - Disbursement "
          + "Amount,Calculated Fee Detail - Requested Net Disbursement Amount,Calculated Fee "
          + "Detail - Disbursement VAT Amount,Calculated Fee Detail - Fixed Fee Amount,Calculated"
          + " Fee Detail - Net Profit Costs Amount,Calculated Fee Detail - Requested Net Profit "
          + "Costs Amount,Calculated Fee Detail - Net Travel Costs Amount,Calculated Fee Detail -"
          + " Net Waiting Costs Amount,Calculated Fee Detail - Travel And Waiting Costs Amount\n";

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getLegalHelpSubmission200(PactDslWithProvider builder) {
    // Must match exactly the header row, optionally followed by newline
    String exampleBody = legalHelpCsvHeaders;

    // Defines expected 200 response for existing submission
    return builder
        .given("no data exists for the export")
        .uponReceiving("a request to export a legal help submission")
        .matchPath("/exports/submission-claims-legal-help")
        .matchQuery("submission-id", UUID_REGEX)
        .matchQuery("office", ANY_FORMAT_REGEX)
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .body(exampleBody)
        .toPact();
  }

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getCrimeLowerSubmission200(PactDslWithProvider builder) {
    // Must match exactly the header row, optionally followed by newline
    String exampleBody = crimeLowerCsvHeaders;

    // Defines expected 200 response for existing submission
    return builder
        .given("no data exists for the export")
        .uponReceiving("a request to export a crime lower submission")
        .matchPath("/exports/submission-claims-crime-lower")
        .matchQuery("submission-id", UUID_REGEX)
        .matchQuery("office", ANY_FORMAT_REGEX)
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .body(exampleBody)
        .toPact();
  }

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getMediationSubmission200(PactDslWithProvider builder) {
    // Must match exactly the header row, optionally followed by newline
    String exampleBody = mediationCsvHeaders;

    // Defines expected 200 response for existing submission
    return builder
        .given("no data exists for the export")
        .uponReceiving("a request to export a mediation submission")
        .matchPath("/exports/submission-claims-mediation")
        .matchQuery("submission-id", UUID_REGEX)
        .matchQuery("office", ANY_FORMAT_REGEX)
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .body(exampleBody)
        .toPact();
  }

  @Test
  @DisplayName("Verify 200 response - Legal Help")
  @PactTestFor(pactMethod = "getLegalHelpSubmission200")
  void verifyLegalHelp200Response() {
    byte[] csvData =
        exportDataClaimsRestClient
            .getSubmissionExport("legal-help", SUBMISSION_ID, "testOffice")
            .map(HttpEntity::getBody)
            .block();

    assertThat(csvData).isNotNull();
  }

  @Test
  @DisplayName("Verify 200 response - Crime Lower")
  @PactTestFor(pactMethod = "getCrimeLowerSubmission200")
  void verifyCrimeLower200Response() {
    byte[] csvData =
        exportDataClaimsRestClient
            .getSubmissionExport("crime-lower", SUBMISSION_ID, "testOffice")
            .map(HttpEntity::getBody)
            .block();

    assertThat(csvData).isNotNull();
  }

  @Test
  @DisplayName("Verify 200 response - Mediation")
  @PactTestFor(pactMethod = "getMediationSubmission200")
  void verifyMediation200Response() {
    byte[] csvData =
        exportDataClaimsRestClient
            .getSubmissionExport("mediation", SUBMISSION_ID, "testOffice")
            .map(HttpEntity::getBody)
            .block();

    assertThat(csvData).isNotNull();
  }
}
