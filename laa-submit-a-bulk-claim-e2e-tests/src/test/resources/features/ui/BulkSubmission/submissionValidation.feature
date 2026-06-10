@validationChecks
Feature: Invalid submission level validation

  Background:
    Given I start from a clean logged-in state


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

