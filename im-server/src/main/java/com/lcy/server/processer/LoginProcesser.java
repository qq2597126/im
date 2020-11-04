package com.lcy.server.processer;

import com.lcy.common.bean.bo.User;
import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.common.constant.ResultCodeEnum;
import com.lcy.server.builder.LoginResponceBuilder;
import com.lcy.server.session.LocalSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author lcy
 * @DESC:
 * @date 2020/9/22.
 */
@Component
public class LoginProcesser extends AbstractServerProcesser{

    @Autowired
    private LoginResponceBuilder loginResponceBuilder;

    @Override
    public ProtoMsg.HeadType type() {
        return ProtoMsg.HeadType.LOGIN_REQUEST;
    }

    @Override
    public boolean action(LocalSession serverSession, ProtoMsg.Message proto) {
        //处理登录
        ProtoMsg.LoginRequest loginRequest = proto.getLoginRequest();

        long sequence = proto.getSequence();

        //获取当前对象
        User user = User.fromMsg(loginRequest);

        //校验用户登录信息
        boolean isLoginSuccess = checkUser(user);
        if(isLoginSuccess){
            serverSession.setUser(user);
            //通道双向绑定
            serverSession.bind();
            ProtoMsg.Message responce = loginResponceBuilder.loginResponce(ResultCodeEnum.SUCCESS, sequence, serverSession.getSessionId());
            serverSession.writeAndFlush(responce);
            return true;
        }else{
            ProtoMsg.Message responce = loginResponceBuilder.loginResponce(ResultCodeEnum.NO_TOKEN, sequence, serverSession.getSessionId());
            serverSession.writeAndFlush(responce);
            return false;
        }
    }


    private boolean checkUser(User user) {


        //校验用户,比较耗时的操作,需要100 ms以上的时间
        //方法1：调用远程用户restfull 校验服务
        //方法2：调用数据库接口校验

        return true;

    }
}
