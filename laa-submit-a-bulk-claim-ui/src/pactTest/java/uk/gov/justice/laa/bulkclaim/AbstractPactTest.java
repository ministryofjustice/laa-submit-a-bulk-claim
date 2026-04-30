package uk.gov.justice.laa.bulkclaim;

import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.LambdaDslJsonBody;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class AbstractPactTest {
  public static final String CONSUMER = "laa-submit-a-bulk-claim";
  public static final String PROVIDER = "laa-data-claims-api";
  protected static final String QUERY_PARAM_SUBMISSION_ID = "submission_id";
  protected static final String QUERY_PARAM_OFFICE_CODE = "office_code";
  protected static final String QUERY_PARAM_PAGE = "page";
  protected static final String QUERY_PARAM_SIZE = "size";
  protected static final String QUERY_PARAM_SORT = "sort";

  protected static final String UUID_REGEX =
      "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
  protected static final String OFFICE_CODE_REGEX = "([A-Z0-9]{6})";
  protected static final String ANY_FORMAT_REGEX =
      "([a-zA-Z0-9 !\"£$%^&*()_+\\-=\\[\\]{};'#:@~,./<>?\\\\|`¬]+)";

  // Any number, but not 0 alone. Maximum 8 digits
  protected static final String ANY_NUMBER_REGEX = "([1-9][0-9]{0,7})";
  protected static final String SORT_REGEX = "[a-zA-Z]+,(asc|desc)";
  protected static final String SORT_CLAIMS_REGEX_V2 =
      "(client_surname|client_forename|client_2_surname|client_2_forename|unique_file_number|unique_client_number|fee_code|case_concluded_date|total_amount|escape_case_flag|total_warnings),(asc|desc)";

  protected static final List<String> USER_OFFICES = List.of("ABC123", "XYZ789");
  protected static final UUID BULK_SUBMISSION_ID = UUID.randomUUID();
  protected static final UUID SUBMISSION_ID = UUID.randomUUID();
  protected static final UUID CLAIM_ID = UUID.randomUUID();
  protected static final UUID MATTER_START_ID = UUID.randomUUID();
  protected static final LambdaDslJsonBody EXPECTED_BODY =
      LambdaDsl.newJsonBody(
          body -> {
            body.minArrayLike(
                "content",
                1,
                claim -> {
                  claim.stringType("id", "string");
                  claim.stringType("submission_id", "string");
                  claim.stringType("status", "READY_TO_PROCESS");
                  claim.stringType("schedule_reference", "string");
                  claim.numberType("line_number", 0);
                  claim.stringType("case_reference_number", "string");
                  claim.stringType("unique_file_number", "string");
                  claim.stringType("case_start_date", "string");
                  claim.stringType("case_concluded_date", "string");
                  claim.stringType("matter_type_code", "string");
                  claim.stringType("crime_matter_type_code", "string");
                  claim.stringType("fee_scheme_code", "string");
                  claim.stringType("fee_code", "string");
                  claim.stringType("procurement_area_code", "string");
                  claim.stringType("access_point_code", "string");
                  claim.stringType("delivery_location", "string");
                  claim.stringType("representation_order_date", "string");
                  claim.numberType("suspects_defendants_count", 0);
                  claim.numberType("police_station_court_attendances_count", 0);
                  claim.stringType("police_station_court_prison_id", "string");
                  claim.stringType("dscc_number", "string");
                  claim.stringType("maat_id", "string");
                  claim.stringType("prison_law_prior_approval_number", "string");
                  claim.booleanType("is_duty_solicitor", true);
                  claim.booleanType("is_youth_court", true);
                  claim.stringType("scheme_id", "string");
                  claim.numberType("mediation_sessions_count", 0);
                  claim.numberType("mediation_time_minutes", 0);
                  claim.stringType("outreach_location", "string");
                  claim.stringType("referral_source", "string");
                  claim.stringType("client_forename", "string");
                  claim.stringType("client_surname", "string");
                  claim.stringType("client_date_of_birth", "string");
                  claim.stringType("unique_client_number", "string");
                  claim.stringType("client_postcode", "string");
                  claim.stringType("gender_code", "string");
                  claim.stringType("ethnicity_code", "string");
                  claim.stringType("disability_code", "string");
                  claim.booleanType("is_legally_aided", true);
                  claim.stringType("client_type_code", "string");
                  claim.stringType("home_office_client_number", "string");
                  claim.stringType("cla_reference_number", "string");
                  claim.stringType("cla_exemption_code", "string");
                  claim.stringType("client_2_forename", "string");
                  claim.stringType("client_2_surname", "string");
                  claim.stringType("client_2_date_of_birth", "string");
                  claim.stringType("client_2_ucn", "string");
                  claim.stringType("client_2_postcode", "string");
                  claim.stringType("client_2_gender_code", "string");
                  claim.stringType("client_2_ethnicity_code", "string");
                  claim.stringType("client_2_disability_code", "string");
                  claim.booleanType("client_2_is_legally_aided", true);
                  claim.stringType("case_id", "string");
                  claim.stringType("unique_case_id", "string");
                  claim.stringType("case_stage_code", "string");
                  claim.stringType("stage_reached_code", "string");
                  claim.stringType("standard_fee_category_code", "string");
                  claim.stringType("outcome_code", "string");
                  claim.stringType("designated_accredited_representative_code", "string");
                  claim.booleanType("is_postal_application_accepted", true);
                  claim.booleanType("is_client_2_postal_application_accepted", true);
                  claim.stringType("mental_health_tribunal_reference", "string");
                  claim.booleanType("is_nrm_advice", true);
                  claim.stringType("follow_on_work", "string");
                  claim.stringType("transfer_date", "string");
                  claim.stringType("exemption_criteria_satisfied", "string");
                  claim.stringType("exceptional_case_funding_reference", "string");
                  claim.booleanType("is_legacy_case", true);
                  claim.numberType("advice_time", 0);
                  claim.numberType("travel_time", 0);
                  claim.numberType("waiting_time", 0);
                  claim.numberType("net_profit_costs_amount", 0);
                  claim.numberType("net_disbursement_amount", 0);
                  claim.numberType("net_counsel_costs_amount", 0);
                  claim.numberType("disbursements_vat_amount", 0);
                  claim.numberType("travel_waiting_costs_amount", 0);
                  claim.numberType("net_waiting_costs_amount", 0);
                  claim.booleanType("is_vat_applicable", true);
                  claim.booleanType("is_tolerance_applicable", true);
                  claim.stringType("prior_authority_reference", "string");
                  claim.booleanType("is_london_rate", true);
                  claim.numberType("adjourned_hearing_fee_amount", 0);
                  claim.booleanType("is_additional_travel_payment", true);
                  claim.numberType("costs_damages_recovered_amount", 0);
                  claim.stringType("meetings_attended_code", "string");
                  claim.numberType("detention_travel_waiting_costs_amount", 0);
                  claim.numberType("jr_form_filling_amount", 0);
                  claim.booleanType("is_eligible_client", true);
                  claim.stringType("court_location_code", "string");
                  claim.stringType("advice_type_code", "string");
                  claim.numberType("medical_reports_count", 0);
                  claim.booleanType("is_irc_surgery", true);
                  claim.stringType("surgery_date", "string");
                  claim.numberType("surgery_clients_count", 0);
                  claim.numberType("surgery_matters_count", 0);
                  claim.numberType("cmrh_oral_count", 0);
                  claim.numberType("cmrh_telephone_count", 0);
                  claim.stringType("ait_hearing_centre_code", "string");
                  claim.booleanType("is_substantive_hearing", true);
                  claim.numberType("ho_interview", 0);
                  claim.stringType("local_authority_number", "string");
                  claim.stringType("submission_period", "string");
                  claim.stringType("created_by_user_id", "string");
                  claim.numberType("total_warnings", 0);
                  claim.object(
                      "fee_calculation_response",
                      fee -> {
                        fee.stringType("calculated_fee_detail_id", "string");
                        fee.uuid(
                            "claim_summary_fee_id",
                            UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
                        fee.uuid(
                            "claim_id", UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
                        fee.stringType("fee_code", "string");
                        fee.stringType("fee_code_description", "string");
                        fee.stringType("fee_type", "HOURLY");
                        fee.stringType("category_of_law", "string");
                        fee.numberType("total_amount", 0);
                        fee.booleanType("vat_indicator", true);
                        fee.numberType("vat_rate_applied", 0);
                        fee.numberType("calculated_vat_amount", 0);
                        fee.numberType("disbursement_amount", 0);
                        fee.numberType("requested_net_disbursement_amount", 0);
                        fee.numberType("disbursement_vat_amount", 0);
                        fee.numberType("hourly_total_amount", 0);
                        fee.numberType("fixed_fee_amount", 0);
                        fee.numberType("net_profit_costs_amount", 0);
                        fee.numberType("requested_net_profit_costs_amount", 0);
                        fee.numberType("net_cost_of_counsel_amount", 0);
                        fee.numberType("net_travel_costs_amount", 0);
                        fee.numberType("net_waiting_costs_amount", 0);
                        fee.numberType("detention_travel_and_waiting_costs_amount", 0);
                        fee.numberType("jr_form_filling_amount", 0);
                        fee.numberType("travel_and_waiting_costs_amount", 0);
                        fee.object(
                            "bolt_on_details",
                            bolt -> {
                              bolt.numberType("bolt_on_total_fee_amount", 0);
                              bolt.numberType("bolt_on_adjourned_hearing_count", 0);
                              bolt.numberType("bolt_on_adjourned_hearing_fee", 0);
                              bolt.numberType("bolt_on_cmrh_telephone_count", 0);
                              bolt.numberType("bolt_on_cmrh_telephone_fee", 0);
                              bolt.numberType("bolt_on_cmrh_oral_count", 0);
                              bolt.numberType("bolt_on_cmrh_oral_fee", 0);
                              bolt.numberType("bolt_on_home_office_interview_count", 0);
                              bolt.numberType("bolt_on_home_office_interview_fee", 0);
                              bolt.numberType("bolt_on_substantive_hearing_fee", 0);
                              bolt.booleanType("escape_case_flag", true);
                              bolt.stringType("scheme_id", "string");
                            });
                      });
                });
            body.numberType("total_pages", 1);
            body.numberType("total_elements", 1);
            body.numberType("number", 0);
            body.numberType("size", 10);
          });

  protected static final LambdaDslJsonBody EMPTY_BODY =
      LambdaDsl.newJsonBody(
          body -> {
            body.array("content", array -> {});
            body.numberType("total_pages", 0);
            body.numberType("total_elements", 0);
            body.numberType("number", 0);
            body.numberType("size", 10);
          });

  protected static final LambdaDslJsonBody EXPECTED_CLAIMS_SEARCH_RESULTS_V2 =
      LambdaDsl.newJsonBody(
          body -> {
            body.minArrayLike(
                "content",
                1,
                claim -> {
                  claim.stringType("id", "string");
                  claim.stringType("submission_id", "string");
                  claim.numberType("line_number", 0);
                  claim.stringType("unique_file_number", "string");
                  claim.stringType("case_concluded_date", "string");
                  claim.stringType("fee_code", "string");
                  claim.stringType("maat_id", "string");
                  claim.stringType("client_forename", "string");
                  claim.stringType("client_surname", "string");
                  claim.stringType("unique_client_number", "string");
                  claim.stringType("client_2_forename", "string");
                  claim.stringType("client_2_surname", "string");
                  claim.stringType("client_2_ucn", "string");
                  claim.numberType("net_profit_costs_amount", 0);
                  claim.numberType("net_disbursement_amount", 0);
                  claim.numberType("net_counsel_costs_amount", 0);
                  claim.numberType("disbursements_vat_amount", 0);
                  claim.stringType("submission_period", "string");
                  claim.numberType("total_warnings", 0);
                  claim.object(
                      "fee_calculation_response",
                      fee -> {
                        fee.stringType("calculated_fee_detail_id", "string");
                        fee.uuid(
                            "claim_summary_fee_id",
                            UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
                        fee.uuid(
                            "claim_id", UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
                        fee.stringType("fee_code", "string");
                        fee.stringType("fee_code_description", "string");
                        fee.stringType("fee_type", "HOURLY");
                        fee.stringType("category_of_law", "string");
                        fee.numberType("total_amount", 0);
                        fee.booleanType("vat_indicator", true);
                        fee.numberType("vat_rate_applied", 0);
                        fee.numberType("calculated_vat_amount", 0);
                        fee.numberType("disbursement_amount", 0);
                        fee.numberType("requested_net_disbursement_amount", 0);
                        fee.numberType("disbursement_vat_amount", 0);
                        fee.numberType("hourly_total_amount", 0);
                        fee.numberType("fixed_fee_amount", 0);
                        fee.numberType("net_profit_costs_amount", 0);
                        fee.numberType("requested_net_profit_costs_amount", 0);
                        fee.numberType("net_cost_of_counsel_amount", 0);
                        fee.numberType("net_travel_costs_amount", 0);
                        fee.numberType("net_waiting_costs_amount", 0);
                        fee.numberType("detention_travel_and_waiting_costs_amount", 0);
                        fee.numberType("jr_form_filling_amount", 0);
                        fee.numberType("travel_and_waiting_costs_amount", 0);
                        fee.object(
                            "bolt_on_details",
                            bolt -> {
                              bolt.numberType("bolt_on_total_fee_amount", 0);
                              bolt.numberType("bolt_on_adjourned_hearing_count", 0);
                              bolt.numberType("bolt_on_adjourned_hearing_fee", 0);
                              bolt.numberType("bolt_on_cmrh_telephone_count", 0);
                              bolt.numberType("bolt_on_cmrh_telephone_fee", 0);
                              bolt.numberType("bolt_on_cmrh_oral_count", 0);
                              bolt.numberType("bolt_on_cmrh_oral_fee", 0);
                              bolt.numberType("bolt_on_home_office_interview_count", 0);
                              bolt.numberType("bolt_on_home_office_interview_fee", 0);
                              bolt.numberType("bolt_on_substantive_hearing_fee", 0);
                              bolt.booleanType("escape_case_flag", true);
                              bolt.stringType("scheme_id", "string");
                            });
                      });
                });
            body.numberType("total_pages", 1);
            body.numberType("total_elements", 1);
            body.numberType("number", 0);
            body.numberType("size", 10);
          });

  @MockitoBean OAuth2AuthorizedClientManager authorizedClientManager;
}
