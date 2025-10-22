# Bulk Upload Flow

This document summarises the high-level flow when a provider uploads a bulk CWA claim file. It highlights the primary controller, service, and client interactions inside the UI as well as touch points with external systems.

## Flow Overview

```mermaid
flowchart TD
    start["User navigates to /"] --> showUpload["BulkImportController.showUploadPage\nrenders upload form"]
    showUpload --> submit["POST /upload\nBulkImportController.performUpload"]
    submit --> validate["BulkImportFileValidator & VirusValidator\nvalidate file and provider input"]
    validate -->|Validation fails| redisplay["Redirect back to / with errors"]
    validate -->|Validation passes| upload["DataClaimsRestClient.upload()\ncalls data-claims-api /bulk-submissions"]
    upload --> response["Submission ID + metadata returned"]
    response --> cache["Session stores submissionId, timestamp, filename"]
    cache --> redirect["Redirect to /upload-is-being-checked"]
    redirect --> poll["BulkImportInProgressController.importInProgress\npolls getSubmission(submissionId)"]
    poll --> status{"Submission status\ncomplete or NIL?"}
    status -->|No| refresh["Return upload-in-progress view\nauto-refresh every 5s"]
    refresh --> poll
    status -->|Yes| detail["Redirect to /submission/{id}"]
    detail --> view["SubmissionDetailController.getSubmissionDetail\nbuilds summary, claim details, errors via builders"]

```

## Key Components
- **BulkImportController** – Coordinates upload validation, virus scanning, and the call to `DataClaimsRestClient.upload`.
- **DataClaimsRestClient** – Declarative WebClient interface to the Data Stewardship `data-claims-api`.
- **BulkImportInProgressController** – Polls submission status until validation completes, handling NIL submissions and transient 404s.
- **SubmissionDetailController** – Uses MapStruct-backed builders to compose the UI view models once processing is complete.

## External Dependencies
- **SILAS/OIDC** – The authenticated `OidcUser` supplies the provider identifiers included on upload.
- **Data Stewardship API** – Receives the bulk file, drives validation, and returns submission identifiers and statuses.
- **WireMock** – Provides the above API responses when running locally.
