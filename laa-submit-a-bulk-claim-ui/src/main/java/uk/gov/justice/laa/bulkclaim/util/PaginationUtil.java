package uk.gov.justice.laa.bulkclaim.util;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagesResponse;

/** Factory responsible for constructing {@link Page} instances for use by the view layer. */
@Component
public class PaginationUtil {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_PAGE_SIZE = 10;

  public Page fromSubmissionsResultSet(
      SubmissionsResultSet resultSet, Integer requestedPage, Integer requestedSize) {
    if (resultSet == null) {
      return from(requestedPage, requestedSize, 0);
    }

    Integer number = Optional.ofNullable(resultSet.getNumber()).orElse(requestedPage);
    Integer size = Optional.ofNullable(resultSet.getSize()).orElse(requestedSize);
    Integer totalElements = Optional.ofNullable(resultSet.getTotalElements()).orElse(0);
    Integer totalPages =
        Optional.ofNullable(resultSet.getTotalPages())
            .orElseGet(() -> calculateTotalPages(totalElements, size));

    return buildPage(number, size, totalPages, totalElements);
  }

  public Page fromValidationMessages(
      ValidationMessagesResponse response, Integer requestedPage, Integer requestedSize) {
    if (response == null) {
      return from(requestedPage, requestedSize, 0);
    }

    Integer number = Optional.ofNullable(response.getNumber()).orElse(requestedPage);
    Integer size = Optional.ofNullable(response.getSize()).orElse(requestedSize);
    Integer totalElements = Optional.ofNullable(response.getTotalElements()).orElse(0);
    Integer totalPages =
        Optional.ofNullable(response.getTotalPages())
            .orElseGet(() -> calculateTotalPages(totalElements, size));

    return buildPage(number, size, totalPages, totalElements);
  }

  public Page from(int pageNumber, int pageSize, int totalElements) {
    int safePageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
    int safePageNumber = Math.max(pageNumber, DEFAULT_PAGE);
    int safeTotalElements = Math.max(totalElements, 0);
    int totalPages = calculateTotalPages(safeTotalElements, safePageSize);

    return buildPage(safePageNumber, safePageSize, totalPages, safeTotalElements);
  }

  private int calculateTotalPages(int totalElements, int pageSize) {
    if (pageSize <= 0) {
      return totalElements > 0 ? 1 : 0;
    }
    return (int) Math.ceil((double) totalElements / pageSize);
  }

  private Page buildPage(Integer number, Integer size, Integer totalPages, Integer totalElements) {
    Page page = new Page();
    page.setNumber(Optional.ofNullable(number).orElse(DEFAULT_PAGE));
    page.setSize(Optional.ofNullable(size).orElse(DEFAULT_PAGE_SIZE));
    page.setTotalPages(Optional.ofNullable(totalPages).orElse(0));
    page.setTotalElements(Optional.ofNullable(totalElements).orElse(0));
    return page;
  }
}
