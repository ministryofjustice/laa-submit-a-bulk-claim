package uk.gov.justice.laa.bulkclaim.e2e.steps.upload;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.When;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import uk.gov.justice.laa.bulkclaim.e2e.state.TestContext;
import uk.gov.justice.laa.bulkclaim.e2e.steps.BaseUiSteps;

/** API upload step parity for TS upload API steps. */
public class UploadApiSteps extends BaseUiSteps {

  private static final HttpClient HTTP = HttpClient.newHttpClient();

  @When("I upload with generated file via the API")
  public void iUploadWithGeneratedFileViaTheApi() {
    Path file = TestContext.current().generatedFile();
    if (file == null || !Files.exists(file)) {
      throw new PendingException("No generated file available for API upload");
    }
    String office = TestContext.current().get("generated.office");
    if (office == null || office.isBlank()) {
      office = "0P322F";
    }
    String submissionId = uploadFileToApi(file, office);
    TestContext.current().put("mostRecentSubmissionId", submissionId);
    waitForTerminalSubmissionStatus(submissionId, office);
  }

  @When("I upload {string} {string} file with {string} outcomes via the API")
  public void iUploadAreaFileWithOutcomesViaTheApi(String areaOfLaw, String format, String outcomes) {
    int parsedOutcomes = Integer.parseInt(outcomes);
    Path generated = generateMinimalFile(areaOfLaw, format, parsedOutcomes);
    TestContext.current().generatedFile(generated);

    String office = TestContext.current().get("generated.office");
    if (office == null || office.isBlank()) {
      office = "0P322F";
    }

    String submissionId = uploadFileToApi(generated, office);
    TestContext.current().put("mostRecentSubmissionId", submissionId);
    waitForTerminalSubmissionStatus(submissionId, office);
  }

  private String uploadFileToApi(Path file, String office) {
    String baseUrl = requiredEnv("DSTEW_API_BASE_URL");
    String token = requiredEnv("DSTEW_API_TOKEN");

    String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "");
    String fileName = file.getFileName().toString();
    String contentType = mimeTypeForFile(fileName);

    try {
      byte[] fileBytes = Files.readAllBytes(file);
      byte[] body = buildMultipartBody(boundary, fileName, contentType, fileBytes);

      String uploadUrl = baseUrl
          + "/api/v1/bulk-submissions?userId=Test.User-submit-a-bulk-claim-auto-test%40devl.justice.gov.uk&offices="
          + office;

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(uploadUrl))
          .header("accept", "application/json")
          .header("Authorization", token)
          .header("Content-Type", "multipart/form-data; boundary=" + boundary)
          .POST(HttpRequest.BodyPublishers.ofByteArray(body))
          .build();

      HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new AssertionError("Bulk upload API failed. HTTP " + response.statusCode() + " body=" + response.body());
      }

      String responseBody = response.body();
      com.fasterxml.jackson.databind.JsonNode json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody);
      com.fasterxml.jackson.databind.JsonNode submissionIds = json.path("submission_ids");
      if (!submissionIds.isArray() || submissionIds.isEmpty()) {
        throw new AssertionError("submission_ids not found in upload response: " + responseBody);
      }
      return submissionIds.get(0).asText();
    } catch (AssertionError ae) {
      throw ae;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to upload generated file via API", e);
    }
  }

  private void waitForTerminalSubmissionStatus(String submissionId, String office) {
    String baseUrl = requiredEnv("DSTEW_API_BASE_URL");
    String token = requiredEnv("DSTEW_API_TOKEN");

    String url = baseUrl + "/api/v1/submissions?offices=" + office + "&submission_id=" + submissionId + "&page=0&size=20";

    for (int i = 0; i < 240; i++) {
      try {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("accept", "application/json")
            .header("Authorization", token)
            .GET()
            .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
          throw new IllegalStateException("Submission poll failed. HTTP " + response.statusCode());
        }

        com.fasterxml.jackson.databind.JsonNode content =
            new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.body()).path("content");
        String status = content.isArray() && !content.isEmpty() ? content.get(0).path("status").asText("UNKNOWN") : "UNKNOWN";

        if ("VALIDATION_SUCCEEDED".equals(status)) {
          return;
        }
        if ("VALIDATION_FAILED".equals(status)) {
          throw new AssertionError("Submission reached VALIDATION_FAILED for id " + submissionId);
        }

        page().waitForTimeout(3000);
      } catch (AssertionError ae) {
        throw ae;
      } catch (Exception e) {
        throw new IllegalStateException("Error polling submission status for " + submissionId, e);
      }
    }

    throw new AssertionError("Submission did not reach terminal state after polling: " + submissionId);
  }

  private byte[] buildMultipartBody(String boundary, String fileName, String contentType, byte[] fileBytes) {
    String preamble = "--" + boundary + "\r\n"
        + "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
        + "Content-Type: " + contentType + "\r\n\r\n";
    String closing = "\r\n--" + boundary + "--\r\n";

    byte[] preambleBytes = preamble.getBytes(StandardCharsets.UTF_8);
    byte[] closingBytes = closing.getBytes(StandardCharsets.UTF_8);

    byte[] body = new byte[preambleBytes.length + fileBytes.length + closingBytes.length];
    System.arraycopy(preambleBytes, 0, body, 0, preambleBytes.length);
    System.arraycopy(fileBytes, 0, body, preambleBytes.length, fileBytes.length);
    System.arraycopy(closingBytes, 0, body, preambleBytes.length + fileBytes.length, closingBytes.length);
    return body;
  }

  private String mimeTypeForFile(String fileName) {
    String lower = fileName.toLowerCase();
    if (lower.endsWith(".xml")) return "application/xml";
    if (lower.endsWith(".txt")) return "text/plain";
    return "text/csv";
  }

  private String requiredEnv(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Missing required env: " + name);
    }
    return value;
  }
}

