# LAA CWA Bulk Upload
[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/laa-cwa-bulk-upload/badge)](https://github-community.service.justice.gov.uk/repository-standards/laa-cwa-bulk-upload)

A Spring Boot web application for securely uploading files in bulk to the Legal Aid Agency's CWA system. The application provides a user-friendly interface for file uploads, provider selection, virus scanning, and result tracking.

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Build](#build)
  - [Wiremock](#wiremock)
  - [Local Application Properties](#local-application-properties)
  - [Run](#run)
  - [Configuration](#configuration)
- [Usage](#usage)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- **Secure File Upload:** Upload files with size and format validation.
- **Virus Scanning:** All files are scanned for viruses before processing.
- **Provider Selection:** Users must select a provider before uploading.
- **Error Handling:** User-friendly error messages for all validation and system errors.
- **Upload Summary:** View results and errors for each upload.
- **Search:** Search for previous uploads by file reference.
- **Role-based Access:** Handles forbidden access and error scenarios gracefully.

## Architecture

- **Controller Layer:** Handles HTTP requests and responses.
- **Service Layer:** Business logic for file upload, virus scanning, and search.
- **Helper Classes:** Provider population and utility logic.
- **Thymeleaf Views:** User interface templates for upload, results, and errors.

## Technologies

- Java 21+
- Spring Boot
- Gradle
- Thymeleaf
- JUnit 5, MockMvc (testing)
- Mockito (mocking dependencies)
- Docker

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle (or use the Gradle wrapper)
- (Optional) Docker for running dependencies
- Install Docker desktop (https://www.docker.com/products/docker-desktop)

Ensure you have the following environment variables set for local development:

### Build

Clone the repository and build the project:

```sh
git clone git@github.com:ministryofjustice/laa-cwa-bulk-upload.git
cd laa-cwa-bulk-upload
./gradlew clean build
```
### Wiremock
```
export WIREMOCK_PORT=8090
export WIREMOCK_HOST=localhost
```
All Wiremock stubs are located in `src/wiremock/mappings/cwa-service`
Before running the application, ensure Wiremock is running on port 8090.
You can start it using Docker compose  : `docker-compose up` (from the root of the project)
Ensure cwa-api.url in your local application yaml is set to 'http://localhost:8090' to point to wiremock.

### Local Application Properties
```
export CWA_API_URL=http://localhost:8090
export CWA_API_TIMEOUT=20
```

### Run

Start the application locally:

```sh
./gradlew bootRun
```

The app will be available at [http://localhost:8082](http://localhost:8082).

### Configuration

You can configure the maximum upload file size and other properties in `src/main/resources/application.properties`:

```
upload-max-file-size=10MB
```

Other configuration options (e.g., CWA API endpoints, authentication) can be added as needed.

## Usage

1. **Open the application** in your browser at [http://localhost:8082](http://localhost:8082).
2. **Select a provider** from the dropdown.
3. **Choose a file** to upload (must be under the configured size limit and virus-free).
4. **Submit the form** to upload.
5. **View the results** or any error messages.
6. **Search** for previous uploads using the file reference and provider.

## Testing

Run all unit and integration tests:

```sh
./gradlew test
```

Test coverage includes:

- Controller logic (file upload, error handling, search)
- Service layer (mocked in controller tests)
- Validation and error scenarios

## Project Structure

- `src/main/java/uk/gov/justice/laa/cwa/bulkupload/controller` — Web controllers
- `src/main/java/uk/gov/justice/laa/cwa/bulkupload/service` — Business logic and services
- `src/main/java/uk/gov/justice/laa/cwa/bulkupload/helper` — Helper and utility classes
- `src/main/resources/templates` — Thymeleaf HTML templates
- `src/test/java/uk/gov/justice/laa/cwa/bulkupload/controller` — Controller tests

## Contributing

Pull requests are welcome! Please:

- Fork the repository and create a feature branch.
- Write clear, concise commit messages.
- Add or update tests for new features or bug fixes.
- Ensure all tests pass before submitting a PR.

## License

[MIT](LICENSE)
```
This README provides a comprehensive overview for developers and users. Adjust project-specific details as needed.