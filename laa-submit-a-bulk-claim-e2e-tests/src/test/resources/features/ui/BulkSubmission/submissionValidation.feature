@validationChecks
Feature: Invalid submission level validation

  Background:
    Given I start from a clean logged-in state
#    Given I am on the bulk import page

#  @submissionValidation @smoke
#  Scenario: Reject submission due to period prior to 2015
#    When I upload "tests/data/invalid/submissionPeriod.txt"
#    And I wait on validation in progress screen
#    Then I should see a submission error message for "<AreaOfLaw>"
#    """
#    Submissions for periods before JAN-2015 are not accepted. Please submit for a period on or after JAN-2015.
#    """

  @submissionValidation
  Scenario Outline: Reject submission due to invalid submission periods
    When I stage "tests/data/invalid/submissionPeriod.txt" file for upload
    And I update the SubmissionPeriod to "<periodType>"
    And I upload the generated file
    Then I should see the following submission error messages for the "<errorPlaceholder>"
      | Error Message                                                                                          |
      | Submissions for <errorText> (<errorPlaceholder>) are not accepted. Please submit for a previous month. |

    Examples:
      | periodType   | errorPlaceholder | errorText               |
      | CurrentMonth | CURRENT_MONTH    | the current month       |
      | FutureDate   | CURRENT_MONTH    | after the current month |

