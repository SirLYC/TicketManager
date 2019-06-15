package com.lyc.TicketManager_Backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession(redisNamespace = "ticket:manager:backend")
public class RedisSessionConfig {
}
