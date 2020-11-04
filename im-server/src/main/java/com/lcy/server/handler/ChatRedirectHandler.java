package com.lcy.server.handler;

import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.common.concurrent.CallbackTask;
import com.lcy.common.concurrent.CallbackTaskScheduler;
import com.lcy.common.session.ServerSession;
import com.lcy.server.processer.ChatRedirectProcesser;
import com.lcy.server.session.LocalSession;
import com.lcy.server.session.SessionManger;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lcy
 * @DESC:
 * @date 2020/9/23.
 */
@ChannelHandler.Sharable
@Slf4j
@Component
public class ChatRedirectHandler extends ChannelInboundHandlerAdapter {
    //事件处理
    @Autowired
    private ChatRedirectProcesser chatRedirectProcesser;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        //判断消息类型
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = ((ProtoMsg.Message) msg).getType();
        if (!headType.equals(chatRedirectProcesser.type())) {
            super.channelRead(ctx, msg);
            return;
        }


        //发送消息
        CallbackTaskScheduler.add(new CallbackTask() {
            @Override
            public Object execute() throws Exception {
                //判断是否登录
                LocalSession session = LocalSession.getSession(ctx);

                if (null != session && session.isLogin()) {
                    chatRedirectProcesser.action(session, pkg);
                    return null;
                }

                ProtoMsg.MessageRequest request = pkg.getMessageRequest();
                List<ServerSession> sessions = SessionManger.inst().getSessionsBy(request.getTo());
                final boolean[] isSended = {false};
                sessions.forEach((serverSession) -> {

                    if (serverSession instanceof LocalSession)
                    // 将IM消息发送到接收方
                    {
                        serverSession.writeAndFlush(pkg);
                        isSended[0] =true;
                    }

                });

                if(!isSended[0])
                {
                    log.error("用户尚未登录，不能接受消息");
                }

                return null;
            }

            @Override
            public void onBack(Object o) {

            }

            @Override
            public void onException(Throwable t) {

            }
        });


    }
}
