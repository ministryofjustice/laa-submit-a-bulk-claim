package uk.gov.justice.laa.bulkclaim.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.inquest.InquestDepartment;

class InquestDepartmentServiceTest {

  private final DataClaimsRestClient client = Mockito.mock(DataClaimsRestClient.class);
  private final InquestDepartmentService service = new InquestDepartmentService(client);

  @Test
  void fetchesActiveDepartmentsOnceForTheApplicationInstance() {
    when(client.getInquestDepartments())
        .thenReturn(
            List.of(
                new InquestDepartment("AGO", "Attorney General's Office", 1, true),
                new InquestDepartment("OLD", "Old department", 2, false)));

    assertThat(service.getActiveDepartments())
        .extracting(InquestDepartment::code)
        .containsExactly("AGO");
    assertThat(service.getActiveDepartments())
        .extracting(InquestDepartment::code)
        .containsExactly("AGO");

    verify(client).getInquestDepartments();
    verifyNoMoreInteractions(client);
  }
}
