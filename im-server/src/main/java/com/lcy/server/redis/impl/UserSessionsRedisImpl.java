package com.lcy.server.redis.impl;


import com.alibaba.fastjson.JSON;
import com.lcy.server.distributed.zk.ImZkServerWorker;
import com.lcy.server.redis.UserSessionsDAO;
import com.lcy.server.session.UserSessions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;


@Repository("UserSessionsRedisImpl")
public class UserSessionsRedisImpl implements UserSessionsDAO {

    public static final String REDIS_PREFIX = "UserSessions:uid:";

    @Autowired
    protected RedisTemplate<String,Object> redisTemplate;

    private static final long CASHE_LONG = 60 * 4;//4小时


    @Override
    public void save(final UserSessions uss) {
        String key = REDIS_PREFIX + uss.getUserId();
        redisTemplate.opsForValue().set(key, JSON.toJSONString(uss), CASHE_LONG, TimeUnit.MINUTES);
    }


    @Override
    public UserSessions get(final String usID) {
        String key = REDIS_PREFIX + usID;
        String value = (String) redisTemplate.opsForValue().get(key);

        if (!StringUtils.isEmpty(value)) {
            return JSON.parseObject(value,UserSessions.class);
        }
        return null;
    }

    @Override
    public void cacheUser(String uid, String sessionId) {
        UserSessions us = get(uid);
        if (null == us) {
            us = new UserSessions(uid);
        }
        us.addSession(sessionId, ImZkServerWorker.getInst().getLocalNodeInfo());
        save(us);
    }

    @Override
    public void removeUserSession(String uid, String sessionId) {
        UserSessions us = get(uid);
        if (null == us) {
            us = new UserSessions(uid);
        }
        us.removeSession(sessionId);
        save(us);
    }

    @Override
    public UserSessions getAllSession(String userId) {
        UserSessions us = get(userId);
        if (null != us) {
            return us;
        }
        return null;
    }
}