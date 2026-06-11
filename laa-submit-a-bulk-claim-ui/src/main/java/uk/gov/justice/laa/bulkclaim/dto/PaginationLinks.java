package uk.gov.justice.laa.bulkclaim.dto;

import java.util.List;

public record PaginationLinks(
    String previousHref, String nextHref, List<PaginationPageLink> pageLinks) {}
