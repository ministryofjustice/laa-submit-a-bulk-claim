package uk.gov.justice.laa.bulkclaim.ui.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for generating mock claim data for sorting tests.
 *
 * Provides factory methods to create realistic claim objects with sortable fields, maintaining
 * data integrity across different area of law types.
 */
public class MockClaimDataGenerator {

  private static final List<String> SURNAMES = List.of(
      "Smith", "Johnson", "Williams", "Jones", "Brown", "Garcia", "Miller", "Davis",
      "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
      "Thomas", "Taylor", "Moore", "Jackson", "Martin"
  );

  private static final List<String> FORENAMES = List.of(
      "John", "Jane", "Michael", "Mary", "Robert", "Patricia", "James", "Jennifer",
      "William", "Linda", "Richard", "Barbara", "Joseph", "Elizabeth", "Thomas",
      "Susan", "David", "Jessica", "Charles", "Sarah"
  );

  private static final List<String> FEE_CODES = List.of(
      "A", "B", "C", "D", "E", "F", "G", "H"
  );

  private static final List<String> ESCAPE_CASES = List.of(
      "No", "No", "No", "Escaped", "No", "Escaped", "No", "No"
  );

  private static final List<String> MESSAGES = List.of(
      "", "", "", "Warning message", "", "Error message", "Info message", ""
  );

  /**
   * Generates a mock claim for Legal help area of law.
   *
   * @param index unique identifier for generating consistent values
   * @return mock claim data as a map (typically converted to JSON/Object)
   */
  public static MockClaim generateLegalHelpClaim(int index) {
    return new MockClaim()
        .withSurname(SURNAMES.get(index % SURNAMES.size()))
        .withForename(FORENAMES.get((index + 1) % FORENAMES.size()))
        .withUfn(String.format("%06d/001", index + 100000))
        .withUcn(String.format("%010d", 1000000000L + index))
        .withFeeCode(FEE_CODES.get(index % FEE_CODES.size()))
        .withCalculatedValue(1000.00 + (index * 50.00))
        .withEscapeCase(ESCAPE_CASES.get(index % ESCAPE_CASES.size()))
        .withMessages(index % 3 == 0 ? MESSAGES.get(3) : "");
  }

  /**
   * Generates a mock claim for Mediation area of law.
   *
   * @param index unique identifier for generating consistent values
   * @return mock claim data as a map (typically converted to JSON/Object)
   */
  public static MockClaim generateMediationClaim(int index) {
    int offset = (index + 5) % FORENAMES.size();
    return new MockClaim()
        .withSurname(SURNAMES.get(index % SURNAMES.size()))
        .withForename(FORENAMES.get((index + 1) % FORENAMES.size()))
        .withUcn(String.format("%010d", 1000000000L + index))
        .withSurnameTwo(SURNAMES.get((index + 2) % SURNAMES.size()))
        .withForenameTwo(FORENAMES.get(offset))
        .withUcnTwo(String.format("%010d", 2000000000L + index))
        .withFeeCode(FEE_CODES.get(index % FEE_CODES.size()))
        .withCalculatedValue(1500.00 + (index * 75.00))
        .withMessages(index % 4 == 0 ? MESSAGES.get(4) : "");
  }

  /**
   * Generates a mock claim for Crime lower area of law.
   *
   * @param index unique identifier for generating consistent values
   * @return mock claim data as a map (typically converted to JSON/Object)
   */
  public static MockClaim generateCrimeLowerClaim(int index) {
    return new MockClaim()
        .withSurname(SURNAMES.get(index % SURNAMES.size()))
        .withInitial(String.valueOf(FORENAMES.get((index + 1) % FORENAMES.size()).charAt(0)))
        .withUfn(String.format("%06d/001", index + 100000))
        .withFeeCode(FEE_CODES.get(index % FEE_CODES.size()))
        .withDateWorkConcluded(String.format("%02d/01/2024", (index % 12) + 1))
        .withCalculatedValue(800.00 + (index * 40.00))
        .withEscapeCase(ESCAPE_CASES.get(index % ESCAPE_CASES.size()))
        .withMessages(index % 5 == 0 ? MESSAGES.get(5) : "");
  }

  /**
   * Generates a submission with mock claims for Legal help.
   *
   * @param claimCount number of claims to generate
   * @return submission with claims
   */
  public static MockSubmission generateLegalHelpSubmission(int claimCount) {
    List<MockClaim> claims = new ArrayList<>();
    for (int i = 0; i < claimCount; i++) {
      claims.add(generateLegalHelpClaim(i));
    }

    return new MockSubmission()
        .withSubmissionId(UUID.randomUUID().toString())
        .withAreaOfLaw("Legal help")
        .withSubmissionPeriod("JAN-2024")
        .withClaimCount(claimCount)
        .withClaims(claims);
  }

  /**
   * Generates a submission with mock claims for Mediation.
   *
   * @param claimCount number of claims to generate
   * @return submission with claims
   */
  public static MockSubmission generateMediationSubmission(int claimCount) {
    List<MockClaim> claims = new ArrayList<>();
    for (int i = 0; i < claimCount; i++) {
      claims.add(generateMediationClaim(i));
    }

    return new MockSubmission()
        .withSubmissionId(UUID.randomUUID().toString())
        .withAreaOfLaw("Mediation")
        .withSubmissionPeriod("FEB-2024")
        .withClaimCount(claimCount)
        .withClaims(claims);
  }

