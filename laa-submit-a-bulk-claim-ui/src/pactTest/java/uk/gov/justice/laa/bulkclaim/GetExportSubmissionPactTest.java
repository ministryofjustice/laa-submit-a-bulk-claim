package uk.gov.justice.laa.bulkclaim;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.dsl.RegexpMatcher;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
 * <p>For the various {@link Pact} annotations, a scenario is created. There are multiple parts of
 * a
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
@DisplayName("GET: /exports/submission_claims_{area-of-law}.csv PACT tests")
public final class GetExportSubmissionPactTest extends AbstractPactTest {

  @Autowired
  ExportDataClaimsRestClient exportDataClaimsRestClient;

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
          + " Detail - Escape Case Flag";

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getLegalHelpSubmission200(PactDslWithProvider builder) {

    String escapedHeaders = Pattern.quote(legalHelpCsvHeaders);
    // Must start with the exact header row, then newline, then at least one data char
    String csvRegex = "(?s)^" + escapedHeaders + "\\r?\\n.+";
    String exampleBody =
        legalHelpCsvHeaders + "\n" +
            "claim_id,status\n"; // any non-empty second line works for the matcher


    // Defines expected 200 response for existing submission
    return builder
        .given("multiple claims exist for the same submission")
        .uponReceiving("a request to export a submission")
        .matchPath("/exports/submission_claims_(legal-help|crime-lower|mediation).csv")
        .matchQuery("submission-id", UUID_REGEX)
        .matchQuery("office", ANY_FORMAT_REGEX)
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "text/csv"))
        .body(PactDslRootValue.stringMatcher(csvRegex, exampleBody))
        .toPact();
  }

  @Test
  @DisplayName("Verify 200 response")
  @PactTestFor(pactMethod = "getLegalHelpSubmission200")
  void verify200Response() {
    byte[] csvData =
        exportDataClaimsRestClient
            .getSubmissionExport("crime-lower", SUBMISSION_ID, "testOffice")
            .map(response -> response.getBody())
            .block();

    assertThat(csvData).isNotNull();
  }
}
