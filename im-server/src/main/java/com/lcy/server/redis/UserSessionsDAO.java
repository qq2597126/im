package com.lcy.server.redis;

import com.lcy.server.session.UserSessions;

public interface UserSessionsDAO {


    public void save(UserSessions s);

    public UserSessions get(String sessionid);

    public void cacheUser(String uid, String sessionId);

    public void removeUserSession(String uid, String sessionId);

    public UserSessions getAllSession(String userId);
}