  /**
   * Generates a submission with mock claims for Crime lower.
   *
   * @param claimCount number of claims to generate
   * @return submission with claims
   */
  public static MockSubmission generateCrimeLowerSubmission(int claimCount) {
    List<MockClaim> claims = new ArrayList<>();
    for (int i = 0; i < claimCount; i++) {
      claims.add(generateCrimeLowerClaim(i));
    }

    return new MockSubmission()
        .withSubmissionId(UUID.randomUUID().toString())
        .withAreaOfLaw("Crime lower")
        .withSubmissionPeriod("MAR-2024")
        .withClaimCount(claimCount)
        .withClaims(claims);
  }

  /**
   * Mock claim object for easy data manipulation.
   */
  public static class MockClaim {
    private String surname;
    private String forename;
    private String initial;
    private String ufn;
    private String ucn;
    private String surnameTwo;
    private String forenameTwo;
    private String ucnTwo;
    private String feeCode;
    private Double calculatedValue;
    private String sortValue;
    private String escapeCase;
    private String dateWorkConcluded;
    private String messages;

    public MockClaim withSurname(String surname) {
      this.surname = surname;
      return this;
    }

    public MockClaim withForename(String forename) {
      this.forename = forename;
      return this;
    }

    public MockClaim withInitial(String initial) {
      this.initial = initial;
      return this;
    }

    public MockClaim withUfn(String ufn) {
      this.ufn = ufn;
      return this;
    }

    public MockClaim withUcn(String ucn) {
      this.ucn = ucn;
      return this;
    }

    public MockClaim withSurnameTwo(String surnameTwo) {
      this.surnameTwo = surnameTwo;
      return this;
    }

    public MockClaim withForenameTwo(String forenameTwo) {
      this.forenameTwo = forenameTwo;
      return this;
    }

    public MockClaim withUcnTwo(String ucnTwo) {
      this.ucnTwo = ucnTwo;
      return this;
    }

    public MockClaim withFeeCode(String feeCode) {
      this.feeCode = feeCode;
      return this;
    }

    public MockClaim withCalculatedValue(Double calculatedValue) {
      this.calculatedValue = calculatedValue;
      // Calculate sort value as cents (integer) for proper numeric sorting
      this.sortValue = String.valueOf((long) (calculatedValue * 100));
      return this;
    }

    public MockClaim withEscapeCase(String escapeCase) {
      this.escapeCase = escapeCase;
      return this;
    }

    public MockClaim withDateWorkConcluded(String dateWorkConcluded) {
      this.dateWorkConcluded = dateWorkConcluded;
      return this;
    }

    public MockClaim withMessages(String messages) {
      this.messages = messages;
      return this;
    }

    public String getSurname() { return surname; }
    public String getForename() { return forename; }
    public String getInitial() { return initial; }
    public String getUfn() { return ufn; }
    public String getUcn() { return ucn; }
    public String getSurnameTwo() { return surnameTwo; }
    public String getForenameTwo() { return forenameTwo; }
    public String getUcnTwo() { return ucnTwo; }
    public String getFeeCode() { return feeCode; }
    public Double getCalculatedValue() { return calculatedValue; }
    public String getSortValue() { return sortValue; }
    public String getEscapeCase() { return escapeCase; }
    public String getDateWorkConcluded() { return dateWorkConcluded; }
    public String getMessages() { return messages; }

    @Override
    public String toString() {
      return "MockClaim{" +
          "surname='" + surname + '\'' +
          ", forename='" + forename + '\'' +
          ", calculatedValue=" + calculatedValue +
          ", ufn='" + ufn + '\'' +
          '}';
    }
  }

  /**
   * Mock submission object for easy data manipulation.
   */
  public static class MockSubmission {
    private String submissionId;
    private String areaOfLaw;
    private String submissionPeriod;
    private int claimCount;
    private List<MockClaim> claims;

    public MockSubmission withSubmissionId(String submissionId) {
      this.submissionId = submissionId;
      return this;
    }

    public MockSubmission withAreaOfLaw(String areaOfLaw) {
      this.areaOfLaw = areaOfLaw;
      return this;
    }

    public MockSubmission withSubmissionPeriod(String submissionPeriod) {
      this.submissionPeriod = submissionPeriod;
      return this;
    }

    public MockSubmission withClaimCount(int claimCount) {
      this.claimCount = claimCount;
      return this;
    }

    public MockSubmission withClaims(List<MockClaim> claims) {
      this.claims = claims;
      return this;
    }

    public String getSubmissionId() { return submissionId; }
    public String getAreaOfLaw() { return areaOfLaw; }
    public String getSubmissionPeriod() { return submissionPeriod; }
    public int getClaimCount() { return claimCount; }
    public List<MockClaim> getClaims() { return claims; }

    @Override
    public String toString() {
      return "MockSubmission{" +
          "submissionId='" + submissionId + '\'' +
          ", areaOfLaw='" + areaOfLaw + '\'' +
          ", claimCount=" + claimCount +
          '}';
    }
  }
}

