package com.lcy.common.bean.bo;


import com.lcy.common.bean.msg.ProtoMsg;
import com.lcy.common.constant.PlatTypeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lcy
 * @DESC:
 * @date 2020/9/1.
 */
@Slf4j
@Data
public class User {
    String uid;
    String devId;
    String token;//标识
    String nickName = "nickName";

    PlatTypeEnum platform = PlatTypeEnum.WINDOWS;


    private String sessionId;


    public void setPlatform(int platform) {
        PlatTypeEnum[] values = PlatTypeEnum.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].ordinal() == platform) {
                this.platform = values[i];
            }
        }

    }


    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", devId='" + devId + '\'' +
                ", token='" + token + '\'' +
                ", nickName='" + nickName + '\'' +
                ", platform=" + platform +
                '}';
    }

    public static User fromMsg(ProtoMsg.LoginRequest info) {
        User user = new User();
        user.uid = new String(info.getUid());
        user.devId = new String(info.getDeviceId());
        user.token = new String(info.getToken());
        user.setPlatform(info.getPlatform());
        return user;

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public PlatTypeEnum getPlatform() {
        return platform;
    }

    public void setPlatform(PlatTypeEnum platform) {
        this.platform = platform;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
