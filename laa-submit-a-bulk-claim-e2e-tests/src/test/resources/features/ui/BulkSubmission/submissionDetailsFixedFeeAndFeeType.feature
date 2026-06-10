@feeScheme
Feature: Submission details - Fixed fee & Fee type

  Background:
    And I start from a clean logged-in state

  Scenario: Should show both escaped and fixed claims - Legal Help
    Given I generate "Legal help" "csv" file with the following claims
      | feeCode | profitCost | londonNonLondonRate |
      | FPB020  | 2000       | Y                   |
      | FPB010  | 00         | Y                   |
    When I upload the generated file and wait for import in progress
    Then I should see the submission summary for "Legal help" with "2" claims
    And There should be 1 warnings
    And The claims should have the following information for "Legal help":
      | feeCode | escapeCase | messages |
      | FPB020  | Escaped    | View (1) |
      | FPB010  | No         |          |

  Scenario: Should show both escaped and fixed claims - Crime lower
    Given I generate "Crime" "csv" file with the following claims
      | feeCode | travelCost | travelWaitingCosts |
      | INVC    | 75.38      | 10                 |
      | INVC    | 500        | 500                |
    When I upload the generated file and wait for import in progress
    Then I should see the submission summary for "Crime lower" with "2" claims
    And There should be 1 warnings
    And The claims should have the following information for "Crime lower":
      | feeCode | escapeCase | messages |
      | INVC    | No         |          |
      | INVC    | Escaped    | View (1) |


  Scenario: Should show both escaped and fixed claims - Mediation (Don't get escaped mediation claims)
    Given I generate "Mediation" "csv" file with the following claims
      | feeCode | profitCost |
      | ASSA    | 50         |
      | ASST    | 5000       |
    When I upload the generated file and wait for import in progress
    Then I should see the submission summary for "Mediation" with "2" claims
    And The claims should have the following information for "Mediation":
      | feeCode |
      | ASSA    |
      | ASST    |

