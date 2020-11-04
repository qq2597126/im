package com.lcy.server.distributed.redis;

import com.lcy.common.constant.Constant;
import com.lcy.server.distributed.OnlineCounter;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 基于redis的分布式计数器
 */
@Data
@Component
public class RedisOnlineCounter implements OnlineCounter {


    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;


    @Override
    public void set(Long nodeId, Long value) {

        redisTemplate.opsForZSet().add(Constant.RedisConstants.REDIS_ONLINE_COUNTER,nodeId,value);
    }

    @Override
    public void increment(Long nodeId) {
        redisTemplate.opsForZSet().incrementScore(Constant.RedisConstants.REDIS_ONLINE_COUNTER,nodeId,1);
    }

    @Override
    public void decrement(Long nodeId) {
        redisTemplate.opsForZSet().incrementScore(Constant.RedisConstants.REDIS_ONLINE_COUNTER,nodeId,-1);
    }

    @Override
    public void remove(Long nodeId) {
        redisTemplate.opsForZSet().remove(Constant.RedisConstants.REDIS_ONLINE_COUNTER,nodeId);
    }


    @Override
    public Long get(Long nodeId) {
        Double score = redisTemplate.opsForZSet().score(Constant.RedisConstants.REDIS_ONLINE_COUNTER, nodeId);
        if(score != null){
            return score.longValue();
        }
        return null;
    }
}
