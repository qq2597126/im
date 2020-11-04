package com.lcy.server.processer;

import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.server.session.LocalSession;

/**
 * @author lcy
 * @DESC:
 * @date 2020/9/22.
 */
public interface ServerProcesser {
    //请求头
    ProtoMsg.HeadType type();
    //相关操作
    boolean action(LocalSession ch, ProtoMsg.Message proto);
}
