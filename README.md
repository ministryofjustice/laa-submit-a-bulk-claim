# Submit a Bulk Claim

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/laa-submit-a-bulk-claim/badge)](https://github-community.service.justice.gov.uk/repository-standards/laa-cwa-bulk-upload)

Submit a Bulk Claim is a Spring Boot web application that enables Legal Aid Agency providers to
upload and track claims in bulk.
The service authenticates users via Sign into LAA Services (SILAS), orchestrates validation and
processing through the Data Stewardship `data-claims-api`, and surfaces submission feedback through
a lightweight web UI.

## Table of Contents

- [Overview](#overview)
- [Key Capabilities](#key-capabilities)
- [Integrations](#integrations)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Local Development](#local-development)
    - [Prerequisites](#prerequisites)
    - [Configure External Dependencies](#configure-external-dependencies)
    - [Run the Application](#run-the-application)
    - [Configuration](#configuration)
- [Testing](#testing)
- [Deployment](#deployment)
- [Tooling & Conventions](#tooling--conventions)
- [Documentation](#documentation)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

## Overview

- Securely capture, validate, and submit bulk claims for providers authorised via SILAS.
- Surface submission progress, validation results, and historic uploads via search workflows.
- Integrate with downstream services through client libraries generated from the `data-claims-api`.

## Key Capabilities

- Guided bulk file upload with validation, virus scanning, and provider scoping.
- In-progress polling for submission status as the Data Stewardship platform processes files.
- Search and pagination for previous submissions with detailed status and error summaries.
- Role-aware error handling and feedback aligned to GOV.UK design guidance.

## Integrations

- **SILAS (Sign into LAA Services)** – Production authentication relies on Azure AD SILAS tenants.
  Locally, use the [laa-oidc-mock-server](https://github.com/ministryofjustice/laa-oidc-mock-server)
  to emulate SILAS OIDC flows.
- **Data Stewardship `data-claims-api`** – REST client (`DataClaimsRestClient`) fetches submission
  summaries, claim details, and validation messages. Data transfer objects are supplied via the
  shared data stewardship model packages.
- **WireMock stubs** – Local testing depends on WireMock mappings in `wiremock/mappings` to simulate
  Data Stewardship responses and avoid touching live infrastructure.
- **MapStruct mappers** – DTOs are mapped to UI view models via MapStruct components in
  `laa-submit-a-bulk-claim-ui/src/main/java/uk/gov/justice/laa/bulkclaim/mapper`.

## Architecture

- **Presentation layer** – Spring MVC controllers orchestrate upload (`BulkImportController`),
  polling (`BulkImportInProgressController`), search (`SearchController`), and detail pages.
- **Service and helper layer** – Validators, pagination helpers, and OIDC utilities encapsulate
  business rules and session handling.
- **Clients** – Declarative Spring `@HttpExchange` clients wrap the Data Stewardship API with
  non-blocking WebClient calls.
- **View templates** – Thymeleaf templates render forms, dashboards, and error states.
- **Security** – Spring Security OIDC handles SILAS authentication; client credentials access the
  Data Stewardship API.

## Tech Stack

- Java 25, Spring Boot, Spring Security, Spring WebFlux clients
- Thymeleaf, HTMX, and GOV.UK Design System components
- Gradle build tooling
- MapStruct for DTO/view-model mapping
- JUnit 5, MockMvc, and Mockito for automated testing
- Docker & Docker Compose for local dependencies

## Local Development

### Prerequisites

- Java 25 or higher
- Gradle (or use the included Gradle wrapper)
- Docker Desktop for dependency containers
- GitHub Packages credentials configured for the [
  `laa-ccms-spring-boot-gradle-plugin`](https://github.com/ministryofjustice/laa-ccms-spring-boot-common?tab=readme-ov-file#provide-your-repository-credentials)

### Configure External Dependencies by running locally

1. **Clone the repository**
   ```sh
   git clone git@github.com:ministryofjustice/submit-a-bulk-claim.git
   ```
2. **Clone other dependencies**
   ```sh
   git clone git@github.com:ministryofjustice/laa-data-claims-api.git
   git clone git@github.com:ministryofjustice/laa-data-claims-event-service.git # Claims API Dependency
   git clone git@github.com:ministryofjustice/laa-fee-scheme-api.git # Claims API Dependency
   ```

3. **Build and run the Data Stewardship APIs locally by following the per-project README.md**

- [Claims API README.md](https://github.com/ministryofjustice/laa-data-claims-api/blob/main/README.md)
    - [Claims Event Service README.md](https://github.com/ministryofjustice/laa-data-claims-event-service/blob/main/README.md)
    - [Fee Scheme API README.md](https://github.com/ministryofjustice/laa-fee-scheme-api/blob/main/README.md)

4. **Setup authentication**

If you wish to have a mocked OAuth solution, you can run the local OIDC mock server. This is
acheived by running the following:

  ```shell
  docker-compose up laa-mock-oidc-service
  ```

Alternatively, you can use the SILAS sandbox. Ask another developer for details on how to
create an account on SILAS for testing. This account can also be used in deployed environments.

5. **Set Local variables**

The easiest method to set the local variables is the ask another developer for a copy of
their `application-local.yaml` file, then you can set the Spring Profile to `local`.

Set the following environment variables, and application.yaml will pick them up:

   ```sh
   export CLAIM_API_URL=http://localhost:8091
   export CLAIMS_API_ACCESS_TOKEN=dummy-token
   export REST_CLIENT_CONNECT_TIMEOUT=5000
   export REST_CLIENT_READ_TIMEOUT=40000
   export UPLOAD_MAX_FILE_SIZE=10MB
   export SERVER_MAX_FILE_SIZE=10MB
   ```

The example access token aligns with the WireMock fixtures; supply a real token when targeting
non-mocked environments. Update `AUTH_*` and `SILAS_*` variables to match either SILAS sandbox
credentials or the mock server claims.

### Configure External Dependencies using Wiremock and Mock OIDC

1. **Clone the repository**
   ```sh
   git clone git@github.com:ministryofjustice/submit-a-bulk-claim.git
   cd submit-a-bulk-claim
   ```
2. **Start Data Stewardship WireMocks**
   ```sh
    docker-compose up claim-service
   ```

WireMock listens on `http://localhost:8091` using stubs from `wiremock/mappings/claim-service`.

3. **Run the SILAS OIDC mock**

Included in docker-compose as `laa-mock-oidc-service`. This is exposed on `http://localhost:9000`
and align issuer/client details with your local Spring profile.

More details
here: [laa-oidc-mock-server](https://github.com/ministryofjustice/laa-oidc-mock-server#running-the-server-via-docker).

4. **Set Local variables**

The easiest method to set the local variables is the ask another developer for a copy of
their `application-local.yaml` file, then you can set the Spring Profile to `local`.

Alternatively set the following environment variables, and application.yaml will pick them up:

  ```sh
  export CLAIM_API_URL=http://localhost:8091
  export CLAIMS_API_ACCESS_TOKEN=dummy-token
  export REST_CLIENT_CONNECT_TIMEOUT=5000
  export REST_CLIENT_READ_TIMEOUT=40000
  export UPLOAD_MAX_FILE_SIZE=10MB
  export SERVER_MAX_FILE_SIZE=10MB
  ```

The example access token aligns with the WireMock fixtures; supply a real token when targeting
non-mocked environments.

Update `AUTH_*` and `SILAS_*` variables to match either SILAS sandbox credentials or the mock
server claims.

### Run the Application

```sh
./gradlew clean bootRun
```

- The UI is served on `http://localhost:8082`.
- Management endpoints are exposed on `http://localhost:8083`.
- Use `SPRING_PROFILES_ACTIVE=local` if you maintain separate local overrides.

### Configuration

- Default configuration lives in `laa-submit-a-bulk-claim-ui/src/main/resources/application.yaml`.
- Override secrets via environment variables or Spring profiles. Kubernetes deployments source them
  from `laa-submit-a-bulk-claim-secrets` (see `.helm/submit-a-bulk-claim/values.yaml`).
- Upload limits can be adjusted with `UPLOAD_MAX_FILE_SIZE` and `SERVER_MAX_FILE_SIZE`.

## Testing

### Unit Tests
```sh
./gradlew test
```

- Controller and service layers are covered with MockMvc and unit tests.
- WireMock supports integration-style tests against Data Stewardship flows.
- Add new tests alongside changes to maintain coverage.

### E2E Tests
E2E tests are designed to run in UAT environments. They can be found on GitHub
within the [bulk-submission-and-fee-scheme-tests](https://github.com/ministryofjustice/bulk-submission-and-fee-scheme-tests-)
repository.

## Deployment

- GitHub Actions pipelines under `.github/workflows` build, scan, and publish Docker images.
    - `build-main.yml` tags merged changes on `main` and publishes artifacts.
    - `deploy-main.yml` produces release images, pushes to ECR, and triggers helm deployments.
- Kubernetes manifests are defined in `.helm/submit-a-bulk-claim/` with environment-specific overrides under
  `.helm/submit-a-bulk-claim/values/`.
- Deployments run on the MoJ Cloud Platform with ModSec ingress and pod security settings defined in
  chart values.

## Tooling & Conventions

- Follows MoJ coding standards; repository badge indicates compliance.
- Formatting and linting are inherited from shared Gradle conventions (
  `laa-ccms-spring-boot-gradle-plugin`).
- MapStruct mappers are generated at compile time; rebuild when DTOs change (
  `./gradlew clean assemble`).

## Documentation

- High-level flow diagrams are available in [`docs/flows`](docs/flows):
    - [`bulk-upload-flow.md`](docs/flows/bulk-upload-flow.md)
    - [`search-flow.md`](docs/flows/search-flow.md)
    - [`submission-detail-flow.md`](docs/flows/submission-detail-flow.md)
- Extend this folder with additional operational or support documentation as needed.

## Project Structure

- `laa-submit-a-bulk-claim-ui/src/main/java/uk/gov/justice/laa/bulkclaim/controller` – Web
  controllers for upload, polling, search, and detail views.
- `laa-submit-a-bulk-claim-ui/src/main/java/uk/gov/justice/laa/bulkclaim/service` – Business
  services and helpers.
- `laa-submit-a-bulk-claim-ui/src/main/java/uk/gov/justice/laa/bulkclaim/client` – Web clients for
  Data Stewardship APIs.
- `laa-submit-a-bulk-claim-ui/src/main/java/uk/gov/justice/laa/bulkclaim/mapper` – MapStruct mappers
  translating API responses to view models.
- `laa-submit-a-bulk-claim-ui/src/main/resources/templates` – Thymeleaf views.
- `wiremock/mappings` – Local stubs for dependent APIs.
- `.helm/submit-a-bulk-claim/` – Helm chart used by GitHub Actions deploy workflows.

## Contributing

- Create a feature branch from `main`.
- Write clear commit messages and include tests for new behaviour.
- Ensure `./gradlew test` passes before raising a pull request.
- Follow the MoJ pull request template and tagging policy where applicable.

## License

[MIT](LICENSE)
