package com.lcy.common.bean.builder;

import com.lcy.common.bean.bo.User;
import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.common.session.ServerSession;

/**
 * @author lcy
 * @DESC:
 * @date 2020/9/1.
 */
public class LoginMsgBuilder extends BaseMsgBuilder {

    private final User user;

    public LoginMsgBuilder(User user, ServerSession session) {
        super(ProtoMsg.HeadType.LOGIN_REQUEST, session);
        this.user = user;
    }

    public ProtoMsg.Message build() {
        ProtoMsg.Message message = buildCommon(-1);
        ProtoMsg.LoginRequest.Builder lb =
                ProtoMsg.LoginRequest.newBuilder()
                        .setDeviceId(user.getDevId())
                        .setPlatform(user.getPlatform().ordinal())
                        .setToken(user.getToken())
                        .setUid(user.getUid());
        return message.toBuilder().setLoginRequest(lb).build();
    }

    public static ProtoMsg.Message buildLoginMsg(
            User user, ServerSession session) {
        LoginMsgBuilder builder =
                new LoginMsgBuilder(user, session);
        return builder.build();

    }
}
