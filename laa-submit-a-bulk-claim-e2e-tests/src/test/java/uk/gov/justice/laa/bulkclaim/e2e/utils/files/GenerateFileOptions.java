package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.util.List;
import java.util.Map;

/** TS-like generator options bag: suffix, office, submissionPeriod, claims. */
public class GenerateFileOptions {
  private String suffix;
  private String office;
  private String submissionPeriod;
  private List<Map<String, String>> claims;

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public String getOffice() {
    return office;
  }

  public void setOffice(String office) {
    this.office = office;
  }

  public String getSubmissionPeriod() {
    return submissionPeriod;
  }

  public void setSubmissionPeriod(String submissionPeriod) {
    this.submissionPeriod = submissionPeriod;
  }

  public List<Map<String, String>> getClaims() {
    return claims;
  }

  public void setClaims(List<Map<String, String>> claims) {
    this.claims = claims;
  }
}

