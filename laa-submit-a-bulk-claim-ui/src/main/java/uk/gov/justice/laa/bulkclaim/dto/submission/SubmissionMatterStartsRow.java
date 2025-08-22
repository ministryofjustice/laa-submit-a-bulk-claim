package uk.gov.justice.laa.bulkclaim.dto.submission;

/**
 * Represents a single row on the matter starts table on the view submission page.
 *
 * @param description a description of the matter start
 * @param numberOfMatterStarts the number of matter starts
 * @author Jamie Briggs
 */
public record SubmissionMatterStartsRow(String description, int numberOfMatterStarts) {}
