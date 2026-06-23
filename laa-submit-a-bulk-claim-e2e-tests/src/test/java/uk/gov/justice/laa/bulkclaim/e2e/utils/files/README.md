# File generator utilities

This package contains the Java file-generation helpers used by the E2E suite to create uploadable submission files for different areas of law.

## What this package is for

These utilities support scenarios that need to:

- generate minimal valid upload files
- generate files from a Cucumber `DataTable`
- create special invalid inputs (empty, invalid type, oversized)
- tweak generated files after creation (for example submission period or specific field overrides)

In the current E2E flow, `BaseUiSteps` is the main consumer of this package.

## Main entry points used by E2E steps

### Generic helper

`FileGeneratorUtil`

Use this for generic operations shared across areas of law:

- `generateMinimalSubmissionFile(areaOfLaw, format, outcomes, target)`
- `generateFromClaimsTable(areaOfLaw, format, rows, target)`
- `generateEmptyFile(target)`
- `generateInvalidFile(target)`
- `generateLargeFile(target, sizeInMb)`
- field-update helpers such as `overrideField(...)`

### Area-specific generators

These are the main generators used when a scenario needs realistic data for one area of law:

- `LegalHelpGenerator`
- `CrimeGenerator`
- `MediationGenerator`

Common methods on these classes include:

- `generateMinimalSubmissionFile(format, outcomes, filePath)`
- `generateFromClaimsTable(format, claimRows, filePath)`

These generators:

- create realistic office / schedule / outcome records
- support CSV and XML output paths
- normalise common override keys from step tables
- derive or resolve submission periods through `SubmissionPeriodHelper`

## Supporting types

- `GenerateFileOptions` - option bag for TS-parity style generators (`suffix`, `office`, `submissionPeriod`, `claims`)
- `GeneratorResult` - return type containing generated file paths and selected office

## Other file families in this package

### Wrapper / compatibility classes

These provide narrow or TS-parity style entry points:

- `GenerateSingleLegalHelpFile`
- `CivilFileGenerator`
- `CrimeFileGenerator`
- `MediationFileGenerator`

### Override-focused generators

These generate files with specific override patterns for targeted scenarios:

- `LegalHelpGeneratorWithOverrides`
- `CrimeGeneratorWithOverrides`
- `MediationGeneratorWithOverrides`
- `GenerateCivilFilesWithOverides`
- `GenerateCivilFilesWithOverridesForPath`
- `GenerateCivilImmigrationBoltOnsOverride`

### Calculation / fixed-data helpers

These appear to support targeted datasets for calculation or deterministic test scenarios:

- `GenerateCrimeFilesForCalculations`
- `GenerateLegalHelpFilesForCalculations`
- `GenerateLegalHelpImmigrationFilesForCalculations`
- `GenerateFixedCrimeFiles`
- `GenerateMatterStartsFile`
- `DuplicateLegalHelpFilesGenerator`

### Post-generation mutation helpers

- `InjectSubmissionPeriod` - updates the `submissionPeriod` value on an existing generated file

## How `BaseUiSteps` uses this package

From the current `BaseUiSteps` implementation:

- `generateMinimalFile(...)` delegates by area of law to `LegalHelpGenerator`, `CrimeGenerator`, `MediationGenerator`, or falls back to `FileGeneratorUtil`
- `generateFromTable(...)` delegates similarly using Cucumber `DataTable` rows
- `generateSpecialFile(...)` uses `FileGeneratorUtil` for empty / invalid / large files
- several step helpers then mutate generated files afterward using utility override methods

## Output location

Generated files are typically written under:

`build/tmp/e2e/generated`

This keeps generated artifacts out of source directories and makes them easy to clean up between runs.

## Notes for maintainers

- This package is utility-oriented; there are currently no standalone `main(...)` entry points in these classes.
- Naming is mixed because several classes preserve TypeScript migration parity with older test tooling.
- If you add a new area-of-law generator, update the delegation in `BaseUiSteps` so step definitions can use it automatically.

