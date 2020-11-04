package com.lcy.server.processer;

import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.server.session.LocalSession;
import com.lcy.common.session.ServerSession;
import com.lcy.server.session.SessionManger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lcy
 * @DESC:
 * @date 2020/9/23.
 */
@Slf4j
@Component
public class ChatRedirectProcesser extends AbstractServerProcesser{
    @Override
    public ProtoMsg.HeadType type() {
        return ProtoMsg.HeadType.MESSAGE_REQUEST;
    }

    @Override
    public boolean action(LocalSession ch, ProtoMsg.Message proto) {
        // 聊天处理
        ProtoMsg.MessageRequest msg = proto.getMessageRequest();
        log.info("chatMsg | from="
                + msg.getFrom()
                + " , to=" + msg.getTo()
                + " , content=" + msg.getContent());
        // 获取接收方的chatID
        String to = msg.getTo();

        List<ServerSession> toSessions = SessionManger.inst().getSessionsBy(to);
        if (toSessions == null || toSessions.size() == 0) {
            //接收方离线
            log.info("[" + to + "] 不在线，发送失败!");
        } else {

            toSessions.forEach((session) -> {
                // 将IM消息发送到接收方
                session.writeAndFlush(proto);
            });
        }
        return true;
    }
}
