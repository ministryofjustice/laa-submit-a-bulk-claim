package uk.gov.justice.laa.bulkclaim.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Configuration class for enabling Redis-backed HTTP sessions.
 *
 * <p>This class uses Spring Session's {@code @EnableRedisHttpSession} annotation to configure Redis
 * as the storage mechanism for HTTP session data. The `redisNamespace` attribute sets the namespace
 * used to isolate session data within Redis.
 *
 * <p>By enabling Redis-backed sessions, this configuration ensures that session data is distributed
 * and persistent, allowing scalability and fault tolerance for the application.
 */
@Configuration
@EnableRedisHttpSession(redisNamespace = "submit-a-bulk-claim:session")
public class RedisSessionConfig {}
