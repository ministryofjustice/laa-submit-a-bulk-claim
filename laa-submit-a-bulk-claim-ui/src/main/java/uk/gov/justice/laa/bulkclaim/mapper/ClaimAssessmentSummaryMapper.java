package uk.gov.justice.laa.bulkclaim.mapper;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimAssessmentSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimSummary;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

/**
 * Maps between {@link AssessmentGet} and {@link ClaimAssessmentSummary}.
 *
 * @author Gabriela Georgieva
 */
@Mapper(componentModel = "spring")
public interface ClaimAssessmentSummaryMapper {

    @Mapping(target = ".", source = "assessmentGet")
    @Mapping(target = "fixedFee", source = "fixedFeeAmount")
    @Mapping(
            target = "netProfitCost",
            source = "netProfitCostsAmount")
    @Mapping(target = "netDisbursments", source = "disbursementAmount")
    @Mapping(target = "disbursementVat", source = "disbursementVatAmount")
    @Mapping(target = "netCostOfCounsel", source = "netCostOfCounselAmount")
    @Mapping(
            target = "travelCosts",
            source = "netTravelCostsAmount")
    @Mapping(
            target = "waitingCosts",
            source = "netWaitingCostsAmount")
    @Mapping(target = "detentionTravelAndWaitingCosts", source = "detentionTravelAndWaitingCostsAmount")
    @Mapping(
            target = "jrFormFilling",
            source = "jrFormFillingAmount")
    @Mapping(target = "cmrhTelephone", source = "boltOnCmrhTelephoneFee")
    @Mapping(target = "cmrhOral", source = "boltOnCmrhOralFee")
    @Mapping(target = "homeOfficeInterview", source = "boltOnHomeOfficeInterviewFee")
    @Mapping(target = "substantiveHearing", source = "boltOnSubstantiveHearingFee")
    @Mapping(target = "adjournedHearingFee", source = "boltOnAdjournedHearingFee")
    @Mapping(target = "vatIndicator", source = "isVatApplicable")

    ClaimAssessmentSummary toClaimAssessmentSummary(
            AssessmentGet assessmentGet);
}
