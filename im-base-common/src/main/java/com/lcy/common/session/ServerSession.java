package com.lcy.common.session;

import com.lcy.common.bean.bo.User;

public interface ServerSession {


    public void writeAndFlush(Object pkg);


    public String getSessionId();

    public boolean isValid();

    public User getUser();

}
