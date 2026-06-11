package uk.gov.justice.laa.bulkclaim.service;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** Service class for counting number of active sessions. */
@Service
public class SessionService {
  private final StringRedisTemplate redisTemplate;

  public SessionService(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public int getActiveSessionCount() {
    return countSessions();
  }

  private int countSessions() {
    ScanOptions options =
        ScanOptions.scanOptions()
            .match("submit-a-bulk-claim:session:sessions:*")
            .count(1000)
            .build();
    int count = 0;

    try (Cursor<byte[]> cursor =
        redisTemplate.getConnectionFactory().getConnection().keyCommands().scan(options)) {
      while (cursor.hasNext()) {
        cursor.next();
        count++;
      }
    }
    return count;
  }
}
