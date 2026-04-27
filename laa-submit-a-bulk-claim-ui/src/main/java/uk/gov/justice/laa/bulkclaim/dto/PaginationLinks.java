package uk.gov.justice.laa.bulkclaim.dto;

import java.util.List;

/**
 * Represents pagination navigation links.
 *
 * @param previousHref the URL for the previous page
 * @param nextHref the URL for the next page
 * @param pageLinks the list of page links
 */
public record PaginationLinks(
    String previousHref, String nextHref, List<PaginationPageLink> pageLinks) {}
