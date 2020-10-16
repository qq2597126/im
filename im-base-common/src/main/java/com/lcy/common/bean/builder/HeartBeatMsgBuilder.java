package com.lcy.common.bean.builder;

import com.lcy.common.bean.bo.User;
import com.lcy.common.bean.msg.ProtoMsg;
import com.mysql.cj.protocol.ServerSession;


/**
 * @author lcy
 * @DESC:2
 * @date 2020/9/25.
 */

public class HeartBeatMsgBuilder extends BaseMsgBuilder{

    private  User user;

    public HeartBeatMsgBuilder(ServerSession session) {
        super(ProtoMsg.HeadType.KEEPALIVE_REQUEST, session);
        this.user = session.getUser();
    }

    public ProtoMsg.Message buildMsg() {
        ProtoMsg.Message message = buildCommon(-1);
        ProtoMsg.MessageHeartBeat.Builder messageHeartBeat = ProtoMsg.MessageHeartBeat.newBuilder().setSeq(0).setJson("{\"from\":\"client\"}").setUid(user.getUid());
        return message.toBuilder().setMessageHeartBeat(messageHeartBeat).build();
    }
}
