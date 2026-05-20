package uk.gov.justice.laa.bulkclaim.e2e.steps.upload;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.gov.justice.laa.bulkclaim.e2e.state.TestContext;
import uk.gov.justice.laa.bulkclaim.e2e.steps.BaseUiSteps;

public class UploadSteps extends BaseUiSteps {

  // ...existing code...

  @Given("I upload the generated file")
  @Given("I re-upload the generated file")
  public void iUploadTheGeneratedFile() {
    uploadCurrentFile();
    waitForUploadTransition();
  }

  @When("I upload that file")
  public void iUploadFileWithoutWaiting() {
    uploadCurrentFile();
    // For parse errors, the page stays on upload page - no waiting for transition
    // The error will appear in the error summary on the same page
  }

  @Given("I upload the first file")
  public void iUploadTheFirstFile() {
    uploadNamedFile("first.generated.file");
    waitForUploadTransition();
  }

  @Given("I upload the second file")
  public void iUploadTheSecondFile() {
    uploadNamedFile("second.generated.file");
    waitForUploadTransition();
  }

  @Given("I upload {string}")
  public void iUploadNamedFile(String filePath) {
    Path file = extractStepFile("I upload \"" + filePath + "\"");
    TestContext.current().generatedFile(file);
    uploadCurrentFile();
    waitForUploadTransition();
  }

  @Given("I stage {string}")
  @Given("I stage {string} file for upload")
  public void iStageNamedFile(String filePath) {
    Path file = extractStepFile("I stage \"" + filePath + "\"");
    TestContext.current().generatedFile(file);
  }

  @Given("I click upload without attaching a file")
  public void iClickUploadWithoutAttachingAFile() {
    bulkImportPage().submitWithoutFile();
  }

  @When("click import")
  public void clickImport() {
    goToBulkImportPageFromNavigation();
  }

  @Given("I upload the generated file with mime type {string}")
  public void iUploadTheGeneratedFileWithMimeType(String mimeType) {
    uploadCurrentFileWithMime(mimeType);
    waitForUploadTransition();
  }

  @Given("I upload the generated file and wait for import in progress")
  public void iUploadGeneratedFileAndWaitForImportInProgress() {
    uploadCurrentFile();
    waitForInProgressScreenOnly();
    waitForInProgressToClear();
  }

  @Given("I upload the generated file and wait for import in progress screen")
  public void iUploadGeneratedFileAndWaitForImportInProgressScreenOnly() {
    uploadCurrentFile();
    waitForInProgressScreenOnly();
  }

  @Given("I wait on validation in progress screen")
  public void iWaitOnValidationInProgressScreen() {
    waitForInProgressToClear();
  }

  private void waitForUploadTransition() {
    waitForInProgressOrSummary();
    if (page().url().contains("upload-is-being-checked")) {
      storeBulkInProgressContext();
    }
  }

  private void waitForInProgressScreenOnly() {
    waitForInProgressOrSummary();
    if (page().url().contains("upload-is-being-checked")) {
      storeBulkInProgressContext();
    }
  }

   private void waitForInProgressToClear() {
     long deadline = System.currentTimeMillis() + Duration.ofMinutes(10).toMillis();
     int reloadCount = 0;
     int maxReloads = 600; // Max 10 minutes at 1-second intervals
     boolean leftInProgressPage = false;
     
     while (System.currentTimeMillis() < deadline && reloadCount < maxReloads) {
       try {
         String currentUrl = page().url();
         boolean isOnCheckingPage = currentUrl.contains("upload-is-being-checked");
         
         if (!isOnCheckingPage) {
           // We've transitioned away from the checking page
           if (!leftInProgressPage) {
             System.out.println("[WAIT-DEBUG] Transitioned away from upload-is-being-checked page after " + reloadCount + " reloads");
             leftInProgressPage = true;
           }
           
           // Wait for the results page to fully render
           System.out.println("[WAIT-DEBUG] Waiting for results page to fully render...");
           try {
             page().waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
           } catch (Exception ignored) {
             // Network idle might not be available, try timeout instead
             page().waitForTimeout(2000);
           }
           
           // Additional wait to ensure all result content is rendered
           page().waitForTimeout(1000);
           System.out.println("[WAIT-DEBUG] Results page rendered. Final URL: " + page().url());
           return;
         }

         // Still on the checking page - log status and continue
         boolean stillInProgress = page().locator("h1.moj-interruption-card__heading").count() > 0
             && page().locator("h1.moj-interruption-card__heading").first().isVisible();
         
         if (reloadCount % 30 == 0) {
           System.out.println("[WAIT-DEBUG] Still on upload-is-being-checked... reload #" + reloadCount + " (elapsed: " + ((System.currentTimeMillis() - (deadline - Duration.ofMinutes(10).toMillis())) / 1000) + "s, in progress: " + stillInProgress + ")");
         }

         page().waitForTimeout(1000);
         page().reload();
         reloadCount++;
       } catch (Exception e) {
         System.out.println("[WAIT-DEBUG] Error during wait: " + e.getMessage());
         throw e;
       }
     }
     System.out.println("[WAIT-DEBUG] TIMEOUT: Still on upload-is-being-checked after " + reloadCount + " reloads and " + (deadline - System.currentTimeMillis()) + "ms over deadline");
     throw new AssertionError("Timeout waiting for validation in progress screen to clear after " + reloadCount + " reloads (10 min max)");
   }

