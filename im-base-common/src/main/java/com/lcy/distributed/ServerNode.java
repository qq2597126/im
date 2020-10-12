package com.lcy.distributed;

import lombok.Data;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 每个服务节点数据
 */
@Data
public class ServerNode implements Comparable<ServerNode>{

    /**
     * 服务节点标识
     */
    private long id;

    /**
     * 当前服务连接的数量
     */
    private AtomicInteger balance;

    /**
     * 当前地址
     */
    private String host;

    /**
     * 端口号
     */
    private Integer port;


    public ServerNode(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerNode that = (ServerNode) o;
        return id == that.id &&
                Objects.equals(balance, that.balance) &&
                Objects.equals(host, that.host) &&
                Objects.equals(port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, balance, host, port);
    }
    /**
     * 用来按照负载升序排列
     */
    public int compareTo(ServerNode serverNode) {
        int weight1 = this.getBalance().get();
        int weight2 = serverNode.getBalance().get();
        if (weight1 > weight2) {
            return 1;
        } else if (weight1 < weight2) {
            return -1;
        }
        return 0;
    }
}
