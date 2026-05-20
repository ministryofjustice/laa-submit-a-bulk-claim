@duplicateChecks
Feature: Duplicate checks - Legal Help - Disbursements

  Background:
    Given I start from a clean logged-in state
    Given I am on the bulk import page

  Scenario Outline: Should accept submission if more than <monthsDifference> months apart
    Given I generate two Legal help files in "<format>" format for office "<office>" that are "<monthsDifference>" months apart with the following claims
      | ucn   | feeCode1 | feeCode2 | ufn   |
      | <ucn> | ICISD    | ICISD    | <ufn> |

    When I upload the first file
    And click import
    When I upload the second file
    Then I should see the submission summary for "Legal help"

    Examples:
      | format | office | ufn        | ucn             | monthsDifference |
      | csv    | 0P322F | 020725/123 | 03021998/S/CSVA | 3                |
      | csv    | 2L849T | 020725/124 | 04021998/S/CSVA | 4                |

  @wede
  Scenario Outline: Within file duplicates
    Given I generate "Legal help" "<format>" file with the following claims
      | ucn   | feeCode | ufn   |
      | <ucn> | ICISD   | <ufn> |
      | <ucn> | ICISD   | <ufn> |
    When I upload the generated file
    Then I should see the following submission error messages for "Legal help":
      | Error Message                                          |
      | A duplicate claim was found within the same submission |
      | A duplicate claim was found within the same submission |
    Examples:
      | format | ufn        | ucn             |
      | csv    | 010825/123 | 01021998/S/CSVA |

  Scenario Outline: Not duplicate if different ufn
    Given I generate single "Legal help" "<format>" file with the following claims
      | ucn   | feeCode | ufn    | office |
      | <ucn> | ICISD   | <ufn>1 | 1T102C |

    And I upload the generated file
    And click import

    Given I generate single "Legal help" "<format>" file with the following claims
      | ucn   | feeCode | ufn    | office |
      | <ucn> | ICISD   | <ufn>2 | 1T102C |

    When I upload the generated file
    Then I should see the submission summary for "Legal help"

    Examples:
      | format | ufn       | ucn             |
      | csv    | 011025/12 | 01021998/S/CSVA |

  Scenario Outline: Not duplicate if different ucn
    Given I generate single "Legal help" "<format>" file with the following claims
      | ucn    | feeCode | ufn   | office |
      | <ucn>A | ICISD   | <ufn> | 2P747T |

    And I upload the generated file
    And click import

    Given I generate single "Legal help" "<format>" file with the following claims
      | ucn    | feeCode | ufn   | office |
      | <ucn>B | ICISD   | <ufn> | 2P747T |

    When I upload the generated file
    Then I should see the submission summary for "Legal help"

    Examples:
      | format | ufn        | ucn            |
      | csv    | 011025/123 | 01021998/S/CSV |


  Scenario Outline: Not duplicate if different office
    Given I generate single "Legal help" "<format>" file with the following claims
      | ucn   | feeCode | ufn   | office    |
      | <ucn> | ICISD   | <ufn> | <office1> |

    And I upload the generated file
    And click import

    Given I generate single "Legal help" "<format>" file with the following claims
      | ucn   | feeCode | ufn   | office    |
      | <ucn> | ICISD   | <ufn> | <office2> |

    When I upload the generated file
    Then I should see the submission summary for "Legal help"

    Examples:
      | format | office1 | office2 | ufn        | ucn             |
      | csv    | 1T102C  | 0P322F  | 011025/123 | 01021998/S/CSVA |


  @duplicateChecks @stable
  Scenario Outline: Not duplicate if different fee code
    Given I generate two Legal help files in "<format>" format for office "<office>" that are "1" months apart with the following claims
      | ucn   | ufn   | feeCode1 | feeCode2 |
      | <ucn> | <ufn> | ICISD    | ICSSD    |

    When I upload the first file
    And click import
    When I upload the second file
    Then I should see the submission summary for "Legal help"

    Examples:
      | format | office | ufn         | ucn             |
      | csv    | 2P746R | 301025/§123 | 01021998/S/CSVA |


  @duplicateChecks
  Scenario Outline: Duplicate rule – should reject second submission for matching claims generated <monthsDifference> months apart
    Given I generate two Legal help files in "<format>" format for office "<office>" that are "<monthsDifference>" months apart with the following claims
      | ucn   | feeCode1  | feeCode2  | ufn   |
      | <ucn> | <feeCode> | <feeCode> | <ufn> |
    When I upload the first file
    And click import
    When I upload the second file
    Then I should see the following submission error messages for "Legal help":
      | Error Message  |
      | <errorMessage> |

    @smoke
    Examples:
      | format | office | ucn             | ufn        | feeCode | monthsDifference | errorMessage                                      |
      | csv    | 0P322F | 05011998/S/CSVA | 020825/523 | ICISD   | 2                | A duplicate claim was found in another submission |

    Examples:
      | format | office | ucn             | ufn        | feeCode | monthsDifference | errorMessage                                      |
      | csv    | 2L849T | 03011998/S/CSVA | 010725/323 | ICISD   | 0                | Submission already exists for Office              |
      | csv    | 0P322F | 04011998/S/CSVA | 020825/423 | ICISD   | 1                | A duplicate claim was found in another submission |

  @duplicateChecks
  Scenario: Duplicate claim is accepted when earlier CCD is on or before the cutoff
    Given I generate two Legal help files outside the duplicate cutoff in "csv" format for office "0P322F" with the following claims
      | ucn             | feeCode1 | feeCode2 | ufn        |
      | 06011998/S/CSVA | ICISD    | ICISD    | 020825/623 |
    When I upload the first file
    Then I should see the submission summary for "Legal help" with "1" claims
    And click import
    When I upload the second file
    Then I should see the submission summary for "Legal help" with "1" claims

#  @duplicateChecks
#  Scenario: Voided claims allow duplicate resubmission 2 months apart
#    Given I generate two Legal help files in "csv" format for office "0P322F" that are "2" months apart with the following claims
#      | ucn             | feeCode1 | feeCode2 | ufn        |
#      | 05011998/S/CSVA | ICISD    | ICISD    | 020825/523 |
#    When I upload the first file
#    Then I should see the submission summary for "Legal help" with "1" claims
#    When I void claim 1 via the void endpoint
#    And click import
#    When I upload the second file
#    Then I should see the submission summary for "Legal help" with "1" claims
#
