package com.lcy.server.handler;

import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.common.concurrent.ExecuteTask;
import com.lcy.common.concurrent.FutureTaskScheduler;
import com.lcy.server.session.SessionManger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author lcy
 * @DESC:
 * @date 2020/9/24.
 */
public class HeartBeatServerHandler extends IdleStateHandler {
    private static final int READ_IDLE_GAP = 150; //最大空闲，单位s
    public HeartBeatServerHandler() {
        super(READ_IDLE_GAP,0,0,TimeUnit.SECONDS);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt)
            throws Exception {
        System.out.println(READ_IDLE_GAP + "秒内未读到数据，关闭连接");
        SessionManger.inst().closeSession(ctx);
    }
    //读取检测数据
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        //判断消息类型
        ProtoMsg.HeadType headType = pkg.getType();

        if (headType.equals(ProtoMsg.HeadType.KEEPALIVE_REQUEST)) {
            //异步处理,将心跳数据包直接回复给客户端
            FutureTaskScheduler.add(new ExecuteTask() {
                @Override
                public void execute() {
                    if (ctx.channel().isActive()) {
                        ctx.writeAndFlush(msg);
                    }
                }

                @Override
                public void onException(Throwable t) {

                }
            });
        }
        super.channelRead(ctx, msg);
    }
}
