@claimValidation
Feature: Display message checks

  Background:
    Given I start from a clean logged-in state
    Given I am on the bulk import page

  Scenario: Invalid Fee code
    When I upload "tests/data/invalid/legal_Invalid_Feecode.txt"
    And I wait on validation in progress screen
    Then I should see an error banner saying "2 claims have errors for missing or incorrect information"
    And I should see the following submission error messages for "LEGAL HELP":
      | Error Message                                                                       |
      | A category of law could not be found for the provided fee code: lol                 |
      | The provider is not contracted for the category of law associated with the fee code |


  Scenario: Legal Help: Should check display messages are shown for missing field based errors
    Given I upload "tests/data/invalid/legal_help_missing_fields.csv"
    And I wait on validation in progress screen
    When I should see an error banner saying "1 claim has errors for missing or incorrect information"
    And I should see the following submission error messages for "LEGAL HELP":
      | Error Message                                                 |
      | Net Disbursement Amount is required                           |
      | Disbursements Vat Amount is required                          |
      | Unique File Number is required for Legal Help claims          |
      | Case Start Date is required for Legal Help claims             |
      | Case Concluded Date is required for Legal Help claims         |
      | Outcome Code is required for Legal Help claims                |
      | Travel Waiting Costs Amount is required for Legal Help claims |
      | Client Forename is required for Legal Help claims             |
      | Client Surname is required for Legal Help claims              |
      | Client Date of Birth is required for Legal Help claims        |
      | Unique Client Number is required for Legal Help claims        |
      | Client Postcode is required for Legal Help claims             |
      | Gender Code is required for Legal Help claims                 |
      | Ethnicity Code is required for Legal Help claims              |
      | Disability Code is required for Legal Help claims             |
      | Advice Time is required for Legal Help claims                 |
      | Travel Time is required for Legal Help claims                 |
      | Waiting Time is required for Legal Help claims                |
      | Net Counsel Costs Amount is required for Legal Help claims    |
      | Case Id is required for Legal Help claims                     |
      | Case Reference Number is required for Legal Help claims       |
      | Schedule Reference is required for Legal Help claims          |
      | Net Profit Costs Amount is required for Legal Help claims     |


  Scenario: Legal Help: Should check display messages are shown for out of bound dates with concluded date before 01/04/2013
    Given I generate "Legal help" "csv" file with the following claims
      | caseStartDate | workConcludedDate | transferDate | repOrderDate | clientDob  |
      | 31/12/1994    | 31/03/2013        | 31/12/1994   | 31/03/2016   | 05/01/1899 |
    And I upload the generated file
    When I should see an error banner saying "1 claim has errors for missing or incorrect information"
    And I should see the following submission error messages for "LEGAL HELP":
      | Error Message                                                  |
      | Case Start Date must be between 01/01/1995 and today           |
      | Case Concluded Date cannot be before 01/04/2013                |
      | Transfer Date must be between 01/01/1995 and today             |
      | Representation Order Date must be between 01/04/2016 and today |
      | Client Date of Birth must be between 01/01/1900 and today      |

#    @fir
  Scenario: Legal Help: Should check display messages are shown for out of bound dates with concluded date after the 20th of the month following the submission period
    Given I generate "Legal help" "csv" file with the following claims
      | caseStartDate | workConcludedDate | transferDate | repOrderDate | clientDob  |
      | 31/12/1994    | later             | 31/12/1994   | 31/03/2016   | 05/01/1899 |
    And I upload the generated file
    When I should see an error banner saying "1 claim has errors for missing or incorrect information"
    And I should see the following submission error messages for "LEGAL HELP":
      | Error Message                                                                                  |
      | Case Start Date must be between 01/01/1995 and today                                           |
      | Case Concluded Date cannot be later than the 20th of the month following the submission period |
      | Transfer Date must be between 01/01/1995 and today                                             |
      | Representation Order Date must be between 01/04/2016 and today                                 |
      | Client Date of Birth must be between 01/01/1900 and today                                      |


  Scenario: Legal Help: Should check display messages are shown for out of bound dates with concluded date in the future
    Given I generate "Legal help" "csv" file with the following claims
      | caseStartDate | workConcludedDate | transferDate | repOrderDate | clientDob  |
      | 31/12/1994    | 31/03/2099        | 31/12/1994   | 31/03/2016   | 05/01/1899 |
    And I upload the generated file
    When I should see an error banner saying "1 claim has errors for missing or incorrect information"
    And I should see the following submission error messages for "LEGAL HELP":
      | Error Message                                                  |
      | Case Start Date must be between 01/01/1995 and today           |
      | Case Concluded Date cannot be a future date                    |
      | Transfer Date must be between 01/01/1995 and today             |
      | Representation Order Date must be between 01/04/2016 and today |
      | Client Date of Birth must be between 01/01/1900 and today      |


  Scenario Outline: Legal Help: Should check parse errors for <fieldName>
    Given I generate "Legal help" "csv" file with the following claims
      | <fieldName> |
      | <value>     |
    When I upload that file
    Then the user sees an error message on the upload page containing "<errorMessage>"

    Examples:
      | fieldName     | value | errorMessage                            |
      | vatApplicable | A     | VAT Applicable must only include Y or N |
      | postalApplication       | A     | Postal Application Accepted must only include Y or N               |
      | nrmAdvice               | A     | NRM Advice must only include Y or N                                |
      | legacyCase              | A     | Legacy Case must only include Y or N                               |
      | londonNonLondonRate     | A     | London Rate must only include Y or N                               |
      | additionalTravelPayment | A     | Additional Travel Payment must only include Y or N                 |
      | eligibleClientIndicator | A     | Eligible Client must only include Y or N                           |
      | ircSurgery              | A     | IRC Surgery must only include Y or N                               |
      | substantiveHearing      | A     | Substantive Hearing must only include Y or N                       |
      | toleranceIndicator      | A     | Tolerance Applicable must only include Y or N                      |
      | caseStartDate           | abc   | Case Start Date must be a valid date in the format DD/MM/YYYY      |
      | workConcludedDate       | abc   | Work Concluded Date must be a valid date in the format DD/MM/YYYY  |
      | clientDateOfBirth       | abc   | Client Date of Birth must be a valid date in the format DD/MM/YYYY |
      | transferDate            | abc   | Transfer Date must be a valid date in the format DD/MM/YYYY        |
      | surgeryDate             | abc   | Surgery Date must be a valid date in the format DD/MM/YYYY         |


  Scenario: Disbursement: Start date checks valid
    Given I generate "Legal help" "csv" file with the following claims
      | feeCode | office |
      | ICISD   | 0P322F |
    And I update case start date to be on 20 and 2 month before submission period
    When I upload the generated file
    Then I should see the submission summary for "Legal help"


  Scenario: Disbursement: Start date checks invalid
    Given I generate "Legal help" "csv" file with the following claims
      | feeCode | office |
      | ICISD   | 0P322F |
    And I update case start date to be on 21 and 2 month before submission period
    When I upload the generated file
    Then I should see the following submission error messages for "Legal help":
      | Error Message                                                                                  |
      | Disbursement claims can only be submitted at least 3 calendar months after the Case Start Date |
