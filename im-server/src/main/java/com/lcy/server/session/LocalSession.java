package com.lcy.server.session;

import com.lcy.common.bean.bo.User;
import com.lcy.common.utils.SessionUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class LocalSession implements ServerSession {

    public static final AttributeKey<String> KEY_USER_ID =
            AttributeKey.valueOf("key_user_id");

    public static final AttributeKey<ServerSession> SESSION_KEY =
            AttributeKey.valueOf("SESSION_KEY");


    /**
     * 绑定的通道
     */
    private Channel channel;


    /**
     * session的唯一标识
     */
    private final String sessionId;

    /**
     * 用户信息
     */
    private  User user;

    /**
     * 是否登录
     * @param channel
     */
    private boolean isLogin;

    /**
     * session中存储的session 变量属性值
     */
    private Map<String, Object> map = new HashMap<String, Object>();


    public LocalSession(Channel channel) {
        this.channel = channel;
        this.sessionId = SessionUtils.createSessionId();
    }
    //反向导航
    public static ServerSession getSession(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        ServerSession serverSession = channel.attr(SESSION_KEY).get();
        return serverSession;
    }

    //关闭连接
    public synchronized void close() {
        ChannelFuture future = channel.close();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws
                    Exception {
                if (!future.isSuccess()) {
                    log.error("CHANNEL_CLOSED error ");
                }
            }
        });
    }

    //和通道实现双向绑定
    public ServerSession bind() {
        log.info(" ServerSession绑定会话 " + channel.remoteAddress());
        channel.attr(SESSION_KEY).set(this);
        isLogin = true;
        return this;
    }

    //写Protobuf数据帧
    public synchronized void writeAndClose(Object pkg) {
        channel.writeAndFlush(pkg);
        close();
    }

    //把Protobuf数据包写入通道
    public synchronized void writeAndFlush(Object pkg) {
        channel.writeAndFlush(pkg);
    }

    @Override
    public boolean isValid() {
        return getUser() != null ? true : false;
    }

    public synchronized void set(String key, Object value) {
        map.put(key, value);
    }


    public synchronized <T> T get(String key) {
        return (T) map.get(key);
    }
}
