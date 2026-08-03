package uk.gov.justice.laa.bulkclaim.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.inquest.InquestDepartment;

@Service
@RequiredArgsConstructor
public class InquestDepartmentService {

  private final DataClaimsRestClient dataClaimsRestClient;
  private volatile List<InquestDepartment> activeDepartments;

  public List<InquestDepartment> getActiveDepartments() {
    var cached = activeDepartments;
    if (cached == null) {
      synchronized (this) {
        cached = activeDepartments;
        if (cached == null) {
          cached =
              dataClaimsRestClient.getInquestDepartments().stream()
                  .filter(department -> Boolean.TRUE.equals(department.active()))
                  .toList();
          activeDepartments = cached;
        }
      }
    }
    return cached;
  }
}
