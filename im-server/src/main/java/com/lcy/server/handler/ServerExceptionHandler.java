package com.lcy.server.handler;

import com.lcy.common.exception.InvalidFrameException;
import com.lcy.server.session.SessionManger;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 出现错误关闭连接
 **/
@Slf4j
@ChannelHandler.Sharable
@Service("ServerExceptionHandler")
public class ServerExceptionHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof InvalidFrameException) {
            log.error(cause.getMessage());

        } else {

            //捕捉异常信息
            cause.printStackTrace();
            log.error(cause.getMessage());
        }

        SessionManger.inst().closeSession(ctx);
        ctx.close();

    }

    /**
     * 通道 Read 读取 Complete 完成
     * 做刷新操作 ctx.flush()
     */
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)
            throws Exception {
        SessionManger.inst().closeSession(ctx);

    }




}