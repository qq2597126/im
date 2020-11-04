package com.lcy.server.handler;

import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.common.concurrent.CallbackTask;
import com.lcy.common.concurrent.CallbackTaskScheduler;
import com.lcy.server.processer.LoginProcesser;
import com.lcy.server.session.LocalSession;
import com.lcy.server.session.SessionManger;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author lcy
 * @DESC:
 * @date 2020/9/22.
 * 处理响应数据
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class LoginRequestHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    private LoginProcesser loginProcesser;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        //判断类型
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = ((ProtoMsg.Message) msg).getType();
        if (!headType.equals(ProtoMsg.HeadType.LOGIN_REQUEST)) {
            super.channelRead(ctx, msg);
            return;
        }

        //处理登录逻辑,创建线程
        LocalSession serverSession = new LocalSession(ctx.channel());

        CallbackTaskScheduler.add(new CallbackTask<Boolean>() {
            @Override
            public Boolean execute() throws Exception {
                return loginProcesser.action(serverSession,pkg);
            }

            @Override
            public void onBack(Boolean b) {
                if(b){ //登录成功
                    //删除登录拦截器
                    ctx.pipeline().remove(LoginRequestHandler.class);
                    //添加本地Session
                    SessionManger.inst().addLocalSession(serverSession);
                    log.info(serverSession.getUser()+": 登录成功");
                }else{ //登录失败
                    SessionManger.inst().closeSession(ctx);
                    log.info(serverSession.getUser()+": 登录失败");
                }
            }
            @Override
            public void onException(Throwable t) {
                SessionManger.inst().closeSession(ctx);
                log.info(serverSession.getUser()+": 登录失败(异常退出)");
            }
        });


    }
}
