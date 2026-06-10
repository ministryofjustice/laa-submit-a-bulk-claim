@matterStarts
Feature: Matter Starts Uploads

  Background:
    Given I start from a clean logged-in state

  Scenario: Successful bulk submission for Legal help matter starts
    When I generate "Legal help" "csv" with all matter type file
    And I upload the generated file
    Then I should see the submission summary for "Legal help" with matter starts matching the generated file

  Scenario: Crime lower submission hides matter starts tab
    When I generate "Crime lower" "csv" file with "1" outcomes
    And I upload the generated file
    Then I should see the submission summary for "Crime lower" without a matter starts tab


