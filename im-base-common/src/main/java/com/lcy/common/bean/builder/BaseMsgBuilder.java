package com.lcy.common.bean.builder;

import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.common.session.ServerSession;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lcy
 * @DESC:
 * @date 2020/9/1.
 */
@Slf4j
@Data
public class BaseMsgBuilder {

    protected ProtoMsg.HeadType type;
    private long seqId;
    private ServerSession session;

    public BaseMsgBuilder(ProtoMsg.HeadType type, ServerSession session) {
        this.type = type;
        this.session = session;
    }

    /**
     * 构建消息 基础部分
     */
    public ProtoMsg.Message buildCommon(long seqId) {
        this.seqId = seqId;

        ProtoMsg.Message.Builder mb =
                ProtoMsg.Message
                        .newBuilder()
                        .setType(type)
                        .setSessionId(session.getSessionId())
                        .setSequence(seqId);
        return mb.buildPartial();
    }

}
