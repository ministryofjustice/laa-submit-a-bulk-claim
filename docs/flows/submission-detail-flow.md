# Submission Detail Flow

This document describes how the application surfaces submission-level and claim-level detail pages after a bulk upload has been processed. It focuses on the controller orchestration, builder/mapper usage, and supporting clients.

## Flow Overview

```mermaid
flowchart TD
    start["User selects a submission\nfrom search or upload flow"] --> captureId["GET /submission/{submissionReference}\nSubmissionDetailController.getSubmissionReference\nstores SUBMISSION_ID in session"]
    captureId --> redirectDetail["Redirect to /view-submission-detail"]
    redirectDetail --> loadDetail["SubmissionDetailController.getSubmissionDetail"]
    loadDetail --> fetchSubmission["DataClaimsRestClient.getSubmission using submissionId"]
    fetchSubmission --> buildSummary["SubmissionSummaryBuilder.build\ncreates SubmissionSummary view model"]
    buildSummary --> decision{"Navigation tab selected?"}
    decision -->|CLAIM_DETAILS| buildClaimOverview["SubmissionClaimDetailsBuilder.build\npaginates claims for the view"]
    decision -->|MATTER_STARTS| buildMatterStarts["SubmissionMatterStartsDetailsBuilder.build"]
    buildSummary --> invalidCheck{"Submission marked invalid?"}
    invalidCheck -->|Yes| buildMessages["SubmissionClaimMessagesBuilder.buildErrors\npopulates validation messages"]
    invalidCheck -->|No| renderAccepted["Render pages/view-submission-detail-accepted"]
    buildMessages --> renderInvalid["Render pages/view-submission-detail-invalid"]
    buildClaimOverview --> renderAccepted
    buildClaimOverview --> renderInvalid
    buildMatterStarts --> renderAccepted
    renderAccepted --> selectClaim{"User selects a claim row?"}
    renderInvalid --> selectClaim
    selectClaim -->|Yes| cacheClaim["GET /submission/claim/{claimReference}\nClaimDetailController.getClaimDetail\nstores CLAIM_ID in session"]
    cacheClaim --> redirectClaim["Redirect to /view-claim-detail"]
    redirectClaim --> loadClaim["ClaimDetailController.getClaimDetail"]
    loadClaim --> fetchClaim["DataClaimsRestClient.getSubmissionClaim using submissionId and claimId"]
    fetchClaim --> navTab{"Navigation tab selected?"}
    navTab -->|FEE_CALCULATION| feeMapper["SubmissionClaimDetailsMapper.toFeeCalculationDetails"]
    navTab -->|CLAIM_MESSAGES| buildClaimMessages["SubmissionClaimMessagesBuilder.build\nfetches warnings and errors"]
    feeMapper --> renderClaim
    buildClaimMessages --> renderClaim
    renderClaim --> endFlow["User reviews details or navigates back"]
```

## Key Components
- **SubmissionDetailController** – Retrieves submission metadata, builds summaries, and routes between claim and matter start tabs.
- **SubmissionSummaryBuilder / SubmissionClaimDetailsBuilder / SubmissionMatterStartsDetailsBuilder** – MapStruct-backed builders that convert API responses into UI-ready models.
- **SubmissionClaimMessagesBuilder** – Fetches and paginates validation errors or warnings for invalid submissions and individual claims.
- **ClaimDetailController** – Loads claim-specific data, supports navigation tabs, and leverages `SubmissionClaimDetailsMapper` for detail and fee calculations.
- **DataClaimsRestClient** – Provides access to submission summaries, claim details, and validation messages from the Data Stewardship `data-claims-api`.
- **Session Attributes** – `SUBMISSION_ID`, `CLAIM_ID`, and related context are preserved across redirects to maintain state.

## External Dependencies
- **Data Stewardship API** – Supplies submission and claim detail payloads, including validation status and matter start data.
- **WireMock (local)** – Mimics the upstream API responses when running on a developer machine.
