package com.lcy.server.session;

public interface ServerSession {


    public void writeAndFlush(Object pkg);


    public String getSessionId();

    public boolean isValid();
}
