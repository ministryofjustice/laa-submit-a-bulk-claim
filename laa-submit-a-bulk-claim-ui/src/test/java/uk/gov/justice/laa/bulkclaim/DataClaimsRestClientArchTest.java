package uk.gov.justice.laa.bulkclaim;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.service.ClaimService;
import uk.gov.justice.laa.bulkclaim.service.SubmissionService;

@AnalyzeClasses(
    packagesOf = SubmitABulkClaimApplication.class,
    importOptions = ImportOption.DoNotIncludeTests.class)
class DataClaimsRestClientArchTest {

  @ArchTest
  static final ArchRule getSubmissionOnlyCalledBySubmissionServiceGetSubmission =
      methods()
          .that()
          .areDeclaredIn(DataClaimsRestClient.class)
          .and()
          .haveName("getSubmission")
          .should()
          .onlyBeCalled()
          .byMethodsThat(isMethodNamed(SubmissionService.class, "getSubmission"))
          .because(
              "DataClaimsRestClient.getSubmission must go through SubmissionService.getSubmission"
                  + " to ensure office access is checked");

  @ArchTest
  static final ArchRule getSubmissionClaimOnlyCalledByGetClaimV2 =
      methods()
          .that()
          .areDeclaredIn(DataClaimsRestClientV2.class)
          .and()
          .haveName("getSubmissionClaim")
          .should()
          .onlyBeCalled()
          .byMethodsThat(isMethodNamed(ClaimService.class, "getClaimV2"))
          .because(
              "DataClaimsRestClientV2.getSubmissionClaim must go through"
                  + " ClaimService.getClaimV2 to ensure office access is checked");

  private static DescribedPredicate<JavaMethod> isMethodNamed(
      Class<?> ownerClass, String methodName) {
    return DescribedPredicate.describe(
        "be %s.%s".formatted(ownerClass.getSimpleName(), methodName),
        method ->
            method.getOwner().isEquivalentTo(ownerClass) && method.getName().equals(methodName));
  }
}
