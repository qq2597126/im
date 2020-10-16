package com.lcy.server.distributed;


/**
 * 节点注册到zk服务器上
 */
public  interface ServerWorker {



    /**
     * 增加负载，表示有用户登录成功
     *
     * @return 成功状态
     */
    public boolean incBalance();
    /**
     * 减少负载，表示有用户下线，写回zookeeper
     *
     * @return 成功状态
     */
    public boolean decrBalance();

    /**
     * 获取节点的标识
     */
    public Long getServerId();
}
