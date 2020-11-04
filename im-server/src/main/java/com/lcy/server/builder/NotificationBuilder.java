package com.lcy.server.builder;

import com.alibaba.fastjson.JSON;
import com.lcy.common.bean.msg.Notification;
import com.lcy.common.bean.msg.ProtoMsg;

public class NotificationBuilder {

    public static ProtoMsg.Message notification(Notification notification){
        ProtoMsg.Message.Builder mb = ProtoMsg.Message.newBuilder()
                .setType(ProtoMsg.HeadType.MESSAGE_NOTIFICATION);  //设置消息类型

        ProtoMsg.MessageNotification.Builder messageNotification =
                ProtoMsg.MessageNotification.newBuilder().setJson(JSON.toJSONString(notification)).
                    setMsgType(notification.getType());

        mb.setNotification(messageNotification);

        return mb.build();
    }
}
