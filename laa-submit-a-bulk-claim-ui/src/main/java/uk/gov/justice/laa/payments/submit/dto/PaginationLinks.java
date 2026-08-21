package uk.gov.justice.laa.payments.submit.dto;

import java.util.List;

public record PaginationLinks(
    String previousHref, String nextHref, List<PaginationPageLink> pageLinks) {}
