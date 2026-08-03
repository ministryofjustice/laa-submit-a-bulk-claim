package uk.gov.justice.laa.bulkclaim.dto.inquest;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InquestDepartment(
    String code,
    @JsonProperty("display_label") String displayLabel,
    @JsonProperty("display_order") Integer displayOrder,
    @JsonProperty("is_active") Boolean active) {}
