package uk.gov.justice.laa.bulkclaim.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UploadInProgressSummary(LocalDate uploadDate, UUID submissionReference,
                                      String fileName) {

}
