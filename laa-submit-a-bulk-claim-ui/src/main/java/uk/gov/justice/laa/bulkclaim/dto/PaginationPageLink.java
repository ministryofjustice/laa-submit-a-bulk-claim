package uk.gov.justice.laa.bulkclaim.dto;

/**
 * Represents a pagination page link.
 *
 * @param pageNumber the page number
 * @param href the URL for the page
 */
public record PaginationPageLink(int pageNumber, String href) {}
