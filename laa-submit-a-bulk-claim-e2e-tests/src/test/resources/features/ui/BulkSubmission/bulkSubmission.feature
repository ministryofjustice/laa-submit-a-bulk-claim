@upload @stable
Feature: Bulk Submission via UI

  Background:
    Given I start from a clean logged-in state


  Scenario Outline: Successful bulk submission for <AreaOfLaw>
    Given I generate "<AreaOfLaw>" "<Format>" file with "<Outcomes>" outcomes
    And I upload the generated file
    Then I should see the submission summary for "<AreaOfLaw>" with "<Claims>" claims

    @validSubmissions
    Examples:
      | AreaOfLaw   | Format | Outcomes | Claims |
      | Legal help  | csv    | 2        | 2      |
      | Mediation   | csv    | 2        | 2      |
      | Crime lower | csv    | 3        | 3      |


  @submissionValidation
  Scenario Outline: Submission Period Validation : Submission already exists for Office" for <AreaOfLaw>
    When I generate "<AreaOfLaw>" "<Format>" file with "<Outcomes>" outcomes
    And I upload the generated file
    And click import

    When I re-upload the generated file
    Then I should have duplicate submission error for "Office" "<AreaOfLaw>"
      | submission period |

    Examples:
      | AreaOfLaw   | Format | Outcomes |
      | Crime lower | txt    | 0        |
      | Legal help  | xml    | 0        |
      | Mediation   | csv    | 0        |


  @duplicateChecks @claimValidation
  Scenario Outline: Duplicate Claim within the same submission <AreaOfLaw>
    When I generate "<AreaOfLaw>" "<Format>" file with "<Outcomes>" outcomes
    And I duplicate the last record in the generated file
    And I upload the generated file
    Then I should see an error banner saying "2 claims have errors for missing or incorrect information"
    And I should see the following submission error messages for "<AreaOfLaw>":
      | Error Message                                          |
      | A duplicate claim was found within the same submission |

    Examples:
      | AreaOfLaw   | Format | Outcomes |
      | Legal help  | csv    | 2        |
      | Crime lower | csv    | 3        |
