# E2E Data Generators Handover Guide

This document explains how test data files are generated for Java E2E and how to safely change generator logic.

## 1) What this system does

The E2E tests generate upload files at runtime (instead of committing many static fixture files).

Main outcomes:
- Generate valid files for `Legal help`, `Crime lower`, and `Mediation`
- Generate negative files (`empty`, invalid extension, oversized)
- Apply post-generation mutations (`submissionPeriod`, `office`, specific row field updates)
- Support duplicate-check scenarios that need two files with controlled time gaps

Generated files are written to:
- `build/tmp/e2e/generated`

## 2) Main code map (start here)

- Step entry points: `src/test/java/uk/gov/justice/laa/bulkclaim/e2e/steps/upload/UploadGenerationSteps.java`
- Step helper + generator dispatch: `src/test/java/uk/gov/justice/laa/bulkclaim/e2e/steps/BaseUiSteps.java`
- Core generator engine: `src/test/java/uk/gov/justice/laa/bulkclaim/e2e/utils/files/FileGeneratorUtil.java`
- Legal help duplicate pair generation: `src/test/java/uk/gov/justice/laa/bulkclaim/e2e/utils/files/CivilFileGenerator.java`
- Submission period selection + provider/DB validation: `src/test/java/uk/gov/justice/laa/bulkclaim/e2e/utils/data/SubmissionPeriodHelper.java`

## 3) Execution flow (simple)

1. Gherkin step calls a method in `UploadGenerationSteps`
2. It delegates to `BaseUiSteps.generateMinimalFile(...)` or `generateFromTable(...)`
3. `BaseUiSteps` routes by area of law:
   - Legal help -> `LegalHelpGenerator`
   - Mediation -> `MediationGenerator`
   - Crime lower -> `CrimeGenerator`
   - fallback -> `FileGeneratorUtil`
4. `FileGeneratorUtil` builds OFFICE/SCHEDULE/OUTCOME payloads
5. File path is stored in `TestContext` and then uploaded by UI steps

## 4) Which generator to use

Use this guide when adding or changing test steps.

- `generateMinimalSubmissionFile(...)`
  - Use for smoke tests that only need valid structure
  - Gives deterministic defaults and date-safe records

- `generateFromClaimsTable(...)`
  - Use when feature files define specific values in a `DataTable`
  - Supports alias normalization (for example `feeCode` -> `FEE_CODE`)

- `overrideField(...)` / `updateLastRowFields(...)`
  - Use for one-off mutation steps after file creation
  - Helpful when testing boundary values or duplicates

- `generateTwoLegalHelpFilesMonthsApart(...)`
  - Use for duplicate-check logic across two submissions
  - Depends on valid periods/contracts from `SubmissionPeriodHelper`

- `generateTwoLegalHelpFilesOutsideDuplicateCutoff(...)`
  - Use when testing accepted duplicates outside cutoff windows

## 5) DataTable aliases you can use in features

The generator accepts business-friendly keys and maps them to upload payload fields.

Common examples:
- `feeCode` -> `FEE_CODE`
- `ufn` -> `UFN`
- `ucn` -> `UCN`
- `netProfitCosts` -> `PROFIT_COST`
- `netDisbursementAmount` -> `DISBURSEMENTS_AMOUNT`
- `disbursementVatAmount` -> `DISBURSEMENTS_VAT`
- `workConcludedDate` -> `WORK_CONCLUDED_DATE`
- `startDate` or `caseStartDate` -> `CASE_START_DATE`
- `office` or `account` -> `office` (schedule/account-level)
- `submissionPeriod` -> schedule `submissionPeriod`

Ignored assertion-only columns (safe in feature tables):
- `expectedTotal`
- `escapeCase`
- `messages`

## 6) Submission period behavior (important)

Period selection is not random:

- `SubmissionPeriodHelper.getUniqueSubmissionPeriod(...)` tries to avoid collisions
- It may validate against:
  - existing DB submissions
  - provider contract schedules (via Provider API)
- If it cannot find a valid period, tests can fail with messages like:
  - `Could not find two valid submission periods ...`

For duplicate-pair generation, `findTwoValidPeriodsApart(...)` may try fallback offices if the requested office has no valid pair.

## 7) Required environment for robust generation

These are the main variables affecting period/contract-aware generation:

- `PROVIDER_API_KEY`
- `PROVIDER_API` (optional override; default URL is used if unset)
- `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASS`, `DB_NAME`, `DB_SSL` (for DB-backed uniqueness checks)
- `FSP_API_BASE_URL`, `FSP_API_TOKEN` (fee-code lookups used in some validation paths)

If these are missing or invalid, some scenarios still run with fallbacks, but duplicate/period-sensitive cases are more likely to fail.

## 8) Known failure patterns and how to debug

### Error: no valid periods found

Likely causes:
- Provider API key missing/expired
- Requested office has no suitable schedule in that date window
- All candidate periods already used/locked

Quick checks:
- Confirm `PROVIDER_API_KEY` is present in runtime env
- Confirm provider schedules endpoint returns non-empty schedules for the office/effective date

### Error: generated dates rejected by app

Likely causes:
- Manual override created inconsistent date ordering
- Submission period too recent for a `later` placeholder

Quick checks:
- Inspect generated file under `build/tmp/e2e/generated`
- Verify `CASE_START_DATE <= WORK_CONCLUDED_DATE`
- Verify submission period and date boundary rules

## 9) Safe extension checklist (for developers)

When adding a new generator field or area-of-law behavior:

1. Add alias mapping in `FileGeneratorUtil.normalizeFeatureFields(...)`
2. Add canonical mapping in `FileGeneratorUtil.canonicalOutcomeKey(...)`
3. Add defaults in the relevant row builder (`buildLegalHelpRow`, `buildCrimeRow`, `buildMediationRow`)
4. Keep date derivation valid in `deriveOutcomeDates(...)`
5. Add/adjust a feature scenario proving the new field path
6. Run E2E locally with mock auth before pushing

## 10) Practical examples

### Generate a simple legal help file from a step table

```gherkin
Given I generate "Legal help" "csv" file with the following claims
  | ucn             | ufn        | feeCode |
  | 01011998/S/CSVA | 010825/123 | ICISD   |
```

### Override a generated field for boundary testing

```gherkin
And I override the generated file field "submissionPeriod" with value "JAN-2024"
```

### Generate two files to test duplicate windows

```gherkin
Given I generate two Legal help files in "csv" format for office "0P322F" that are "2" months apart with the following claims
  | ucn             | feeCode1 | feeCode2 | ufn        |
  | 05011998/S/CSVA | ICISD    | ICISD    | 020825/523 |
```

## 11) Ownership suggestion

For handover, treat these as the core ownership boundary:
- Step contracts (Gherkin <-> Java): `UploadGenerationSteps`, `BaseUiSteps`
- Payload construction + alias mapping: `FileGeneratorUtil`
- Contract-aware period logic: `SubmissionPeriodHelper`

If tests fail in generation before upload, start debugging in this order:
1. `UploadGenerationSteps`
2. `BaseUiSteps`
3. `FileGeneratorUtil`
4. `SubmissionPeriodHelper`

