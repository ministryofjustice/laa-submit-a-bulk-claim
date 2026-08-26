# AGENTS.md

## Guidance

- Sister app: `../laa-amend-a-claim` - [repo](https://github.com/ministryofjustice/laa-amend-a-claim)
- Use it to align shared claim terminology and journey wording.

## Architecture

- App code: `laa-submit-a-bulk-claim-ui`
- Related services:
  - All available at `../`
  - `laa-data-claims-api` - claims data store - [repo](https://github.com/ministryofjustice/laa-data-claims-api)
  - `laa-amend-a-claim` - Caseworker UI - [repo](https://github.com/ministryofjustice/laa-amend-a-claim)
  - `laa-data-claims-event-service` - [repo](https://github.com/ministryofjustice/laa-data-claims-event-service)
  - `laa-data-claims-notify-service` - [repo](https://github.com/ministryofjustice/laa-data-claims-notify-service)
  - `laa-oidc-mock-server` - [repo](https://github.com/ministryofjustice/laa-oidc-mock-server)
- Supporting infra: `postgres`, `localstack`, `redis`
- Auth: SILAS (Azure Entra)


## Code standards

- Follow existing Spring Boot, MVC, Thymeleaf, and MapStruct patterns.
- Java uses **Google Java Format** via Spotless.
- Run:

```sh
./gradlew spotlessApply checkstyleAll
```

## Testing

Only run the required tests.

```sh
./gradlew :laa-submit-a-bulk-claim-ui:test
./gradlew :laa-submit-a-bulk-claim-ui:pactTest
./gradlew :laa-submit-a-bulk-claim-ui:accessibilityTest
```
