package com.lcy.server.handler;


import com.lcy.common.bean.msg.ProtoMsg;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@ChannelHandler.Sharable
public class ImNodeHeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    //心跳的时间间隔，单位为s
    private static final int HEARTBEAT_INTERVAL = 100;

    //在Handler被加入到Pipeline时，开始发送心跳
    @Override
    public void handlerAdded(ChannelHandlerContext ctx)
            throws Exception {

        ProtoMsg.Message.Builder mb =
                ProtoMsg.Message
                        .newBuilder()
                        .setType(ProtoMsg.HeadType.KEEPALIVE_REQUEST)
                        .setSessionId("unknown")
                        .setSequence(-1);
        ProtoMsg.Message message =   mb.buildPartial();
        ProtoMsg.MessageHeartBeat.Builder lb =
                ProtoMsg.MessageHeartBeat.newBuilder()
                        .setSeq(0)
                        .setJson("{\"from\":\"imNode\"}")
                        .setUid("-1");
        message.toBuilder().setMessageHeartBeat(lb).build();

        //发送心跳
        heartBeat(ctx, message);
    }

    //使用定时器，发送心跳报文
    public void heartBeat(ChannelHandlerContext ctx,
                          ProtoMsg.Message heartbeatMsg) {
        ctx.executor().schedule(() -> {

            if (ctx.channel().isActive()) {
                log.info(" 发送 ImNode HEART_BEAT  消息 other");
                ctx.writeAndFlush(heartbeatMsg);

                //递归调用，发送下一次的心跳
                heartBeat(ctx, heartbeatMsg);
            }

        }, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        //判断类型
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = pkg.getType();
        if (headType.equals(ProtoMsg.HeadType.KEEPALIVE_REQUEST)) {
            log.info(" imNode 收到回写的 HEART_BEAT  消息 from other node");
            return;
        } else {
            super.channelRead(ctx, msg);

        }

    }

}