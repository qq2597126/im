package com.lcy.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ZkConfig {
    //Zk集群地址
    public static String zkAddress;
    //超时时间,单位毫秒
    public static Integer baseSleepTimeMs;

    public static Integer maxRetries;

    //连接超时
    public static Integer connectionTimeoutMs;

    //session 超时
    public static Integer sessionTimeoutMs;

    public String getZkAddress() {
        return zkAddress;
    }
    @Value("${zk.address}")
    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public Integer getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }
    @Value("${zk.baseSleepTimeMs}")
    public void setBaseSleepTimeMs(Integer baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }
    @Value("${zk.maxRetries}")
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    @Value("${zk.connectionTimeoutMs}")
    public void setConnectionTimeoutMs(Integer connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public Integer getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }
    @Value("${zk.sessionTimeoutMs}")
    public void setSessionTimeoutMs(Integer sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }
}
