package com.lcy.server.handler;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lcy.common.bean.msg.Notification;
import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.common.constant.Constant;
import com.lcy.server.distributed.ImServerNode;
import com.lcy.server.session.LocalSession;
import com.lcy.server.session.RemoteSession;
import com.lcy.server.session.SessionManger;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("RemoteNotificationHandler")
@ChannelHandler.Sharable
public class RemoteNotificationHandler
        extends ChannelInboundHandlerAdapter {

    /**
     * 收到消息
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;

        //取得请求类型,如果不是通知消息，直接跳过
        ProtoMsg.HeadType headType = pkg.getType();

        if (!headType.equals(ProtoMsg.HeadType.MESSAGE_NOTIFICATION)) {
            super.channelRead(ctx, msg);
            return;
        }

        //处理消息的内容
        ProtoMsg.MessageNotification notificationPkg = pkg.getNotification();

        String json = notificationPkg.getJson();



        //节点的链接成功
        switch (notificationPkg.getMsgType()){

            case Notification.CONNECT_FINISHED:

                Notification<ImServerNode> nodInfo =
                        JSON.parseObject(json, new TypeReference<Notification<ImServerNode>>() {
                        }.getType());


                log.info("收到分布式节点连接成功通知, node={}", json);

                //删除登录验证
                ctx.pipeline().remove("loginRequest");
                //ctx.pipeline().remove("heartBeat");

                ctx.channel().attr(Constant.ImServerConstants.CHANNEL_NAME).set(JSON.toJSONString(nodInfo.getData()));

                break;
            case Notification.SESSION_ON :

                Notification<RemoteSession> notification = JSON.parseObject(json,new TypeReference<Notification<RemoteSession>>(){}.getType());
                log.info("收到用户上线通知, node={}", json);
                RemoteSession remoteSession = notification.getData();
                SessionManger.inst().addRemoteSession(remoteSession);
                break;
            case Notification.SESSION_OFF :
                notification = JSON.parseObject(json,new TypeReference<Notification<RemoteSession>>(){}.getType());
                log.info("收到用户下线通知, node={}", json);
                remoteSession = notification.getData();
                SessionManger.inst().removeRemoteSession(remoteSession.getSessionId());
                break;
            default:
                log.info("新增通知消息类型暂时无法处理");
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx)
            throws Exception {
        LocalSession localSession = (LocalSession) LocalSession.getSession(ctx);

        if (null != localSession) {
            localSession.unbind();
        }
    }
}
