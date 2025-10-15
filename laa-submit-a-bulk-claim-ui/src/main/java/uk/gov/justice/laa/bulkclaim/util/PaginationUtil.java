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

  /**
   * Builds a {@link Page} instance using pagination details from a {@link SubmissionsResultSet}.
   *
   * @param resultSet the result set returned from the Claims API (may be {@code null}).
   * @param requestedPage the requested page number.
   * @param requestedSize the requested page size.
   * @return a populated {@link Page} instance.
   */
  public Page fromSubmissionsResultSet(
      SubmissionsResultSet resultSet, int requestedPage, int requestedSize) {
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

  /**
   * Builds a {@link Page} instance using pagination details from a {@link
   * ValidationMessagesResponse}.
   *
   * @param response the response returned from the Claims API (may be {@code null}).
   * @param requestedPage the requested page number.
   * @param requestedSize the requested page size.
   * @return a populated {@link Page} instance.
   */
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

  /**
   * Builds a {@link Page} instance from the supplied pagination parameters.
   *
   * @param pageNumber the zero-based page number requested.
   * @param pageSize the page size requested.
   * @param totalElements the calculatedTotal number of elements available.
   * @return a populated {@link Page} instance.
   */
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
