package uk.gov.justice.laa.bulkclaim.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.StringRedisTemplate;

public class SessionServiceTest {

  @Test
  void getActiveSessionCount_shouldReturnNonNegativeNumber() {
    StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
    RedisConnectionFactory factory = Mockito.mock(RedisConnectionFactory.class);
    RedisConnection connection = Mockito.mock(RedisConnection.class);
    RedisKeyCommands keyCommands = Mockito.mock(RedisKeyCommands.class);
    Cursor<byte[]> cursor = Mockito.mock(Cursor.class);

    Mockito.when(redisTemplate.getConnectionFactory()).thenReturn(factory);
    Mockito.when(factory.getConnection()).thenReturn(connection);
    Mockito.when(connection.keyCommands()).thenReturn(keyCommands);
    Mockito.when(
            keyCommands.scan(Mockito.any(org.springframework.data.redis.core.ScanOptions.class)))
        .thenReturn(cursor);

    Mockito.when(cursor.hasNext()).thenReturn(true, true, false);

    SessionService service = new SessionService(redisTemplate);

    int result = service.getActiveSessionCount();

    assertThat(result).isEqualTo(2);
  }
}
