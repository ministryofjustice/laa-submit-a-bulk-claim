package uk.gov.justice.laa.bulkclaim.util;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.PaginationLinks;
import uk.gov.justice.laa.bulkclaim.dto.PaginationPageLink;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;

/** Utility for constructing pagination links for navigation. */
@Component
@RequiredArgsConstructor
public class PaginationLinksBuilder {

  private final ThymeleafHrefUtils thymeleafHrefUtils;

  /**
   * Builds pagination links based on page data.
   *
   * @param currentUrl the current request URL
   * @param page the page data
   * @param pageVarName the page parameter name
   * @param params additional parameters for the URL
   * @return the constructed pagination links
   */
  public PaginationLinks build(String currentUrl, Page page, String pageVarName, Object... params) {
    if (page == null || page.getTotalPages() == null) {
      return new PaginationLinks(null, null, List.of());
    }

    List<PaginationPageLink> pageLinks = new ArrayList<>();
    for (int pageNumber = 0; pageNumber < page.getTotalPages(); pageNumber++) {
      pageLinks.add(
          new PaginationPageLink(
              pageNumber, buildHref(currentUrl, pageVarName, pageNumber, params)));
    }

    String previousHref =
        page.getNumber() != null && page.getNumber() > 0
            ? buildHref(currentUrl, pageVarName, page.getNumber() - 1, params)
            : null;
    String nextHref =
        page.getNumber() != null && page.getNumber() < page.getTotalPages() - 1
            ? buildHref(currentUrl, pageVarName, page.getNumber() + 1, params)
            : null;

    return new PaginationLinks(previousHref, nextHref, pageLinks);
  }

  /**
   * Builds a URL with pagination parameters.
   *
   * @param currentUrl the current request URL
   * @param pageVarName the page parameter name
   * @param pageNumber the page number to include
   * @param params additional parameters for the URL
   * @return the constructed URL
   */
  private String buildHref(
      String currentUrl, String pageVarName, int pageNumber, Object... params) {
    Object[] linkParams = new Object[params.length + 2];
    linkParams[0] = pageVarName;
    linkParams[1] = pageNumber;
    System.arraycopy(params, 0, linkParams, 2, params.length);
    return thymeleafHrefUtils.build(currentUrl, linkParams);
  }
}
