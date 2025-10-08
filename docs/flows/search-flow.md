# Search Flow

This document outlines how the search experience retrieves historic submissions, including validation, pagination, and downstream calls to the Data Stewardship platform.

## Flow Overview

```mermaid
flowchart TD
    start["User navigates to /submissions/search"] --> form["SearchController.search\npopulates SubmissionsSearchForm"]
    form --> submit["POST /submissions/search\nSearchController.handleSearch"]
    submit --> validate["SubmissionSearchValidator\nchecks submissionId and date range"]
    validate -->|Validation fails| redisplay["Redirect to /submissions/search\nwith BindingResult errors"]
    validate -->|Validation passes| redirect["Redirect to /submissions/search/results\nwith filters and pagination"]
    redirect --> results["SearchController.submissionsSearchResults\nparses filters and user offices"]
    results --> dsCall["DataClaimsRestClient.search using offices and filters and page and size"]
    dsCall --> returnedResults["SubmissionsResultSet returned"]
    returnedResults --> paginate["PaginationUtil builds Page metadata"]
    paginate --> view["Render submissions-search-results view\nwith submissions and pagination"]
    view --> select{"User selects a submission?"}
    select -->|Yes| detail["Redirect to /submission/{id}\nhandled by SubmissionDetailController"]
    select -->|No| endFlow["User adjusts filters or pagination"]


```

## Key Components
- **SearchController** – Owns form lifecycle, validation, and the results page orchestration.
- **SubmissionSearchValidator** – Ensures supplied submission ID format and submitted date range are valid.
- **OidcAttributeUtils** – Extracts SILAS office identifiers from the logged-in `OidcUser` to scope searches.
- **DataClaimsRestClient** – Invokes the `data-claims-api` search endpoint, returning a `SubmissionsResultSet`.
- **PaginationUtil** – Converts API paging metadata into a UI-friendly `Page` object.

## External Dependencies
- **SILAS/OIDC** – Provides the office list that limits the search to the user’s permissible providers.
- **Data Stewardship API** – Supplies submission summaries, statuses, and pagination information.
- **WireMock** – Supplies deterministic search responses during local development.
