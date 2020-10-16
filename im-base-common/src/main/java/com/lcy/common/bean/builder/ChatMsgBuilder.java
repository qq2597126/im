package com.lcy.common.bean.builder;

import com.lcy.common.bean.bo.ChatMsg;
import com.lcy.common.bean.bo.User;
import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.common.session.ServerSession;
import lombok.Data;


/**
 * @author lcy
 * @DESC:
 * @date 2020/9/4.
 */
@Data
public class ChatMsgBuilder extends BaseMsgBuilder {

    private ChatMsg chatMsg;
    private User user;


    public ChatMsgBuilder(ChatMsg chatMsg, User user, ServerSession session) {
        super(ProtoMsg.HeadType.MESSAGE_REQUEST, session);
        this.chatMsg = chatMsg;
        this.user = user;

    }


    public ProtoMsg.Message build() {
        ProtoMsg.Message message = buildCommon(-1);
        ProtoMsg.MessageRequest.Builder cb
                = ProtoMsg.MessageRequest.newBuilder();

        chatMsg.fillMsg(cb);
        return message
                .toBuilder()
                .setMessageRequest(cb)
                .build();
    }

    public static ProtoMsg.Message buildChatMsg(
            ChatMsg chatMsg,
            User user,
            ServerSession session) {
        ChatMsgBuilder builder =
                new ChatMsgBuilder(chatMsg, user, session);
        return builder.build();

    }
}
