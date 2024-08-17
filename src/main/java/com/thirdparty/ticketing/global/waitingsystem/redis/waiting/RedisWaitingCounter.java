package com.thirdparty.ticketing.global.waitingsystem.redis.waiting;

import com.thirdparty.ticketing.domain.waitingsystem.waiting.WaitingCounter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class RedisWaitingCounter implements WaitingCounter {

    private static final String WAITING_COUNTER_KEY = "waiting_counter:";

    private final ValueOperations<String, String> waitingCounter;

    public RedisWaitingCounter(StringRedisTemplate redisTemplate) {
        waitingCounter = redisTemplate.opsForValue();
    }

    public long getNextCount(long performanceId) {
        return waitingCounter.increment(getWaitingCounterKey(performanceId), 1);
    }

    private String getWaitingCounterKey(long performanceId) {
        return WAITING_COUNTER_KEY + performanceId;
    }
}
