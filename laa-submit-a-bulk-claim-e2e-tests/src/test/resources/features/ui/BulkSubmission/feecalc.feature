@upload
Feature: Bulk Submission via UI

  Background:
    Given I start from a clean logged-in state

  Scenario: Legal Help – CAPA calculation with disbursement VAT
    Given I generate "Legal help" "csv" file with the following civil claims
      | feeCode | vatIndicator | netDisbursementAmount | disbursementVatAmount |
      | CAPA    | N            |                    20 |                  10.5 |
    When I upload the generated file
    Then the submission summary total should be "£269.50"
    And the claim calculated value should be "£269.50"
    When I view the first claim
    Then the fee calculation should show the following values
      | Item               | Entered | Calculated |
      | Net Disbursements  |   20.00 |      20.00 |
      | Disbursement VAT   |   10.50 |      10.50 |
      | Total              |         |     269.50 |

  Scenario: Mediation – ASSA calculation with disbursement VAT
    Given I generate "Mediation" "csv" file with the following civil claims
      | feeCode | numberOfMediationSessions | vatIndicator | netDisbursementAmount | disbursementVatAmount |
      | ASSA    | 10                        | Y            | 20                    | 10.5                  |
    When I upload the generated file
    Then the submission summary total should be "£134.90"
    When I view the first claim
    Then the fee calculation should show the following values
      | Item              | Entered | Calculated |
      | Net Disbursements |   20.00 |      20.00 |
      | Disbursement VAT  |   10.50 |      10.50 |
      | Total             |         |     134.90 |

  Scenario: Crime – APPA calculation with disbursements and travel
    Given I generate "Crime lower" "csv" file with the following crime claims
      | feeCode | netProfitCosts | netTravelCosts | netWaitingCosts | vatIndicator | netDisbursementAmount | disbursementVatAmount |
      | APPA    |             40 |             10 |              30 | Y            |                    20 |                  15.5 |
    When I upload the generated file
    Then the submission summary total should be "£131.50"
    When I view the first claim
    Then the fee calculation should show the following values
      | Item              | Entered | Calculated |
      | Net Profit Cost   |   40.00 |      40.00 |
      | Net Disbursements |   20.00 |      20.00 |
      | Disbursement VAT  |   15.50 |      15.50 |
      | Travel Costs      |   10.00 |      10.00 |
      | Waiting Costs     |   30.00 |      30.00 |
      | Total             |         |     131.50 |

  Scenario Outline: Immigration and Asylum – Fixed fee with bolt-ons
    Given I generate "Legal help" "csv" file for office "<office>" with the following immigration claims
      | feeCode   | startDate   | immigrationPriorAuthorityNumber   | detentionTravelAndWaitingCosts   | jrFormFilling   | boltOnHomeOfficeInterview   | boltOnAdjournedHearing   | boltOnCmrhOral   | boltOnCmrhTelephone   | boltOnSubstantiveHearing   | vatIndicator   | netDisbursementAmount   | disbursementVatAmount   | expectedTotal   | ucn   |
      | <feeCode> | <startDate> | <immigrationPriorAuthorityNumber> | <detentionTravelAndWaitingCosts> | <jrFormFilling> | <boltOnHomeOfficeInterview> | <boltOnAdjournedHearing> | <boltOnCmrhOral> | <boltOnCmrhTelephone> | <boltOnSubstantiveHearing> | <vatIndicator> | <netDisbursementAmount> | <disbursementVatAmount> | <expectedTotal> | <ucn> |
      When I upload the generated file
    Then the submission summary total should be "£<total>"
    When I view the first claim
    Then the fee calculation should show the following values
      | Item  | Entered | Calculated      |
      | Total |         | <expectedTotal> |

    Examples: Immigration and Asylum Fixed Fee
      | office | feeCode | startDate  | immigrationPriorAuthorityNumber | detentionTravelAndWaitingCosts | jrFormFilling | boltOnHomeOfficeInterview | boltOnAdjournedHearing | boltOnCmrhOral | boltOnCmrhTelephone | boltOnSubstantiveHearing | vatIndicator | netDisbursementAmount | disbursementVatAmount | expectedTotal | total    | ucn             |
      | 0P322F | IACA    | 2013-04-01 |                                 | 50                             | 100           |                           |                        | 4              | 5                   |                          | Y            | 20                    | 10.5                  | 1766.73       | 1,766.73 | 14091962/T/EKKR |
      | 2N199K | IALB    | 2013-04-01 |                                 | 50                             | 100           | 1                         |                        |                |                     |                          | No           | 20                    | 15.5                  | 820.36        | 820.36   | 14091962/T/EKKK |

