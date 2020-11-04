package com.lcy.server.processer;


import com.lcy.server.session.LocalSession;
import io.netty.channel.Channel;

/**
 * @author lcy
 * @DESC:
 * @date 2020/9/22.
 */
public abstract class AbstractServerProcesser implements ServerProcesser{

    protected String getKey(Channel ch) {
        return ch.attr(LocalSession.KEY_USER_ID).get();
    }

    protected void setKey(Channel ch, String key) {
        ch.attr(LocalSession.KEY_USER_ID).set(key);
    }

    protected void checkAuth(Channel ch) throws Exception {
        if (null == getKey(ch)) {
            throw new Exception("此用户，没有登录成功");
        }
    }
}
