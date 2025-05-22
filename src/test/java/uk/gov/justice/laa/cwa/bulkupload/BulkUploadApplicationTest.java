package uk.gov.justice.laa.cwa.bulkupload;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.profiles.active=test")
class BulkUploadApplicationTest {
    @Test
    void contextLoads() {
        // empty due to only testing context load
    }
}