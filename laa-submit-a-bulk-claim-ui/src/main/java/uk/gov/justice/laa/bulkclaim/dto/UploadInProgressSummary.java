package uk.gov.justice.laa.bulkclaim.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Summary of an upload in progress.
 *
 * @param uploadDate date and time of upload
 * @param submissionReference unique identifier for the bulk submission
 * @param fileName name of the file uploaded
 * @author Jamie Briggs
 */
public record UploadInProgressSummary(
    LocalDateTime uploadDate, UUID submissionReference, String fileName) {}
