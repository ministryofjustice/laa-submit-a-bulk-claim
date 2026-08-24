# Sample data

The files stored [here](https://github.com/ministryofjustice/laa-submit-a-bulk-claim/docs/sample-data) 
represent a range of claims that can be uploaded via Submit a Bulk Claim (SaBC). All files are for the 
0P322F office code. There are two files for each area of law, one for May 2026 and one for June 2026.
For crime and legal help there are a mix of claims that will either escape or not. For mediation there
are only claims that will not escape. Each submission contains 10 claims with a range of synthetic data.

## Searching for the data

In SaBC submissions can be found by specifying office code and submission period. E.g. in staging the
following links can be used:

- [May 2026 submissions](https://stg-submit-a-bulk-claim-laa-submit-a-bulk-claim-staging.apps.live.cloud-platform.service.justice.gov.uk/submissions/search/results?page=0&submissionPeriod=MAY-2026&areaOfLaw&offices=0P322F&submissionStatuses=SUCCEEDED&sort=createdOn,desc)
- [June 2026 submissions](https://stg-submit-a-bulk-claim-laa-submit-a-bulk-claim-staging.apps.live.cloud-platform.service.justice.gov.uk/submissions/search/results?page=0&submissionPeriod=JUN-2026&areaOfLaw&offices=0P322F&submissionStatuses=SUCCEEDED&sort=createdOn,desc)

In Amend a Claim claims can also be found by specifying office code and submission period. E.g. in staging the
following links can be used and then search parameters further refined if required:

- [May 2026 claims](https://laa-amend-a-claim-staging.apps.live.cloud-platform.service.justice.gov.uk/?officeCode=0P322F&submissionDateMonth=5&submissionDateYear=2026&page=1&sort=unique_file_number,asc)
- [June 2026 claims](https://laa-amend-a-claim-staging.apps.live.cloud-platform.service.justice.gov.uk/?officeCode=0P322F&submissionDateMonth=6&submissionDateYear=2026&page=1&sort=unique_file_number,asc)

## Uploading the files

Files can be uploaded into a test environment by any user with access to office code 0P322F in SaBC.
This can only be done once per file without refreshing the data (see below).

## Refreshing the data

Claims and submissions will need to be manually cleaned up in test environment databases so that
files can be reuploaded.