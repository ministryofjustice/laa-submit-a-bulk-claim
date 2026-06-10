@void
Feature: Void a claim

  Background:
    Given I start from a clean logged-in state

  Scenario: Successfully submitted claim can be voided and shown in the UI
    Given I generate "Crime lower" "csv" file with "1" outcomes
    When I upload the generated file
    Then I should see the submission summary for "Crime lower" with "1" claims
    When I void claim 1 via the void endpoint
    Then I should see a VOIDED tag under the View link for claim 1
    When I open the first claim in the submission
    Then I should see a voided claim banner on the fee calculation screen