   @Then("I should see the following submission error messages for {string}:")
   public void iShouldSeeTheFollowingSubmissionErrorMessagesFor(String areaOfLaw, DataTable dataTable) {
     // Ensure we're completely off the in-progress screen before checking for errors
     long deadline = System.currentTimeMillis() + Duration.ofMinutes(10).toMillis();
     int reloadCount = 0;
     
     while (System.currentTimeMillis() < deadline && reloadCount < 600) {
       if (!page().url().contains("upload-is-being-checked")) {
         System.out.println("[DEBUG] Successfully transitioned off upload-is-being-checked page");
         break;
       }
       page().waitForTimeout(1000);
       page().reload();
       reloadCount++;
     }
     
      // Poll for error table to appear (backend may still be rendering validation results)
      long tableDeadline = System.currentTimeMillis() + 30000; // 30 second wait for table
      int pollCount = 0;
      int tableRowsFound = 0;
      
      System.out.println("[DEBUG] Waiting for error table to appear...");
      while (System.currentTimeMillis() < tableDeadline) {
        com.microsoft.playwright.Locator rows = page().locator("table.govuk-table tbody tr");
        tableRowsFound = rows.count();
        
        if (tableRowsFound > 0) {
          System.out.println("[DEBUG] Error table appeared after " + pollCount + " polls with " + tableRowsFound + " rows");
          break;
        }
        
        if (pollCount % 5 == 0) {
          System.out.println("[DEBUG] Poll attempt #" + pollCount + " - error table not yet visible");
        }
        
        page().waitForTimeout(500); // Short poll interval
        pollCount++;
      }
      
      if (tableRowsFound == 0) {
        System.out.println("[DEBUG] ERROR TABLE NOT FOUND after " + pollCount + " polls!");
        System.out.println("[DEBUG] Current URL: " + page().url());
        System.out.println("[DEBUG] Page title: " + page().title());
        System.out.println("[DEBUG] Page HTML (first 1000 chars): " + page().content().substring(0, Math.min(1000, page().content().length())));
      }
      
      // Extract expected error messages from the data table
      List<String> expectedErrors = dataTable.asList(String.class);
      
      // Filter out header rows (like "Error Message")
      expectedErrors = expectedErrors.stream()
          .filter(e -> !e.equalsIgnoreCase("Error Message") && !e.equalsIgnoreCase("error"))
          .toList();
      
      System.out.println("[DEBUG] Current URL: " + page().url());
      System.out.println("[DEBUG] Page title: " + page().title());
      System.out.println("[DEBUG] Looking for errors in Messages column...");
      System.out.println("[DEBUG] Expected errors (after filtering headers): " + expectedErrors);
      
      // Collect all error messages from the Messages column
      Set<String> collectedErrors = new HashSet<>();
      
      boolean hasMorePages = true;
      int pageCount = 0;
      
      while (hasMorePages && pageCount < 20) { // Prevent infinite loops
        pageCount++;
        System.out.println("[DEBUG] Page " + pageCount + " - Current URL: " + page().url());
        
        // Get all rows in the table
        com.microsoft.playwright.Locator rows = page().locator("table.govuk-table tbody tr");
        int rowCount = rows.count();
        System.out.println("[DEBUG] Found " + rowCount + " table rows");

        // Find the Messages column index from the header row
        int messagesColIndex = -1;
        com.microsoft.playwright.Locator headerCells = page().locator("table.govuk-table thead tr th");
        int headerCount = headerCells.count();
        for (int h = 0; h < headerCount; h++) {
          String headerText = headerCells.nth(h).textContent().trim().toLowerCase();
          if (headerText.contains("message")) {
            messagesColIndex = h;
            System.out.println("[DEBUG] Found Messages column at index " + h);
            break;
          }
        }

        for (int i = 0; i < rowCount; i++) {
          try {
            com.microsoft.playwright.Locator cells = rows.nth(i).locator("td");
            int cellCount = cells.count();

            if (messagesColIndex >= 0 && messagesColIndex < cellCount) {
              // Use the exact Messages column
              String cellText = cells.nth(messagesColIndex).textContent();
              if (cellText != null && !cellText.isBlank()) {
                String cleaned = cellText.replaceAll("\\s+", " ").trim();
                if (!cleaned.isBlank() && cleaned.length() > 3) {
                  System.out.println("[DEBUG] Row " + i + " Messages cell: '" + cleaned.substring(0, Math.min(150, cleaned.length())) + "'");
                  collectedErrors.add(cleaned);
                }
              }
            } else {
              // No header found — collect all cells with meaningful text (fallback)
              for (int j = 0; j < cellCount; j++) {
                String cellText = cells.nth(j).textContent();
                if (cellText != null && !cellText.isBlank()) {
                  String cleaned = cellText.replaceAll("\\s+", " ").trim();
                  if (!cleaned.isBlank() && cleaned.length() > 10 && cleaned.contains(" ")) {
                    System.out.println("[DEBUG] Row " + i + ", Cell " + j + ": '" + cleaned.substring(0, Math.min(150, cleaned.length())) + "'");
                    collectedErrors.add(cleaned);
                  }
                }
              }
            }
          } catch (Exception e) {
            System.out.println("[DEBUG] Error reading row " + i + ": " + e.getMessage());
          }
        }
       
       // Check for next page button
       com.microsoft.playwright.Locator nextButton = page().locator("a:has-text('Next'), button:has-text('Next')");
       if (nextButton.count() > 0 && nextButton.first().isEnabled()) {
         try {
           System.out.println("[DEBUG] Next button found, clicking...");
           nextButton.first().click();
           page().waitForLoadState();
           hasMorePages = true;
         } catch (Exception e) {
           System.out.println("[DEBUG] Error clicking next button: " + e.getMessage());
           hasMorePages = false;
         }
       } else {
         hasMorePages = false;
       }
     }
     
     System.out.println("[DEBUG] Scan complete - Expected " + expectedErrors.size() + " error messages");
     System.out.println("[DEBUG] Collected " + collectedErrors.size() + " error messages");
     System.out.println("[DEBUG] Collected errors: " + collectedErrors);
     
     // Check if we actually found any errors  
     if (collectedErrors.isEmpty()) {
       throw new AssertionError("No validation error messages found in Messages column. " +
           "Expected " + expectedErrors.size() + " errors.\n" +
           "Current URL: " + page().url() + "\n" +
           "Page title: " + page().title());
     }
     
     // Verify all expected errors are found using flexible matching
     List<String> missingErrors = new ArrayList<>();
     for (String expectedError : expectedErrors) {
       boolean found = collectedErrors.stream()
           .anyMatch(collected -> 
               collected.toLowerCase().contains(expectedError.toLowerCase()) ||
               expectedError.toLowerCase().contains(collected.toLowerCase()));
       
       if (!found) {
         missingErrors.add(expectedError);
       }
     }
     
     if (!missingErrors.isEmpty()) {
       throw new AssertionError(
           "Missing " + missingErrors.size() + " expected error messages:\n" +
           "Missing: " + missingErrors + "\n" +
           "Collected (" + collectedErrors.size() + "): " + collectedErrors);
     }
     
     System.out.println("[✅] All " + expectedErrors.size() + " expected error messages found in Messages column for " + areaOfLaw);
   }

  @Then("the user sees an error message on the upload page containing {string}")
  public void theUserSeesAnErrorMessageOnTheUploadPageContaining(String expectedError) {
    // Wait for error summary to appear on upload page
    page().waitForSelector("#error-summary", new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(5000));
    
    // Get the error text
    String errorText = page().locator("#error-summary").textContent();
    
    // Verify the error message is displayed
    if (!errorText.contains(expectedError)) {
      throw new AssertionError(
          "Expected error message not found on upload page.\n" +
          "Expected: " + expectedError + "\n" +
          "Found: " + errorText);
    }
  }
}
